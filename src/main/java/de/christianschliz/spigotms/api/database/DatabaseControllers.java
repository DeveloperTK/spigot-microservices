package de.christianschliz.spigotms.api.database;

/**
 * @author Christian Schliz
 * @version 1.0
 * */
public final class DatabaseControllers {

    private MySQLController mysqlController;
    private RedisController redisController;
    private DatastaxController datastaxController;

    /**
     * Provides services with the different database driver
     * connection/session objects.
     *
     * @param mysql MySQL controller class
     * @param redis Redis controller class
     * @param datastax Datastax controller class
     * */
    public DatabaseControllers(final MySQLController mysql,
                               final RedisController redis,
                               final DatastaxController datastax) {
        mysqlController = mysql;
        redisController = redis;
        datastaxController = datastax;
    }

    /**
     * Provides services with the different database driver
     * connection/session objects.
     *
     * @see DatabaseControllers#DatabaseControllers
     * (MySQLController, RedisController, DatastaxController)
     * */
    public DatabaseControllers() {
    }

    /**
     * Provides the MySQL driver with the required
     * connection and authentication information.
     *
     * @param hostname MySQL Server endpoint
     * @param port MySQL Server port
     * @param database The standard Microservice Database
     * @param username basic authentication
     * @param password basic authentication
     * */
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

    /**
     * Provides the.
     *
     * @param connectionNodes Redis server addresses
     * @param password The database password
     * */
    public void configureRedis(final String[] connectionNodes, final String password) {
        redisController = new RedisController(connectionNodes, password);
    }

    /**
     * Does.
     *
     * @param address address
     * @param port port
     * @param username username
     * @param password password
     * */
    public void configureDatastax(final String address, final int port, final String username, final String password) {
        datastaxController = new DatastaxController(address, port, username, password);
    }

    /**
     * @return MySQLController Controller
     * */
    public MySQLController mysql() {
        return mysqlController;
    }

    /**
     * @return RedisController Controller
     * */
    public RedisController redis() {
        return redisController;
    }

    /**
     * @return DatastaxController Controller
     * */
    public DatastaxController datastax() {
        return datastaxController;
    }

}
