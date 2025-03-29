package me.gosdev.chatpointsttv.Utils;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.ChatColor;

public class LocalizationUtils {
    public static String replacePlaceholders(String in, String username, String channel, String event) {
        String out = in;

        if (username != null) {
            out = out.replace("{USERNAME}", ChatPointsTTV.user_color + username + ChatColor.RESET);
        }
        if (channel != null) {
            out = out.replace("{CHANNEL}", channel);
        }
        if (event != null) {
            out = out.replace("{EVENT}", ChatPointsTTV.event_color + event + ChatColor.RESET);
        }

        return out;
    }
}
