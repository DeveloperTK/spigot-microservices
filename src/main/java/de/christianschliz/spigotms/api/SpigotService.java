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

import java.io.File;
import java.lang.reflect.Field;

@SuppressWarnings("unused")
public abstract class SpigotService {

    // -- instance fields

    private SpigotMS pluginInstance;

    private boolean enabledByDefault;
    private boolean isEnabled;

    private FileConfiguration configuration;

    // -- constructors

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

    public void tryEnable() {
        if(this.configuration.contains("enable") && this.configuration.getBoolean("enable")) {
            this.isEnabled = true;
            onEnable();
        }
    }

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

    public void doDisable() {
        this.isEnabled = false;
    }

    /**
     * On load is called when the service got loaded but before any other
     * service was enabled. It is ideal for loading states or configurations.
     * */
    protected void onLoad() {
    }

    public void doLoad() {
        onLoad();
    }

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

    protected void registerEvents(Listener eventListener) {
        this.pluginInstance.getServer().getPluginManager().registerEvents(eventListener, this.pluginInstance);
    }

    protected void unregisterEvents(Listener eventListener) {
        HandlerList.unregisterAll(eventListener);
    }

    // -- getter and setter

    public SpigotMS getPlugin() {
        return pluginInstance;
    }

    public void setPluginInstance(SpigotMS pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public void setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    public void setConfiguration(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
