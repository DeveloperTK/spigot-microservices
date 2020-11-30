package de.christianschliz.spigotms.plugin;

import de.christianschliz.spigotms.api.SpigotService;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class ServiceManager {

    private final SpigotMS pluginInstance;

    private final File localServiceDirectory;
    private final File[] remoteServiceDirectories;
    private final HashMap<String, SpigotService> services;

    // Constructors

    /**
     * The ServiceManager is responsible for loading, enabling
     * and disabling all services. It searches the local service
     * directory in the plugin folder, but can also retrieve
     * services from your network.
     *
     * @param _localServiceDirectory    The path to the local directory.
     *                                  Usually: <code>JavaPlugin.getDataFolder() + "/services"</code>
     * @param _remoteServiceDirectories A list of remote directories on your drive
     *                                  or your network. Services from there are loaded
     *                                  but not enabled by default and have to be explicitly
     *                                  set to be enabled via the <code>service.yml</code>
     * @param _pluginInstance           The JavaPlugin instance, which all services reference
     *                                  when for example registering command or events.
     * */
    public ServiceManager(final File _localServiceDirectory,
                          final List<File> _remoteServiceDirectories, SpigotMS _pluginInstance) {
        this.localServiceDirectory = _localServiceDirectory;
        this.remoteServiceDirectories = _remoteServiceDirectories.toArray(new File[0]);
        this.pluginInstance = _pluginInstance;
        services = new HashMap<>();
    }

    // public methods

    /**
     * Only searches the services directory inside
     * the plugin folder and loads them.
     * */
    @SuppressWarnings("unused")
    public void loadLocalServices() {
        registerServicesFromDirectors(this.localServiceDirectory, true);
    }

    /**
     * Enables all services loaded into {@link ServiceManager#services}
     * */
    public void enableLoadedServices() {
        services.forEach((name, service) -> {
            System.out.println("[SpigotMS] Enabling service " + name + " at " + service.getClass().getCanonicalName());
            service.onEnable();
        });
    }

    /**
     * Disables all services loaded into {@link ServiceManager#services}
     * */
    public void disableLoadedServices() {
        services.forEach((name, service) -> {
            System.out.println("[SpigotMS] Disabling service " + name + " at " + service.getClass().getCanonicalName());
            service.onDisable();
        });
    }

    /**
     * Looks for services in local and remote directories
     * and loads all services found.
     * */
    @SuppressWarnings("unused")
    public void loadServices() {
        if(Objects.nonNull(this.localServiceDirectory)) {
            registerServicesFromDirectors(this.localServiceDirectory, true);
        }

        if (Objects.nonNull(this.remoteServiceDirectories)) {
            for(File directory : this.remoteServiceDirectories) {
                registerServicesFromDirectors(directory, false);
            }
        }
    }

    /**
     * Returns a SpigotService by its name. Used for
     * Interaction between services or via the SpigotMS
     * command tools.
     *
     * @param name The unique name of a service.
     * @return SpigotService service with the given name.
     * */
    @SuppressWarnings("unused")
    public SpigotService getService(String name) {
        if(this.services.containsKey(name)) {
            return this.services.get(name);
        } else {
            System.err.printf("No such service named %s found %n", name);
            return null;
        }
    }

    // private methods

    private void registerServicesFromDirectors(final File directory, final boolean isEnabledByDefault) {
        URL[] urls = new URL[0];

        if (Objects.isNull(directory)) {
            System.out.println("Cannot load Services from File: null");
        } else if(!directory.canRead()) {
            System.out.println("Cannot load Services from a read protected path!");
        } else if(directory.isDirectory()) {
            ArrayList<File> files = new ArrayList<>(
                    Arrays.asList(
                            Objects.requireNonNull(directory.listFiles())
                    )
            );
            files.removeIf(f -> !f.getName().endsWith(".jar"));

            urls = new URL[files.size()];

            for (int i = 0; i < files.size(); i++) {
                try {
                    urls[i] = files.get(i).toURI().toURL();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if(directory.isFile() && directory.getName().endsWith(".jar")) {
            try {
                urls = new URL[]{directory.toURI().toURL()};
            } catch (MalformedURLException exception) {
                exception.printStackTrace();
            }
        } else {
            System.out.println("Services must be in the .jar format");
        }

        loadJARsFromURLIntoClasspath(urls, isEnabledByDefault);
    }

    private void loadJARsFromURLIntoClasspath(final URL[] urls, final boolean isEnabledByDefault) {
        try {
            URLClassLoader serviceClassLoader = new URLClassLoader(urls, SpigotMS.class.getClassLoader());

            Enumeration<URL> configPaths = serviceClassLoader.findResources("service.yml");

            while (configPaths.hasMoreElements()) {
                InputStreamReader config = new InputStreamReader(configPaths.nextElement().openStream());
                FileConfiguration fileConfiguration = new YamlConfiguration();

                fileConfiguration.load(config);

                if(fileConfiguration.contains("name") && fileConfiguration.contains("main")) {
                    try {
                        Class<?> testServiceClass = Class.forName(
                                fileConfiguration.getString("main"), true, serviceClassLoader);

                        SpigotService serviceInstance;

                        if (fileConfiguration.contains("enable")) {
                            serviceInstance = (SpigotService) testServiceClass.getDeclaredConstructor(Boolean.class)
                                    .newInstance(fileConfiguration.getBoolean("enable"));
                        } else {
                            serviceInstance = (SpigotService) testServiceClass.getDeclaredConstructor(Boolean.class)
                                    .newInstance(isEnabledByDefault);
                        }

                        serviceInstance.setPluginInstance(this.pluginInstance);

                        services.put(fileConfiguration.getString("name"), serviceInstance);
                    } catch (InstantiationException | InvocationTargetException | NoSuchMethodException exception) {
                        exception.printStackTrace();
                    } catch (IllegalAccessException | ClassNotFoundException exception) {
                        System.err.println("Please check your configuration files");
                        exception.printStackTrace();
                    }
                } else {
                    System.out.printf("Invalid service.yml found at %s%n", fileConfiguration.getCurrentPath());
                }
            }
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
        }
    }

    // Getter and Setter

    @SuppressWarnings("unused")
    public File getLocalServiceDirectory() {
        return localServiceDirectory;
    }

    @SuppressWarnings("unused")
    public File[] getRemoteServiceDirectories() {
        return remoteServiceDirectories;
    }
}
