package cl.hcs.finder.appointmentback.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.lang.reflect.Proxy;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import reactor.test.StepVerifier;

class HealthControllerTest {

    private HealthController healthController;

    @Test
    void returnsUpWhenDatabaseRespondsToPing() {
        healthController = new HealthController(new StubDataSource(true));

        StepVerifier.create(healthController.health())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(Map.of("status", "UP", "database", "UP"), response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void returnsServiceUnavailableWhenDatabaseIsDown() {
        healthController = new HealthController(new StubDataSource(false));

        StepVerifier.create(healthController.health())
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    assertEquals(Map.of("status", "DOWN", "database", "DOWN"), response.getBody());
                })
                .verifyComplete();
    }

    private static class StubDataSource implements DataSource {
        private final boolean available;

        StubDataSource(boolean available) {
            this.available = available;
        }

        @Override
        public Connection getConnection() throws SQLException {
            if (!available) {
                throw new SQLException("database unavailable");
            }
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[] { Connection.class },
                    (proxy, method, args) -> {
                        if (method.getName().equals("isValid")) {
                            return true;
                        }
                        if (method.getName().equals("isClosed")) {
                            return false;
                        }
                        return null;
                    });
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not a wrapper");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        @Override
        public java.io.PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(java.io.PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public java.util.logging.Logger getParentLogger() {
            return java.util.logging.Logger.getGlobal();
        }
    }
}
