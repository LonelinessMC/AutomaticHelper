package it.loneliness.mc.automatihelper.Controller;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import it.loneliness.mc.automatihelper.Plugin;
import it.loneliness.mc.automatihelper.Custom.Manager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final List<String> allowedPrefixes;
    private final String permissionPrefix;

    private final List<CommandEntry> commandList;
    private Manager manager;
    private final Announcement announcement;

    public CommandHandler(Plugin plugin, List<String> allowedPrefixes, Manager manager) {
        this.plugin = plugin;
        this.allowedPrefixes = allowedPrefixes;
        this.permissionPrefix = allowedPrefixes.get(0); // Set the primary permission prefix

        commandList = new ArrayList<CommandEntry>();
        commandList.add(new CommandEntry("help", permissionPrefix, this::getHelp, true));
        commandList.add(new CommandEntry("disable", permissionPrefix,
                params -> setEnabledCommand(params.sender, params.cmd, params.label, params.args, false), true));
        commandList.add(new CommandEntry("enable", permissionPrefix,
                params -> setEnabledCommand(params.sender, params.cmd, params.label, params.args, true), true));
        commandList.add(new CommandEntry("status", permissionPrefix, this::getStatusCommand, true));
        commandList.add(new CommandEntry("help", permissionPrefix, this::getHelp, true));
        commandList.add(new CommandEntry("disableuntrustedchat", permissionPrefix, this::disableUntrustedChat, true));
        commandList.add(new CommandEntry("enableuntrustedchat", permissionPrefix, this::enableUntrustedChat, true));
        commandList.add(new CommandEntry("flushipcache", permissionPrefix, this::flushIpCache, true));

        this.manager = manager;
        this.announcement = Announcement.getInstance(plugin);
    }

    private boolean flushIpCache(CommandParams params) {
        manager.flushIPCache();
        Announcement.getInstance(plugin).sendPrivateMessage(params.sender,
                "Flushed ip cache of untrusted players");
        return true;
    }

    private boolean disableUntrustedChat(CommandParams params) {
        manager.setUntrustedChatDisabled(true);
        Announcement.getInstance(plugin).sendPrivateMessage(params.sender,
                "Chat disabled for untrusted players");
        return true;
    }

    private boolean enableUntrustedChat(CommandParams params) {
        manager.setUntrustedChatDisabled(false);
        Announcement.getInstance(plugin).sendPrivateMessage(params.sender,
                "Chat enabled for untrusted players");
        return true;
    }

    private boolean getHelp(CommandParams params){
        String output = " - comandi possibili:\n";
        for (CommandEntry command : commandList) {
            if(command.isAllowed(params.sender)){
                output += "/"+command.permissionPrefix+" "+command.commandName+"\n";
            }
        }
        Announcement.getInstance(plugin).sendPrivateMessage(params.sender, output);
        return true;
    }

    private boolean setEnabledCommand(CommandSender sender, Command cmd, String label, String[] args, boolean enabled) {
        if (enabled) {
            Announcement.getInstance(plugin).sendPrivateMessage(sender, "Use plugman to disable the plugin");
        } else {
            Announcement.getInstance(plugin).sendPrivateMessage(sender, "Use plugman to enabled the plugin");
        }

        return true;
    }

    private boolean getStatusCommand(CommandParams params) {
        Announcement.getInstance(plugin).sendPrivateMessage(params.sender,
                "Scheduler running: " + this.plugin.getTaskScheduler().isRunning());
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }

        // Check if the command starts with any of the allowed prefixes
        boolean validPrefix = false;
        for (String prefix : allowedPrefixes) {
            if (cmd.getName().equalsIgnoreCase(prefix)) {
                validPrefix = true;
                break;
            }
        }

        if (!validPrefix) {
            return false;
        }

        String commandName = args[0].toLowerCase();
        CommandEntry commandEntry = commandList.stream().filter(command -> command.commandName.equals(commandName))
                .findFirst().orElse(null);

        if (commandEntry == null ||
                !commandEntry.isAllowed(sender)) {
            return false;
        }

        return commandEntry.commandFunction.apply(new CommandParams(sender, cmd, label, args));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        // Check if the command starts with any of the allowed prefixes
        boolean validPrefix = false;
        for (String prefix : allowedPrefixes) {
            if (cmd.getName().equalsIgnoreCase(prefix)) {
                validPrefix = true;
                break;
            }
        }

        if (!validPrefix) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String partialCommand = args[0].toLowerCase();
            return commandList.stream()
                    .filter(command -> command.commandName.startsWith(partialCommand))
                    .filter(command -> command.isAllowed(sender))
                    .map(command -> command.commandName)
                    .toList();
        }

        return new ArrayList<>();
    }

    private static class CommandParams {
        CommandSender sender;
        Command cmd;
        String label;
        String[] args;

        CommandParams(CommandSender sender, Command cmd, String label, String[] args) {
            this.sender = sender;
            this.cmd = cmd;
            this.label = label;
            this.args = args;
        }
    }

    private static class CommandEntry {
        String commandName;
        String permissionPrefix;
        Function<CommandParams, Boolean> commandFunction;
        boolean consoleAllowed;

        CommandEntry(String commandName, String permissionPrefix, Function<CommandParams, Boolean> commandFunction,
                boolean consoleAllowed) {
            this.commandName = commandName;
            this.permissionPrefix = permissionPrefix;
            this.commandFunction = commandFunction;
            this.consoleAllowed = consoleAllowed;
        }

        boolean isAllowed(CommandSender sender) {
            if (!(sender instanceof Player) && this.consoleAllowed) {
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                String permission = permissionPrefix + "." + this.commandName;

                if (player.hasPermission(permission)) {
                    return true;
                }
            }

            return false;
        }
    }
}
