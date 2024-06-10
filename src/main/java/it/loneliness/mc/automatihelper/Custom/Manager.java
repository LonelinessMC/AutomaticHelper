package it.loneliness.mc.automatihelper.Custom;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import it.loneliness.mc.automatihelper.Plugin;
import it.loneliness.mc.automatihelper.Controller.Announcement;
import it.loneliness.mc.automatihelper.Controller.ConfigManager.CONFIG_ITEMS;
import it.loneliness.mc.automatihelper.Model.FIFOCache;
import it.loneliness.mc.automatihelper.Model.LogHandler;

public class Manager implements Listener {

    private static final long CACHE_DURATION = TimeUnit.SECONDS.toMillis(10);
    private static final String ALLOWED_CHARACTERS = "a-zA-ZàèéìòùÀÈÉÌÒÙçÇ .,;!?";
    private static final Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile("^[ " + ALLOWED_CHARACTERS + "]+$");

    private String kickMessage;
    private int maxNotTrustedPlayer;
    private String trustedPlayerPermission;
    private String chatDeleteMessage;
    private String invalidCharMessage;
    private String tooLongMessage;
    private int maxMexLength;
    private int cachedUntrustedPlayerCount = -1;
    private long lastCacheTime = 0;

    private boolean untrustedChatDisabled;

    private Announcement anouncement;
    private FIFOCache<String, String> ipCache;


    public Manager(Plugin plugin, LogHandler logger) {
        kickMessage = plugin.getConfigManager().getString(CONFIG_ITEMS.UNABLE_TO_JOIN_MESSAGE);
        maxNotTrustedPlayer = plugin.getConfigManager().getInt(CONFIG_ITEMS.MAX_UNVERIFIED_PLAYERS);
        trustedPlayerPermission = plugin.getConfigManager().getString(CONFIG_ITEMS.TRUSTED_PLAYER_PERMISSION);
        chatDeleteMessage = plugin.getConfigManager().getString(CONFIG_ITEMS.CHAT_DELETE_MESSAGE);
        invalidCharMessage = plugin.getConfigManager().getString(CONFIG_ITEMS.INVALID_CHAR_MESSAGE);
        tooLongMessage = plugin.getConfigManager().getString(CONFIG_ITEMS.TOO_LONG_MESSAGE);
        maxMexLength = plugin.getConfigManager().getInt(CONFIG_ITEMS.MAX_MEX_LENGTH);

        anouncement = Announcement.getInstance(plugin);

        untrustedChatDisabled = false; // when plugin starts we don't want to disable the chat

        ipCache = new FIFOCache<>(100);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if(p != null && !p.hasPermission(trustedPlayerPermission)){
            if(untrustedChatDisabled){
                event.setCancelled(true);
                anouncement.sendPrivateMessage(p, chatDeleteMessage);
                return;
            }
        }
        String message = event.getMessage();

        if(message.length() > maxMexLength){
            event.setCancelled(true);
            anouncement.sendPrivateMessage(p, tooLongMessage);
            return;
        }

        if(!ALLOWED_CHARACTERS_PATTERN.matcher(message).matches()){
            event.setCancelled(true);
            anouncement.sendPrivateMessage(p, invalidCharMessage);
            return;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPermission(trustedPlayerPermission)){
            int onlineUntrustedPlayerCount = getOnlineUntrustedPlayerCound();
            if(onlineUntrustedPlayerCount >= maxNotTrustedPlayer){
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
                return;
            }

            String ipAddress = player.getAddress().getHostString();
            String playerName = player.getName();
            String existingPlayerName = ipCache.get(ipAddress);
            if(existingPlayerName != null && !existingPlayerName.equals(playerName)){
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
                return;
            } else {
                ipCache.put(ipAddress, playerName);
            }
        }
    }

    private int getOnlineUntrustedPlayerCound() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheTime > CACHE_DURATION) {
            cachedUntrustedPlayerCount = (int) Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.hasPermission(trustedPlayerPermission))
                    .count();
            lastCacheTime = currentTime;
        }
        return cachedUntrustedPlayerCount;
    }

    public void onDisable() {
    }

    public void setUntrustedChatDisabled(boolean value){
        this.untrustedChatDisabled = value;
    }

    public void flushIPCache(){
        this.ipCache.clear();
    }

}
