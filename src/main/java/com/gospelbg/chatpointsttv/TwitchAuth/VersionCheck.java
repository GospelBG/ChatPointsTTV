package com.gospelbg.chatpointsttv.TwitchAuth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.json.JSONArray;

import com.gospelbg.chatpointsttv.ChatPointsTTV;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class VersionCheck {
    private final static String url = "https://api.modrinth.com/v2/project/nN0gRvoO/version";
    private final static String download_url = "https://modrinth.com/plugin/chatpointsttv";

    public static void check() {
        ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
        Logger log = plugin.log;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray json = new JSONArray(response.body());
            String latest = json.getJSONObject(0).getString("version_number");

            if (ChatPointsTTV.getPlugin().getDescription().getVersion() != latest) {
                for (Player p: plugin.getServer().getOnlinePlayers()) {
                    if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                        p.sendMessage(ChatColor.YELLOW + "ChatPointsTTV v" + latest + " has been released!");

                        ComponentBuilder formatted = new ComponentBuilder(ChatColor.YELLOW + "Click " + ChatColor.UNDERLINE + "here" + ChatColor.RESET + ChatColor.YELLOW + " to download the latest version");
            
                        BaseComponent btn = formatted.create()[0];
                        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to open in browser")));
                        btn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, download_url));

                        p.spigot().sendMessage(btn);
                    }
                }
                log.info("ChatPointsTTV v" + latest + " has been released! Download the latest version in " + download_url);

            }

        } catch (IOException | InterruptedException e) {
            log.warning("Couldn't fetch latest version." + e.toString());
        }
    }
}
