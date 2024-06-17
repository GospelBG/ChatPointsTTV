package com.gospelbg.chatpointsttv;

import com.gospelbg.chatpointsttv.TwitchAuth.AuthenticationCallbackServer;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;

public class CommandController implements TabExecutor {

    public static AuthenticationCallbackServer server = new AuthenticationCallbackServer(3000);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ChatPointsTTV plugin = ChatPointsTTV.getPlugin();

        if (args.length == 0) {
            help(plugin, sender, cmd);
            return true;

        } else {
            switch (args[0]) {
                case "link":
                    if (ChatPointsTTV.getClient() != null) ChatPointsTTV.getClient().close();
                    link(plugin, sender);
                    break;

                case "reload":
                    reload(plugin);
                    break;

                case "help":
                    help(plugin, sender, cmd);
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command: /twitch " + args[0] + "\n ");
                    help(plugin, sender, cmd);
                    return true;
            }
        }

        // If the sender (or console) uses our command correct, we can return true
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 1) {
            list.add("link");
            list.add("reload");
            list.add("help");

            return list;
        }
        return null;        
    }

    private void link(ChatPointsTTV plugin, CommandSender p) {
        server = new AuthenticationCallbackServer(3000);
        if (ChatPointsTTV.getTwitchClient() != null) {
            ChatPointsTTV.getTwitchClient().close();
        }

        if (p == Bukkit.getServer().getConsoleSender()) {
            String msg = "Link your Twitch account to set ChatPointsTTV up. Open this link in your browser to login:\n" + plugin.getAuthURL();
            p.sendMessage(msg);
        } else {
            String msg = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "--------------- ChatPointsTTV ---------------\n" + ChatColor.RESET + ChatColor.WHITE + "Link your Twitch account to set ChatPointsTTV up";
            ComponentBuilder formatted = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "[Click here to login with Twitch]");
            
            BaseComponent btn = formatted.create()[0];

            btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));
            btn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getAuthURL()));

            ChatPointsTTV.getUtils().sendMessage(p, new BaseComponent[]{new ComponentBuilder(msg + "\n").create()[0], btn});
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                
                try {
                    server.start();
                    
                    if(server.getAccessToken() != null) {
                        plugin.linkToTwitch(server.getAccessToken());
                        server.stop();
                    }
                } catch(IOException e) {
                    plugin.log.warning(e.toString());
                }
            }
        });
        
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            public void run() {
                server.stop();
            }
        }, 6000L);// 60 L == 3 sec, 20 ticks == 1 sec
    }

    private void reload(ChatPointsTTV plugin) {
        if (server != null) server.stop(); // Stop HTTP server if it is actve

        plugin.reloadConfig();
        plugin.onDisable();
        plugin.onEnable();
    }

    private void help(ChatPointsTTV plugin, CommandSender p, Command cmd) {
        String msg = (
        "---------- " + ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "ChatPointsTTV help" + ChatColor.RESET + " ----------\n" + 
        ChatColor.GRAY + "Usage: " + cmd.getUsage() + ChatColor.RESET + "\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch link: " + ChatColor.RESET + "Use this command to link your Twitch account and enable the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files. You will need to link again your Twitch account.\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch help: " + ChatColor.RESET + "Displays this help message."
        );
        ComponentBuilder formatted = new ComponentBuilder(msg);
        
        ChatPointsTTV.getUtils().sendMessage(p, new BaseComponent[]{formatted.create()[0]});
    }
}
