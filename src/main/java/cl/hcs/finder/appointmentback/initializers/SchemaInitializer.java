package cl.hcs.finder.appointmentback.initializers;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import cl.hcs.finder.appointmentback.common.Helper;
import jakarta.annotation.PostConstruct;


@Component
public class SchemaInitializer {

    private static final Logger log = Helper.getLogger();

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.properties.hibernate.default_schema}") // Inyecta el valor del esquema
    private String defaultSchema;

    @Autowired
    public SchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createSchemaIfNotExists() {
        log.info("Verificando existencia del esquema " + defaultSchema + "...");
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + defaultSchema);
        log.info("Esquema " + defaultSchema + " verificado.");
    }
}