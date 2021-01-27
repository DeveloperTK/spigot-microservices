package de.christianschliz.spigotms.api;

import de.christianschliz.spigotms.plugin.SpigotMS;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import java.lang.reflect.Field;

/**
 * Spigot microservices superclass
 *
 * @author Christian Schliz
 * @version 1.0
 * */
@SuppressWarnings("unused")
public abstract class SpigotService {

    // -- instance fields

    private SpigotMS pluginInstance;

    private boolean enabledByDefault;
    private boolean isEnabled;

    private FileConfiguration configuration;

    // -- constructors

    /**
     * Default constructor
     * */
    public SpigotService() {
        enabledByDefault = true;
        isEnabled = false;
    }

    // -- public methods

    /**
     * onEnable is called when the service gets enabled.
     * Methods like {@link SpigotService#registerCommand(String, CommandExecutor)}
     * and {@link SpigotService#registerEvents(Listener)} are available here.
     * */
    protected void onEnable() {
    }

    /**
     * Check whether the service should be enabled by default
     * and does so if required.
     * */
    public void tryEnable() {
        if (this.configuration.contains("enable") && this.configuration.getBoolean("enable")) {
            this.isEnabled = true;
            onEnable();
        }
    }

    /**
     * Enables the service
     * */
    public void doEnable() {
        this.isEnabled = true;
        onEnable();
    }


    /**
     * onDisable is called when the service gets disabled.
     * Files or states should be saved here
     * */
    protected void onDisable() {
    }

    /**
     * Disables the service
     * */
    public void doDisable() {
        this.isEnabled = false;
    }

    /**
     * On load is called when the service got loaded but before any other
     * service was enabled. It is ideal for loading states or configurations.
     * */
    protected void onLoad() {
    }

    /**
     * Calls the onLoad function after the service was
     * loaded into the local repository.
     * */
    public void doLoad() {
        onLoad();
    }

    /**
     * Registers commands directly to the command map,
     * which eliminates the need to mention the command
     * in a plugin.yml configuration.
     *
     * @param commandLabel command name
     * @param commandExecutor executor class
     * */
    protected void registerCommand(String commandLabel, CommandExecutor commandExecutor) {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register(commandLabel, new Command(commandLabel) {
                @Override
                public boolean execute(CommandSender commandSender, String label, String[] args) {
                    return commandExecutor.onCommand(commandSender, this, label, args);
                }
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.printf("Failed to register command %s"
                    + " because the commandMap could not be accessed!%n", commandLabel);
            e.printStackTrace();
        }
    }

    /**
     * Passes the listener class to the plugin manager so that
     * this long ass call doesn't need to be executed every
     * time a listener class is registered.
     *
     * @param eventListener listener class
     * */
    protected void registerEvents(Listener eventListener) {
        this.pluginInstance.getServer().getPluginManager().registerEvents(eventListener, this.pluginInstance);
    }

    /**
     * Unregisters all events from a listener class. This needs
     * to be called manually in onDisable() because the service
     * manager on purpose doesn't unregister events automatically.
     *
     * @param eventListener listener class
     * */
    protected void unregisterEvents(Listener eventListener) {
        HandlerList.unregisterAll(eventListener);
    }

    // -- getter and setter

    /**
     * @return SpigotMS plugin instance
     * */
    public SpigotMS getPlugin() {
        return pluginInstance;
    }

    /**
     * @param pluginInstance new plugin instance
     * */
    public void setPluginInstance(SpigotMS pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    /**
     * @param enabledByDefault If true, the service will be enabled automatically upon load.
     * */
    public void setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    /**
     * @param configuration links the service.yml config to the service class
     * */
    public void setConfiguration(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @return boolean whether the service should be enabled after loading
     * */
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    /**
     * @return boolean whether the service is enabled and running
     * */
    public boolean isEnabled() {
        return isEnabled;
    }
}
