package org.example.Config;

import org.flywaydb.core.Flyway;

/**
 * Ejecuta las migraciones de Flyway ANTES de que JPA inicialice.
 *
 * Cómo funciona:
 *  - Flyway guarda un historial en la tabla "flyway_schema_history".
 *  - Al arrancar, compara qué migraciones V*.sql ya se aplicaron.
 *  - Solo ejecuta las nuevas. Nunca repite las ya aplicadas.
 *  - Para añadir un cambio: crea un nuevo archivo V3__descripcion.sql.
 *    Nunca modifiques los anteriores.
 */
public class FlywayConfig {

    private static final String URL      = "jdbc:h2:./data/taskhub_jpa;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USER     = "sa";
    private static final String PASSWORD = "";

    public static void migrate() {
        Flyway flyway = Flyway.configure()
            .dataSource(URL, USER, PASSWORD)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)   // si la BD existe pero sin historial Flyway, la baseline en V1
            .load();

        var result = flyway.migrate();
        if (result.migrationsExecuted > 0) {
            System.out.println("\u001B[36m[Flyway] " + result.migrationsExecuted
                + " migración(es) aplicada(s).\u001B[0m");
        }
    }
}
