package de.eldoria.shepard.database;

import de.chojo.sadu.databases.PostgreSql;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.eldoria.shepard.core.configuration.Config;
import de.eldoria.shepard.modulebuilder.requirements.ReqConfig;
import de.eldoria.shepard.modulebuilder.requirements.ReqInit;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
public class ConnectionPool implements ReqConfig, ReqInit {
    private DataSource source;
    private Config config;

    /**
     * Create a new connection pool.
     */
    public ConnectionPool() {
    }

    /**
     * Get the data source of the pool.
     *
     * @return data source obbject
     */
    public DataSource getSource() {
        return source;
    }

    @Override
    public void addConfig(Config config) {
        this.config = config;
    }

    @Override
    public void init() {
        try {
            var db = config.getDatabase();
            source = DataSourceCreator.create(PostgreSql.postgresql())
                    .configure(builder ->
                            builder.applicationName("Shepard Legacy")
                                   .host(db.getAddress())
                                   .database(db.getDb())
                                   .user(db.getUsername())
                                   .password(db.getPassword())
                                   .port(db.getPort()))
                    .create()
                            .withPoolName("PG Source")
                            .withMaximumPoolSize(5)
                            .withMinimumIdle(1)
                            .build();

            try (var conn = source.getConnection()) {
                conn.isValid(10);
            }
            log.info("Created new connectionpool for {}@{}:{}/{}",
                    db.getUsername(), db.getAddress(),
                    db.getPort(), db.getDb());
        } catch (Exception e) {
            log.error("Could not connect to database. Retrying in 10.", e);
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException ignore) {
            }
            init();
        }
    }
}
