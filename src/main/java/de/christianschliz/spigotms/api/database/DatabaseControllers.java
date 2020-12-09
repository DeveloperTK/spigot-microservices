package de.christianschliz.spigotms.api.database;

import com.datastax.oss.driver.api.core.CqlSession;

public final class DatabaseControllers {

    private MySQLController mysqlController;
    private RedisController redisController;
    private DatastaxController datastaxController;

    public DatabaseControllers(final MySQLController mysql,
                               final RedisController redis,
                               final DatastaxController datastax) {
        mysqlController = mysql;
        redisController = redis;
        datastaxController = datastax;
    }

    public DatabaseControllers() {
    }

    public void configureMySQL(final String hostname,
                               final int port,
                               final String database,
                               final String username,
                               final String password) {
        mysqlController = new MySQLController(
                hostname,
                port,
                database,
                username,
                password
        );
    }

    public void configureRedis(final String... connectionNodes) {
        redisController = new RedisController(connectionNodes);
    }

    public void configureDatastax(final String address, final int port, final String username, final String password) {
        datastaxController = new DatastaxController(address, port, username, password);
    }

    public MySQLController mysql() {
        return mysqlController;
    }

    public RedisController redis() {
        return redisController;
    }

    public DatastaxController datastax() {
        return datastaxController;
    }

}
