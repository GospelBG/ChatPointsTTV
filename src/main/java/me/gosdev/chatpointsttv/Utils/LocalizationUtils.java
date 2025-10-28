package me.gosdev.chatpointsttv.Utils;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;
import net.md_5.bungee.api.ChatColor;

public class LocalizationUtils {
    public static String replacePlaceholders(String in, String username, String channel, String event, Platforms platform) {
        String out = in;

        ChatColor userColor;
        ChatColor eventColor;

        switch (platform) {
            case TWITCH:
                userColor = ChatPointsTTV.getTwitch().userColor;
                eventColor = ChatPointsTTV.getTwitch().eventColor;
                break;

            default:
            userColor = ChatPointsTTV.userColor;
            eventColor = ChatPointsTTV.eventColor;
        }
        
        if (username != null) {
            out = out.replace("{USERNAME}", userColor + username + ChatColor.RESET);
        }
        if (channel != null) {
            out = out.replace("{CHANNEL}", channel);
        }
        if (event != null) {
            out = out.replace("{EVENT}", eventColor + event + ChatColor.RESET);
        }

        return out;
    }
}
