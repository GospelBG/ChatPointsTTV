package me.gosdev.chatpointsttv.Utils;

import java.util.Map;
import java.util.Set;
import java.awt.Color;

import org.bukkit.Bukkit;

import com.google.common.collect.ImmutableMap;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.ChatColor;

public class ColorUtils {
    // Mapping of Spigot ChatColor to appropriate Java Color
    private static final Map<ChatColor, Color> COLOR_MAPPINGS = ImmutableMap.<ChatColor, Color>builder()
        .put(ChatColor.BLACK, new Color(0, 0, 0))
        .put(ChatColor.DARK_BLUE, new Color(0, 0, 170))
        .put(ChatColor.DARK_GREEN, new Color(0, 170, 0))
        .put(ChatColor.DARK_AQUA, new Color(0, 170, 170))
        .put(ChatColor.DARK_RED, new Color(170, 0, 0))
        .put(ChatColor.DARK_PURPLE, new Color(170, 0, 170))
        .put(ChatColor.GOLD, new Color(255, 170, 0))
        .put(ChatColor.GRAY, new Color(170, 170, 170))
        .put(ChatColor.DARK_GRAY, new Color(85, 85, 85))
        .put(ChatColor.BLUE, new Color(85, 85, 255))
        .put(ChatColor.GREEN, new Color(85, 255, 85))
        .put(ChatColor.AQUA, new Color(85, 255, 255))
        .put(ChatColor.RED, new Color(255, 85, 85))
        .put(ChatColor.LIGHT_PURPLE, new Color(255, 85, 255))
        .put(ChatColor.YELLOW, new Color(255, 255, 85))
        .put(ChatColor.WHITE, new Color(255, 255, 255))
        .build();

    public static String rgbToHex(int rgb) {
        return Integer.toHexString(rgb).substring(2);
    }

    public static int hexToRgb(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1); // Remove # from color code
        return Integer.valueOf(hex, 16);
    }

    public static void test2() {
        Bukkit.getServer().getConsoleSender().sendMessage("AFTER");
        ChatPointsTTV.getPlugin().log.info("Hi!");
    }

    public static int getRgb(int red, int green, int blue) {
        return new Color(red, green, blue).getRGB();
    }

    public static String formattedRgb(Color color) {
        return "(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")";
    }

    public static ChatColor getClosestChatColor(Color color) {
        ChatColor closest = null;
        int mark = 0;
        for (Map.Entry<ChatColor, Color> entry : COLOR_MAPPINGS.entrySet()) {
            ChatColor key = entry.getKey();
            Color value = entry.getValue();

            int diff = getDiff(value, color);
            if (closest == null || diff < mark) {
                closest = key;
                mark = diff;
            }
        }
        
        return closest;
    }

    // Algorithm to determine the difference between two colors, source:
    // https://stackoverflow.com/questions/27374550/how-to-compare-color-object-and-get-closest-color-in-an-color
    private static int getDiff(Color color, Color compare) {
        int a = color.getAlpha() - compare.getAlpha(),
                r = color.getRed() - compare.getRed(),
                g = color.getGreen() - compare.getGreen(),
                b = color.getBlue() - compare.getBlue();
        return a * a + r * r + g * g + b * b;
    }
}

