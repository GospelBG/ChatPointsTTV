package me.gosdev.chatpointsttv.Utils;

import java.util.ArrayList;

import me.gosdev.chatpointsttv.Events.EventInformation;

public class LocalizationUtils {
    public static String replacePlaceholders(String in, EventInformation event) {
        String out = in;

        if (event.getChatter() != null) {
            out = out.replace("{USERNAME}", event.getChatter() + ChatColor.RESET);
        }
        if (event.getStreamer() != null) {
            out = out.replace("{CHANNEL}", event.getStreamer());
        }
        if (event.getEvent() != null) {
            out = out.replace("{EVENT}", event.getEvent() + ChatColor.RESET);
        }
        if (event.getAmount() != null) {
            out = out.replace("{AMOUNT}", event.getAmount().toString() + ChatColor.RESET);
        }
        if (event.getExtra() != null) {
            out = out.replace("{EXTRA}", event.getExtra() + ChatColor.RESET);
        }

        return out;
    }

    public static String[] parseQuotes(String[] in) {
        ArrayList<String> args =  new ArrayList<>();
        for (int i = 0; i < in.length; i++) {
            String arg = in[i];
            // Check if the argument starts with a quote and does not end with an escaped quote
            if (arg.startsWith("\"") && !arg.endsWith("\\\"")) {
            StringBuilder sb = new StringBuilder(arg.substring(1));
            // Continue appending arguments until the closing quote is found
            while (i + 1 < in.length && !(in[i + 1].endsWith("\"") && !in[i + 1].endsWith("\\\""))) {
                sb.append(" ").append(in[++i]);
            }
            // Append the last part of the quoted argument
            if (i + 1 < in.length) {
                sb.append(" ").append(in[++i], 0, in[i].length() - 1);
            }
            // Add the complete quoted argument to the args list
            args.add(sb.toString().replace("\\\"", "\""));
            } else {
            // Add the argument to the args list, replacing escaped quotes
            args.add(arg.replace("\\\"", "\""));
            }
        }
        return args.toArray(new String[0]);
    }
}
