package de.christianschliz.spigotms.plugin;

import java.io.File;
import java.util.HashMap;

public class ServiceManager {

    private File serviceFolder;
    private HashMap<String, String> services;

    public static ServiceManager createFromFolder(File serviceFolder) {
        ServiceManager manager = new ServiceManager();

        manager.setServiceFolder(serviceFolder);

        return manager;
    }

    public static ServiceManager create() {
        return new ServiceManager();
    }

    public File getServiceFolder() {
        return serviceFolder;
    }

    public void setServiceFolder(File serviceFolder) {
        this.serviceFolder = serviceFolder;
    }
}
