package me.gosdev.chatpointsttv;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import me.gosdev.chatpointsttv.Config.SpigotConfigs;
import me.gosdev.chatpointsttv.Config.SpigotConfigFile;
import me.gosdev.chatpointsttv.Spigot.SpigotListeners;
import me.gosdev.chatpointsttv.Spigot.SpigotLoader;
import me.gosdev.chatpointsttv.Spigot.SpigotAccountsManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.gosdev.chatpointsttv.Commands.SpigotCommandController;
import me.gosdev.chatpointsttv.Commands.TikTokCommandController;
import me.gosdev.chatpointsttv.Commands.TwitchCommandController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatPointsTTVSpigot extends JavaPlugin {
    private final AtomicBoolean isReloading = new AtomicBoolean(false);
    private static ChatPointsTTVSpigot plugin;
    private SpigotCommandController cmdController;
    private TwitchCommandController twitchCmdController;
    private TikTokCommandController tikTokCmdController;
    private boolean firstRun = false;

    public SpigotConfigFile config;
    public Metrics metrics;

    public static ChatPointsTTVSpigot getPlugin() {
        return plugin;
    }
    private static ChatPointsTTV chatPointsTTV;

    public Boolean isReloading() {
        return isReloading.get();
    }

    public ChatPointsTTV getChatPointsTTV() {
        return chatPointsTTV;
    }

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getServer().getPluginManager();

        setupBStats();
        
        if (!plugin.getDataFolder().exists()) firstRun = true;

        this.saveDefaultConfig();
        reloadConfig();

        chatPointsTTV = new ChatPointsTTV(new SpigotLoader(), new SpigotConfigs(), new SpigotAccountsManager());
        chatPointsTTV.onEnable();

        File stringsFile = new File(plugin.getDataFolder(), "locales.yml");
        if (!stringsFile.exists()) {
            plugin.saveResource("locales.yml", false);
        }
        
        FileConfiguration stringsYaml = YamlConfiguration.loadConfiguration(stringsFile);
        FileConfiguration defaultStrings = YamlConfiguration.loadConfiguration(plugin.getTextResource("locales.yml"));
        ChatPointsTTV.getInstance().locales = new HashMap<>();
        for (String key : defaultStrings.getKeys(true)) {
            if (stringsYaml.isString(key)) {
                ChatPointsTTV.getInstance().locales.put(key, stringsYaml.getString(key));
            } else {
                ChatPointsTTV.getInstance().locales.put(key, defaultStrings.getString(key));
            }
        }

        if (cmdController == null) {
            cmdController = new SpigotCommandController();
            this.getCommand("cpttv").setExecutor(cmdController);
            this.getCommand("cpttv").setTabCompleter(cmdController);
        }

        if (twitchCmdController == null) {
            twitchCmdController = new TwitchCommandController();
            this.getCommand("twitch").setExecutor(twitchCmdController);
            this.getCommand("twitch").setTabCompleter(twitchCmdController);
        }

        if (tikTokCmdController == null) {
            tikTokCmdController = new TikTokCommandController();
            this.getCommand("tiktok").setExecutor(tikTokCmdController);
            this.getCommand("tiktok").setTabCompleter(tikTokCmdController);
        }

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.spigot().sendMessage(new TextComponent("ChatPointsTTV reloaded!"));
            }
        }

        if (firstRun) {
            Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + "Configuration files have just been created. You need to set up ChatPointsTTV before using it.\nCheck out the quick start guide at https://gosdev.me/chatpointsttv/install");
        }

        pm.registerEvents(new SpigotListeners(), this);
        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (!player.getPlayer().hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) return;
                if (firstRun) {
                    TextComponent welcomeMsg = new TextComponent("  ------------ " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "Welcome to ChatPointsTTV" + ChatColor.RESET + " ------------" + ChatColor.GRAY +
                        "\nThanks for installing ChatPointsTTV!\nYou " + ChatColor.BOLD + "need to set up the configuration files " + ChatColor.RESET + ChatColor.GRAY + "in order to use the plugin.\nYou can take a look at the quick start guide ");
                    
                    TextComponent link = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "here");
                    link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gosdev.me/chatpointsttv/install"));
                    link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));

                    welcomeMsg.addExtra(link);
                    welcomeMsg.addExtra(".\n  -------------------------------------------------\n");
                    player.getPlayer().spigot().sendMessage(welcomeMsg);
                }
                if (!VersionCheck.runningLatest) {
                    TextComponent updPrompt = new TextComponent(ChatColor.YELLOW + "ChatPointsTTV v" + VersionCheck.latestVersion + " has been released!\n" + ChatColor.YELLOW + "Click ");
                    
                    TextComponent updBtn = new TextComponent(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "here");
                    updBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create())); 
                    updBtn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VersionCheck.download_url));

                    updPrompt.addExtra(updBtn);
                    updPrompt.addExtra("" + ChatColor.RESET + ChatColor.YELLOW + " to download the latest version.\n");
    
                    player.getPlayer().spigot().sendMessage(updPrompt);
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {
        if (ChatPointsTTV.getInstance() != null) {
            ChatPointsTTV.getInstance().onDisable();
        }
        // Erase variables
        config = null;
        plugin = null;

        HandlerList.unregisterAll(this);
    }

    private void setupBStats() {
        if (metrics == null) metrics = new Metrics(this, 22873);

        metrics.addCustomChart(new SingleLineChart("twitchModule", () -> {
            if (ChatPointsTTV.getTwitch() != null && ChatPointsTTV.getTwitch().isAccountConnected()) {
                return 1;
            } else {
                return 0;
            }
        }));
        metrics.addCustomChart(new SingleLineChart("tiktokModule", () -> {
            if (ChatPointsTTV.getTikTok() != null && ChatPointsTTV.getTikTok().isAccountConnected()) {
                return 1;
            } else {
                return 0;
            }
        }));
    }

}