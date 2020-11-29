package de.christianschliz.spigotms.plugin;

import de.christianschliz.spigotms.api.SpigotService;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class SpigotMS extends JavaPlugin {

    @Override
    public void onEnable() {
        saveConfig();

        try {
            List<File> files = Arrays.asList(new File(getDataFolder().getPath() + "/services").listFiles());
            URL[] urls = new URL[files.size()];
            for (int i = 0; i < files.size(); i++) {
                try {
                    urls[i] = files.get(i).toURI().toURL();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            URLClassLoader serviceClassLoader = new URLClassLoader(urls, SpigotMS.class.getClassLoader());

            try {
                Class<?> testServiceClass = Class.forName("me.test.TestService", true, serviceClassLoader);

                SpigotService serviceInstance = (SpigotService) testServiceClass.getDeclaredConstructor().newInstance();
                serviceInstance.setPluginInstance(this);
                serviceInstance.onEnable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
