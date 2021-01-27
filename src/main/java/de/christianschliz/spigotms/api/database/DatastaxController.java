package de.christianschliz.spigotms.api.database;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import java.net.InetSocketAddress;

/**
 * @author Christian Schliz
 * @version 1.0
 * */
public class DatastaxController implements DatabaseRepository<CqlSession> {

    private CqlSession session;

    private final InetSocketAddress endpoint;
    private final String username;
    private final String password;

    /**
     * The CQL Controller class for access within SpigotServices.
     *
     * @param address The connection adress
     * @param port The connection port
     * @param username Basic authentication
     * @param password with just user/password
     * */
    public DatastaxController(final String address, final int port, final String username, final String password) {
        this.endpoint = new InetSocketAddress(address, port);
        this.username = username;
        this.password = password;
    }

    @Override
    public void connect() {
        try {
            session = CqlSession.builder()
                    .addContactPoint(this.endpoint)
                    .withAuthCredentials(this.username, this.password)
                    .build();
            session.executeAsync("select release_version from system.local").thenAccept(result -> {
                if (result == null) {
                    System.err.println("Could not connect to Cassandra database.");
                }
            });
        } catch (NullPointerException exception) {
            System.err.println("Error while connecting to Cassandra database at " + endpoint.getHostString());
            exception.printStackTrace();
        }
    }

    @Override
    public CqlSession get() {
        return session;
    }
}
