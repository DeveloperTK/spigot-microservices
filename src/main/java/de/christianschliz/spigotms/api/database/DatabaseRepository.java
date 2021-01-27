package de.christianschliz.spigotms.api.database;

/**
 * @author Christian Schliz
 * @version 1.0
 *
 * @param <T> Database driver session/connection object
 * */
public interface DatabaseRepository<T> {

    /**
     * Creates the database connection. Should be called,
     * when the plugin is enabled, otherwise it slows down
     * the server start.
     * */
    void connect();

    /**
     * Gets the database connection object.
     *
     * @return T database connection object
     * */
    T get();
}
