package it.loneliness.mc.automatihelper.Controller;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    public enum CONFIG_ITEMS {
        //TODO if any of the keys is the same return an error, hopefully at compile time
        CHAT_PREFIX("chatPrefix", "&#FE6847[&#FBB13CLoneHelper&#FE6847] "),
        MAX_UNVERIFIED_PLAYERS("maxUnverifiedPlayers", 20),
        UNABLE_TO_JOIN_MESSAGE("unableToJoinMessage", "Chiedi su discord.loneliness.it di accedere alla coda prioritaria"),
        CHAT_DELETE_MESSAGE("chatDeleteMessage", "Chat temporaneamente disabilitata per gli stranieri, chiedi su discord.loneliness.it di verificare il tuo account"),
        INVALID_CHAR_MESSAGE("invalidCharMessage", "Messaggio non consentito per gli stranieri, chiedi su discord.loneliness.it di verificare il tuo account"),
        TOO_LONG_MESSAGE("tooLongMessage", "Messaggio troppo lungo, chiedi su discord.loneliness.it di verificare il tuo account"),
        MAX_MEX_LENGTH("maxMexLength", 30),
        TRUSTED_PLAYER_PERMISSION("trustedPlayerPermission", "automatichelper.trustedplayer"),
        DEBUG("debug", true);

        private final String key;
        private final Object defaultValue;

        CONFIG_ITEMS(String key, Object defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey() {
            return key;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        loadConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        addMissingDefaults();
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    private void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        addMissingDefaults();
    }

    private void addMissingDefaults() {
        boolean saveNeeded = false;
        for (CONFIG_ITEMS item : CONFIG_ITEMS.values()) {
            if (!config.contains(item.getKey())) {
                config.set(item.getKey(), item.getDefaultValue());
                saveNeeded = true;
            }
        }
        if (saveNeeded) {
            saveConfig();
        }
    }

    // Typized getter methods
    public String getString(CONFIG_ITEMS item) {
        return getConfig().getString(item.getKey(), (String) item.getDefaultValue());
    }

    public int getInt(CONFIG_ITEMS item) {
        return getConfig().getInt(item.getKey(), (Integer) item.getDefaultValue());
    }

    public List<String> getStringList(CONFIG_ITEMS item) {
        return getConfig().getStringList(item.getKey());
    }

    public Boolean getBoolean(CONFIG_ITEMS item){
        return getConfig().getBoolean(item.getKey());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> getMapList(CONFIG_ITEMS item) {
        return (List<Map<String,Object>>) getConfig().getList(item.getKey(), (List<Map<String,Object>>) item.getDefaultValue());
    }

    // Typized setter methods
    public void setString(CONFIG_ITEMS item, String value) {
        config.set(item.getKey(), value);
        saveConfig();
    }

    public void setInt(CONFIG_ITEMS item, int value) {
        config.set(item.getKey(), value);
        saveConfig();
    }

    public void setStringList(CONFIG_ITEMS item, List<String> value) {
        config.set(item.getKey(), value);
        saveConfig();
    }

    public void setBoolean(CONFIG_ITEMS item, Boolean value) {
        config.set(item.getKey(), value);
        saveConfig();
    }
}
