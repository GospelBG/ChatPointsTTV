package me.gosdev.chatpointsttv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotConfigFile implements ConfigFile {
    private FileConfiguration config;
    private final File file;

    public SpigotConfigFile(String fileName) {
        this.file = new File(SpigotPlugin.getPlugin().getDataFolder(), fileName);
        if (!this.file.exists()) {
            SpigotPlugin.getPlugin().saveResource(fileName, false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }
    public SpigotConfigFile(JavaPlugin plugin) {
        this.file = null;
        this.config = plugin.getConfig();
    }

    @Override
    public Boolean contains(String key) {
        return config.contains(key);
    }

    @Override
    public List<String> getKeys(String key) {
        if (key == null || key.isEmpty()) {
            return new ArrayList<>(config.getKeys(false));
        } else {
            return new ArrayList<>(config.getConfigurationSection(key).getKeys(false));
        }
    }

    @Override
    public void set(String key, Object value) {
        config.set(key, value);
    }

    @Override
    public Boolean getBoolean(String key, Boolean def) {
        return config.getBoolean(key, def);
    }

    @Override
    public String getString(String key) {
        return config.getString(key);
    }

    @Override
    public String getString(String key, String def) {
        return config.getString(key, def);
    }

    @Override
    public Boolean isString(String key) {
        return config.isString(key);
    }

    @Override
    public List<String> getSectionKeys(String key) {
        if (!config.isConfigurationSection(key)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(config.getConfigurationSection(key).getKeys(false));
    }
    @Override
    public Boolean isSection(String key) {
        return config.isConfigurationSection(key);
    }

    @Override
    public List<String> getStringList(String key) {
        return config.getStringList(key);
    }

    @Override
    public Boolean isList(String key) {
        return config.isList(key);
    }

    @Override
    public void save() throws IOException {
        if (file != null) {
            config.save(file);
        } else {
            SpigotPlugin.getPlugin().saveConfig();
        }
    }

    @Override
    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}
