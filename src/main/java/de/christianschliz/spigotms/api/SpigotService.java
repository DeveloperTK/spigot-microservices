package de.christianschliz.spigotms.api;

import de.christianschliz.spigotms.plugin.SpigotMS;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public abstract class SpigotService {

    private SpigotMS pluginInstance;

    private final boolean enabledByDefault;

    // constructors

    public SpigotService(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    public SpigotService() {
        this(true);
    }

    // public methods

    /**
     * onEnable is called when the service gets enabled.
     * Methods like {@link SpigotService#registerCommand(String, CommandExecutor)}
     * and {@link SpigotService#registerEvent(Listener)} are available here.
     * */
    public void onEnable() {
    }

    /**
     * onDisable is called when the service gets disabled.
     * Files or states should be saved here
     * */
    public void onDisable() {
    }

    /**
     * On load is called when the service got loaded but before any other
     * service was enabled. It is ideal for loading states or configurations.
     * */
    public void onLoad() {
    }

    public void registerCommand(String commandLabel, CommandExecutor commandExecutor) {
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

    public void registerEvent(Listener eventListener) {
        this.pluginInstance.getServer().getPluginManager().registerEvents(eventListener, this.pluginInstance);
    }

    // getter and setter

    public SpigotMS getPlugin() {
        return pluginInstance;
    }

    public void setPluginInstance(SpigotMS pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }
}
