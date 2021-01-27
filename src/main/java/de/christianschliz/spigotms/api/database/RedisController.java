package de.christianschliz.spigotms.api.database;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @author Christian Schliz
 * @version 1.0
 * */
public final class RedisController
        implements DatabaseRepository<RedissonClient> {

    private final Config config;
    private RedissonClient redissonClient;

    /**
     * The Redis Controller class for access within SpigotServices.
     *
     * @param nodeAddresses Redis endpoints
     * @param password The corresponding password
     * */
    public RedisController(final String[] nodeAddresses, final String password) {
        this.config = new Config();

        if (nodeAddresses.length == 1) {
            config.useSingleServer().setPassword(password).setAddress(nodeAddresses[0]);

        } else if (nodeAddresses.length > 1) {
            config.useClusterServers().setPassword(password).addNodeAddress(nodeAddresses);
        }
    }

    @Override
    public void connect() {
        redissonClient = Redisson.create(config);
    }

    @Override
    public RedissonClient get() {
        return redissonClient;
    }
}
