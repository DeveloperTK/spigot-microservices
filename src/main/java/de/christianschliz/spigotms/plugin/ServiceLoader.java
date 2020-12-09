package de.christianschliz.spigotms.plugin;

import de.christianschliz.spigotms.api.SpigotService;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ServiceLoader {

    // -- instance fields

    private final SpigotMS pluginInstance;

    private final File localServiceDirectory;
    private final File[] remoteServiceDirectories;
    private final HashMap<String, SpigotService> services;

    // -- constructors

    /**
     * The ServiceManager is responsible for loading, enabling
     * and disabling all services. It searches the local service
     * directory in the plugin folder, but can also retrieve
     * services from your network.
     *
     * @param localServiceDirectory    The path to the local directory.
     *                                  Usually: <code>JavaPlugin.getDataFolder() + "/services"</code>
     * @param remoteServiceDirectories A list of remote directories on your drive
     *                                  or your network. Services from there are loaded
     *                                  but not enabled by default and have to be explicitly
     *                                  set to be enabled via the <code>service.yml</code>
     * @param pluginInstance           The JavaPlugin instance, which all services reference
     *                                  when for example registering command or events.
     * */
    public ServiceLoader(final File localServiceDirectory,
                         final List<File> remoteServiceDirectories, SpigotMS pluginInstance) {
        this.localServiceDirectory = localServiceDirectory;
        this.remoteServiceDirectories = remoteServiceDirectories.toArray(new File[0]);
        this.pluginInstance = pluginInstance;
        services = new HashMap<>();
    }

    // -- public methods

    /**
     * Only searches the services directory inside
     * the plugin folder and loads them.
     * */
    @SuppressWarnings("unused")
    public void loadLocalServices() {
        registerServicesFromDirectories(this.localServiceDirectory, true);
    }

    /**
     * Enables all services loaded into {@link ServiceLoader#services}.
     * */
    public void enableLoadedServices() {
        services.forEach((name, service) -> {
            System.out.println("[SpigotMS] Enabling service " + name + " at " + service.getClass().getCanonicalName());
            service.tryEnable();
        });
    }

    /**
     * Disables all services loaded into {@link ServiceLoader#services}.
     * */
    public void disableLoadedServices() {
        services.forEach((name, service) -> {
            System.out.println("[SpigotMS] Disabling service " + name + " at " + service.getClass().getCanonicalName());
            service.doDisable();
        });
    }

    /**
     * Looks for services in local and remote directories
     * and loads all services found.
     * */
    @SuppressWarnings("unused")
    public void loadServices() {
        if (Objects.nonNull(this.localServiceDirectory)) {
            registerServicesFromDirectories(this.localServiceDirectory, true);
        }

        if (Objects.nonNull(this.remoteServiceDirectories)) {
            for (File directory : this.remoteServiceDirectories) {
                registerServicesFromDirectories(directory, false);
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
        if (this.services.containsKey(name)) {
            return this.services.get(name);
        } else {
            System.err.printf("No such service named %s found %n", name);
            return null;
        }
    }

    // -- private methods

    private void registerServicesFromDirectories(final File directory, final boolean isEnabledByDefault) {
        URL[] urls = new URL[0];

        if (Objects.isNull(directory)) {
            // No directory specified
            System.out.println("Cannot load Services from File: null");
        } else if (!directory.canRead()) {
            // Can't read from the specified path
            System.out.println("Cannot load Services from a read protected path!");
        } else if (directory.isDirectory()) {
            // Load all files within the specified directory
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
        } else if (directory.isFile() && directory.getName().endsWith(".jar")) {
            // Only load one jar file
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
            // Load all .jar files from the target directory into the SpigotMS classpath
            URLClassLoader serviceClassLoader = new URLClassLoader(urls, SpigotMS.class.getClassLoader());

            // A list of all service.yml config files found inside the directory
            Enumeration<URL> configPaths = serviceClassLoader.findResources("service.yml");

            // Iterate over every service.yml config file
            while (configPaths.hasMoreElements()) {
                // Read the file
                InputStreamReader config = new InputStreamReader(configPaths.nextElement().openStream());

                // load the YAML structure
                FileConfiguration fileConfiguration = new YamlConfiguration();
                fileConfiguration.load(config);

                // check if the configuration file has all necessary fields
                if (fileConfiguration.contains("name") && fileConfiguration.contains("main")
                        && fileConfiguration.contains("enable")) {
                    try {
                        // Load the target class
                        Class<?> testServiceClass = Class.forName(
                                fileConfiguration.getString("main"), true, serviceClassLoader);

                        // Create an instance of the targeted class
                        SpigotService serviceInstance = (SpigotService)
                                testServiceClass.getDeclaredConstructor().newInstance();

                        serviceInstance.setEnabledByDefault(fileConfiguration.getBoolean("enable"));
                        serviceInstance.setPluginInstance(this.pluginInstance);
                        serviceInstance.setConfiguration(fileConfiguration);

                        // Add the service to the list
                        services.put(fileConfiguration.getString("name"), serviceInstance);
                    } catch (InstantiationException | InvocationTargetException | NoSuchMethodException exception) {
                        // Could either not cast to SpigotService or the jar is obfuscated
                        exception.printStackTrace();
                    } catch (IllegalAccessException | ClassNotFoundException exception) {
                        // bad jar
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

    // -- getter and setter

    @SuppressWarnings("unused")
    public File getLocalServiceDirectory() {
        return localServiceDirectory;
    }

    @SuppressWarnings("unused")
    public File[] getRemoteServiceDirectories() {
        return remoteServiceDirectories;
    }

    @SuppressWarnings("unused")
    public HashMap<String, SpigotService> getServices() {
        return services;
    }
}
