package me.gosdev.chatpointsttv;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.gosdev.chatpointsttv.TikTok.TikTokCommandController;
import me.gosdev.chatpointsttv.Twitch.TwitchCommandController;
import me.gosdev.chatpointsttv.Utils.FollowerLog;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SpigotPlugin extends JavaPlugin {
    private final AtomicBoolean isReloading = new AtomicBoolean(false);
    private static SpigotPlugin plugin;
    private CommandController cmdController;
    private TwitchCommandController twitchCmdController;
    private TikTokCommandController tikTokCmdController;
    private boolean firstRun = false;

    public SpigotConfigFile config;
    public Metrics metrics;

    public static SpigotPlugin getPlugin() {
        return plugin;
    }

    public Boolean isReloading() {
        return isReloading.get();
    }

    @Override
    public void onEnable() {
        ChatPointsTTV.log = new SpigotLog();
        plugin = this;
        PluginManager pm = Bukkit.getServer().getPluginManager();

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
        
        if (!plugin.getDataFolder().exists()) firstRun = true;

        this.saveDefaultConfig();
        reloadConfig();
        config = new SpigotConfigFile("config.yml");
        
        SpigotConfigFile accountsFile = new SpigotConfigFile("accounts");
        SpigotConfigFile twitchCfg = new SpigotConfigFile("twitch.yml");
        SpigotConfigFile tiktokCfg = new SpigotConfigFile("tiktok.yml");

        SpigotConfigFile followerLog = new SpigotConfigFile("followers");
        FollowerLog.setAccountsFile(followerLog);

        ChatPointsTTV.enable(new SpigotLoader(), accountsFile, config, twitchCfg, tiktokCfg);

        File stringsFile = new File(plugin.getDataFolder(), "locales.yml");
        if (!stringsFile.exists()) {
            plugin.saveResource("locales.yml", false);
        }
        
        FileConfiguration stringsYaml = YamlConfiguration.loadConfiguration(stringsFile);
        FileConfiguration defaultStrings = YamlConfiguration.loadConfiguration(plugin.getTextResource("locales.yml"));
        ChatPointsTTV.locales = new HashMap<>();
        for (String key : defaultStrings.getKeys(true)) {
            if (stringsYaml.isString(key)) {
                ChatPointsTTV.locales.put(key, stringsYaml.getString(key));
            } else {
                ChatPointsTTV.locales.put(key, defaultStrings.getString(key));
            }
        }

        if (cmdController == null) {
            cmdController = new CommandController();
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
            Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.PREFIX + "Configuration files have just been created. You need to set up ChatPointsTTV before using it.\nCheck out the quick start guide at https://gosdev.me/chatpointsttv/install");
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
        ChatPointsTTV.disable();
        // Erase variables
        config = null;
        plugin = null;

        HandlerList.unregisterAll(this);
    }

    public void reload(CommandSender p) {
        if (!isReloading.compareAndSet(false, true)) {
            p.sendMessage(ChatColor.RED + "ChatPointsTTV is already reloading!");
            return;
        }

        if (!p.equals(Bukkit.getConsoleSender())) p.sendMessage(ChatPointsTTV.PREFIX + "Reloading ChatPointsTTV...");
        ChatPointsTTV.log.info("Reloading ChatPointsTTV...");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            onDisable();
            try {
                Bukkit.getScheduler().callSyncMethod(this, () -> { // Need to run on main thread, due to Bukkit API usage
                    onEnable();
                    return null;
                }).get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                p.sendMessage(ChatColor.RED + "There was an error reloading ChatPointsTTV. Please check the server console.");
                e.printStackTrace();
            }
            
            isReloading.set(false);
        });
    }

}