package me.gosdev.chatpointsttv.Twitch;

import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.ChatComponent;
import me.gosdev.chatpointsttv.Utils.ChatEvent;
import me.gosdev.chatpointsttv.Utils.Translatable;

public class TwitchButtonComponents {
    public static ChatComponent clientStop() {
        ChatComponent comp = new ChatComponent(Translatable.getString("button.stop.text", ChatColor.RED + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.RED, "Twitch"));
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("button.stop.tooltip", "Twitch")));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch stop"));

        return comp;
    }

    public static ChatComponent clientStart() {
        ChatComponent comp = new ChatComponent(Translatable.getString("button.start.text", ChatColor.GREEN + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.GREEN, "Twitch"));
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("button.start.tooltip", "Twitch")));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch start"));

        return comp;
    }

    public static ChatComponent manageAccounts() {
        ChatComponent comp = new ChatComponent(Translatable.getString("button.manage.text", ChatColor.YELLOW + "" + ChatColor.BOLD + "[✔]" + ChatColor.RESET + ChatColor.YELLOW));
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("button.manage.tooltip")));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch accounts"));

        return comp;
    }

    public static ChatComponent accountLink() {
        ChatComponent comp = new ChatComponent(Translatable.getString("button.link.text", ChatColor.GREEN + "" + ChatColor.BOLD + "\n[+]" + ChatColor.RESET + ChatColor.GREEN));
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("button.link.tooltip")));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch link "));

        return comp;
    }

    public static ChatComponent accountUnlink() {
        ChatComponent comp = new ChatComponent(Translatable.getString("button.unlink.text", ChatColor.RED + "" + ChatColor.BOLD + "[❌]" + ChatColor.RESET + ChatColor.RED));
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("button.unlink.tooltip")));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch unlink"));

        return comp;
    }
}
