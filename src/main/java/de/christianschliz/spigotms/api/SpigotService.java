package de.christianschliz.spigotms.api;

import de.christianschliz.spigotms.plugin.SpigotMS;

public abstract class SpigotService {

    private SpigotMS pluginInstance;

    public void setPluginInstance(SpigotMS pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public SpigotMS getPlugin() {
        return pluginInstance;
    }

    public void broadcast(String message) {
        pluginInstance.getServer().broadcastMessage(message);
    }

    public abstract void onEnable();
}
