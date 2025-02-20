package me.gosdev.chatpointsttv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.json.JSONArray;

import me.gosdev.chatpointsttv.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class VersionCheck {
    private final static String url = "https://api.modrinth.com/v2/project/nN0gRvoO/version";
    private final static String download_url = "https://modrinth.com/plugin/chatpointsttv";

    public static void check() {
        ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
        Logger log = plugin.log;

        try {
            StringBuilder result = new StringBuilder();
            HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            conn.disconnect();

            JSONArray json = new JSONArray(result.toString());
            String latest = json.getJSONObject(0).getString("version_number");
            Utils utils = ChatPointsTTV.getUtils();

            if (!ChatPointsTTV.getPlugin().getDescription().getVersion().equals(latest.replaceAll("[^\\d.]", ""))) {
                for (Player p: plugin.getServer().getOnlinePlayers()) {
                    if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                        ComponentBuilder formatted = new ComponentBuilder(ChatColor.YELLOW + "Click " + ChatColor.UNDERLINE + "here" + ChatColor.RESET + ChatColor.YELLOW + " to download the latest version");
            
                        BaseComponent btn = formatted.create()[0];
                        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create())); 
                        btn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, download_url));

                        utils.sendMessage(p, new TextComponent(ChatColor.YELLOW + "ChatPointsTTV v" + latest + " has been released!"));
                        utils.sendMessage(p, btn);
                    }
                }
                log.info("ChatPointsTTV v" + latest + " has been released! Download the latest version in " + download_url);

            }

        } catch (IOException | URISyntaxException e) {
            log.warning("Couldn't fetch latest version." + e.toString());
        }
    }
}
