package de.christianschliz.spigotms.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import de.christianschliz.spigotms.api.database.DatabaseControllers;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The plugin class for the SpigotMicroservices module.
 *
 * @author Christian Schliz
 * @version 1.0
 */
public final class SpigotMS extends JavaPlugin {

    private ServiceLoader serviceLoader;
    private MicroserviceCommand microserviceCommand;
    private DatabaseControllers databaseControllers;

    // -- public methods

    @Override
    public void onEnable() {
        serviceLoader.enableLoadedServices();
        this.getServer().getPluginCommand("microservices").setExecutor(microserviceCommand);
        enableDatabases();
    }

    @Override
    public void onLoad() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.serviceLoader = loadServiceManager();
        serviceLoader.loadLocalServices();

        microserviceCommand = new MicroserviceCommand(this);
    }

    @Override
    public void onDisable() {
        serviceLoader.disableLoadedServices();
    }

    // -- private methods

    private ServiceLoader loadServiceManager() {
        File localServiceFolder = new File(getDataFolder() + "/services");

        if (!localServiceFolder.exists() && localServiceFolder.getParentFile().canWrite()) {
            localServiceFolder.mkdirs();
        }

        List<File> remoteServiceDirectories = new ArrayList<>();

        if (getConfig().contains("remoteServiceDirectories")) {
            List<String> remoteServiceDirectoryPaths = getConfig().getStringList("remoteServiceDirectories");

            for (String path : remoteServiceDirectoryPaths) {
                File directory = new File(path);

                if (directory.exists() && directory.canRead()) {
                    remoteServiceDirectories.add(directory);
                } else {
                    System.out.println("Cannot read contents of remote path: " + path);
                }
            }
        }

        return new ServiceLoader(localServiceFolder, remoteServiceDirectories, this);
    }

    private void enableDatabases() {
        if (getConfig().contains("database.mysql.enabled") && getConfig().getBoolean("database.mysql.enabled")) {
            if(configContains("database.mysql.", "server", "port", "database", "username", "password")) {
                databaseControllers.configureMySQL(
                        getConfig().getString("database.mysql.server"),
                        getConfig().getInt("database.mysql.port"),
                        getConfig().getString("database.mysql.database"),
                        getConfig().getString("database.mysql.username"),
                        getConfig().getString("database.mysql.password")
                );
            } else {
                Bukkit.getLogger().log(Level.CONFIG, "MySQL configuration invalid!");
            }
        } else if (getConfig().contains("database.redis.enabled") && getConfig().getBoolean("database.redis.enabled")) {
            if(configContains("database.redis.", "redisNodes", "password")) {
                databaseControllers.configureRedis(
                        getConfig().getStringList("database.redis.redisNodes").toArray(new String[0]),
                        getConfig().getString("database.redis.password")
                );
            } else {
                Bukkit.getLogger().log(Level.CONFIG, "Redis configuration invalid!");
            }
        } else if (getConfig().contains("database.datastax.enabled") && getConfig().getBoolean("database.datastax.enabled")) {
            if(configContains("database.mysql.", "server", "port", "username", "password")) {
                databaseControllers.configureDatastax(
                        getConfig().getString("database.mysql.server"),
                        getConfig().getInt("database.mysql.port"),
                        getConfig().getString("database.mysql.username"),
                        getConfig().getString("database.mysql.password")
                );
            } else {
                Bukkit.getLogger().log(Level.CONFIG, "Cassandra/Datastax configuration invalid!");
            }
        }
    }

    private boolean configContains(String prefix, String... keys) {
        for (String key : keys) {
            if (!getConfig().contains(prefix + key))
                return false;
        }

        return true;
    }

    // -- getter and setter

    /**
     * @return ServiceLoader service manager
     */
    public ServiceLoader getServiceLoader() {
        return this.serviceLoader;
    }

    /**
     * @return DatabaseControllers database controllers
     */
    @SuppressWarnings("unused")
    public DatabaseControllers getDatabaseControllers() {
        return this.databaseControllers;
    }
}
