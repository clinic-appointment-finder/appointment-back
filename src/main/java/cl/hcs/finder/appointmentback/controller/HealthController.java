package cl.hcs.finder.appointmentback.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Endpoint used by orchestrators to verify application and database readiness. */
@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        return Mono.fromCallable(this::isDatabaseAvailable)
                // DataSource access is blocking and must not run on the WebFlux event loop.
                .subscribeOn(Schedulers.boundedElastic())
                .map(available -> available
                        ? ResponseEntity.ok(Map.of("status", "UP", "database", "UP"))
                        : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(Map.of("status", "DOWN", "database", "DOWN")));
    }

    private boolean isDatabaseAvailable() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (SQLException exception) {
            return false;
        }
    }
}
