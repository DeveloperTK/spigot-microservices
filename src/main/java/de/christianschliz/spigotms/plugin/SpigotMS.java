package de.christianschliz.spigotms.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class SpigotMS extends JavaPlugin {

    ServiceManager serviceManager;

    @Override
    public void onEnable() {
        serviceManager.enableLoadedServices();
    }

    @Override
    public void onLoad() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        File localServiceFolder = new File(getDataFolder() + "/services");

        if(!localServiceFolder.exists() && localServiceFolder.getParentFile().canWrite()) {
            localServiceFolder.mkdirs();
        }

        List<File> remoteServiceDirectories = new ArrayList<>();

        if(getConfig().contains("remoteServiceDirectories")) {
            List<String> remoteServiceDirectoryPaths = getConfig().getStringList("remoteServiceDirectories");

            for(String path : remoteServiceDirectoryPaths) {
                File directory = new File(path);

                if(directory.exists() && directory.canRead()) {
                    remoteServiceDirectories.add(directory);
                } else {
                    System.out.println("Cannot read contents of remote path: " + path);
                }
            }
        }

        serviceManager = new ServiceManager(localServiceFolder, remoteServiceDirectories, this);

        serviceManager.loadLocalServices();
    }

    @Override
    public void onDisable() {
        serviceManager.disableLoadedServices();
    }
}
