package me.gosdev.chatpointsttv.Utils;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

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
        return Integer.parseInt(hex, 16);
    }

    public static int getRgb(int red, int green, int blue) {
        return new Color(red, green, blue).getRGB();
    }

    public static String formattedRgb(Color color) {
        return "(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")";
    }

    public static ChatColor getClosestChatColor(Color color) {
        ChatColor closestColor = null;
        double closestDistance = Double.MAX_VALUE;

        for (Map.Entry<ChatColor, Color> entry : COLOR_MAPPINGS.entrySet()) {
            double distance = getColorDistance(color, entry.getValue());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestColor = entry.getKey();
            }
        }

        return closestColor;
    }

    private static double getColorDistance(Color c1, Color c2) {
        int redDiff = c1.getRed() - c2.getRed();
        int greenDiff = c1.getGreen() - c2.getGreen();
        int blueDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }
}
