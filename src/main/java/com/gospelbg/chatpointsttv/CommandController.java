package com.gospelbg.chatpointsttv;

import com.gospelbg.chatpointsttv.TwitchAuth.AuthenticationCallbackServer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
            
            switch(cmd.getName()) {
                case "link":
                    sendLinkMessage(player);                    
            }
        }

        // If the player (or console) uses our command correct, we can return true
        return true;
    }

    public void sendLinkMessage(Player player) {
        String msg = ChatColor.BOLD + "Link your Twitch account to setup ChatPointsTTV";
        TextComponent btn = new TextComponent(ChatColor.GOLD + msg + "\n" + ChatColor.RESET + "[Click here to login with Twitch]");
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));
        btn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Main.plugin.getAuthURL()));

        player.spigot().sendMessage(btn);

        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();

                    Main.plugin.linkToTwitch(server.getAccessToken());
                } catch(IOException e) {
                    Main.plugin.log.warning(e.toString());
                }
            }
        });
    }
}
