package de.christianschliz.spigotms.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.christianschliz.spigotms.api.database.DatabaseControllers;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The plugin class for the SpigotMicroservices module.
 *
 * @author Christian Schliz
 * @version 1.0
 * */
public final class SpigotMS extends JavaPlugin {

    private ServiceLoader serviceManager;
    private MicroserviceCommand microserviceCommand;
    private DatabaseControllers databaseControllers;

    // -- public methods

    @Override
    public void onEnable() {
        serviceManager.enableLoadedServices();
        this.getServer().getPluginCommand("microservices").setExecutor(microserviceCommand);
    }

    @Override
    public void onLoad() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.serviceManager = loadServiceManager();
        serviceManager.loadLocalServices();

        microserviceCommand = new MicroserviceCommand(this);
    }

    @Override
    public void onDisable() {
        serviceManager.disableLoadedServices();
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

    // -- getter and setter

    public ServiceLoader getServiceManager() {
        return this.serviceManager;
    }

    public DatabaseControllers getDatabaseControllers() {
        return this.databaseControllers;
    }
}
