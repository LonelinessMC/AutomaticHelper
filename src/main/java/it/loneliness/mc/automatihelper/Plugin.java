package it.loneliness.mc.automatihelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import it.loneliness.mc.automatihelper.Controller.CommandHandler;
import it.loneliness.mc.automatihelper.Controller.ConfigManager;
import it.loneliness.mc.automatihelper.Controller.TaskScheduler;
import it.loneliness.mc.automatihelper.Custom.Manager;
import it.loneliness.mc.automatihelper.Model.LogHandler;

public class Plugin extends JavaPlugin{
    LogHandler logger;
    CommandHandler commandHandler;
    TaskScheduler taskScheduler;
    ConfigManager configManager;
    boolean enabled;
    Manager manager;
    
    @Override
    public void onEnable() {
        logger = LogHandler.getInstance(getLogger());
        logger.info("Enabling the plugin");

        configManager = new ConfigManager(this);

        if(configManager.getBoolean(ConfigManager.CONFIG_ITEMS.DEBUG)){
            logger.setDebug(true);
        }
        
        manager = new Manager(this, logger);
        this.getServer().getPluginManager().registerEvents(manager, this);

        try {
            //Make sure this is alligned with the plugin.yml, the first in the list is used for the permissions
            List<String> prefixes = new ArrayList<>(Arrays.asList("automatichelper", "ah"));
            this.commandHandler = new CommandHandler(this, prefixes, manager);
            for(String prefix : prefixes){
                this.getCommand(prefix).setExecutor(commandHandler);
                this.getCommand(prefix).setTabCompleter(commandHandler);
            }
        } catch(NullPointerException e){
            logger.severe("Ensure you're defining the same comands both in plugin.yml as well as in Plugin.java");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {
        logger.info("Disabling the plugin");
        manager.onDisable();
    }

    public TaskScheduler getTaskScheduler(){
        return this.taskScheduler;
    }

    public ConfigManager getConfigManager(){
        return this.configManager;
    }
}