package de.christianschliz.spigotms.api.database;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public final class RedisController
        implements DatabaseRepository<RedissonClient> {

    private Config config;
    private RedissonClient redissonClient;

    public RedisController(final String... nodeAddresses) {
        this.config = new Config();

        if (nodeAddresses.length == 1) {
            config.useSingleServer().setAddress(nodeAddresses[0]);
        } else if (nodeAddresses.length > 1) {
            config.useClusterServers().addNodeAddress(nodeAddresses);
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
