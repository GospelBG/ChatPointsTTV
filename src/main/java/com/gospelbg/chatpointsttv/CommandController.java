package com.gospelbg.chatpointsttv;

import com.gospelbg.chatpointsttv.TwitchAuth.AuthenticationCallbackServer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

public class CommandController implements CommandExecutor {
    private AuthenticationCallbackServer server = new AuthenticationCallbackServer(3000);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ChatPointsTTV plugin = ChatPointsTTV.getPlugin();

            try {
                ChatPointsTTV.getTwitchClient().close();
            } catch (Exception e) {}


            String msg = ChatColor.BOLD + "Link your Twitch account to setup ChatPointsTTV\n[Click here to login with Twitch]";
            ComponentBuilder formatted = new ComponentBuilder(msg);
            
            BaseComponent btn = formatted.create()[0];
    
            btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));
            btn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getAuthURL()));
    
            player.spigot().sendMessage(btn);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    
                    try {
                        server.start();
                        
                        if(server.getAccessToken() != null) plugin.linkToTwitch(server.getAccessToken());
                    } catch(IOException e) {
                        plugin.log.warning(e.toString());
                    }
                }
            });
            
           Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
                public void run() {
                    plugin.log.info("Stopping HTTP Server");
                    server.stop();
                }
              }, 6000L);// 60 L == 3 sec, 20 ticks == 1 sec
            
        }

        // If the player (or console) uses our command correct, we can return true
        return true;
    }
}
