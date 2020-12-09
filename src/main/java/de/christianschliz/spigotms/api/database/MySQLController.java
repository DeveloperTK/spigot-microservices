package de.christianschliz.spigotms.api.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class MySQLController
        implements DatabaseRepository<HikariDataSource> {

    private final HikariConfig hikariConfig;
    private HikariDataSource dataSource;

    /**
     * The MySQL Controller class for access within SpigotServices.
     *
     * @param jdbcConnectionString The JDBC Connection URI
     * @param username A valid mysql database user
     * @param password The corresponding password
     * */
    public MySQLController(final String jdbcConnectionString,
                           final String username, final String password) {
        this.hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcConnectionString);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    }

    /**
     * The MySQL Controller class for access within SpigotServices.
     *
     * @param hostname MySQL server hostname
     * @param port     MySQL server port
     * @param database Database for service
     * @param username A valid mysql database user
     * @param password The corresponding password
     * */
    public MySQLController(final String hostname,
                           final int port,
                           final String database,
                           final String username,
                           final String password) {
        this(String.format("jdbc:mysql://%s:%s/%s", hostname, port, database),
                username, password);
    }

    @Override
    public void connect() {
        if (dataSource == null) {
            dataSource = new HikariDataSource(hikariConfig);
        }
    }

    @Override
    public HikariDataSource get() {
        return dataSource;
    }
}
