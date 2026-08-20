package me.gosdev.chatpointsttv.Twitch;

import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.ChatComponent;
import me.gosdev.chatpointsttv.Utils.ChatEvent;

public class TwitchButtonComponents {
    public static ChatComponent clientStop() {
        ChatComponent comp = new ChatComponent(ChatColor.RED + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.RED + " Stop Twitch Module");
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to stop listening to incoming Twitch events."));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch stop"));

        return comp;
    }

    public static ChatComponent clientStart() {
        ChatComponent comp = new ChatComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.GREEN + " Start Twitch Module");
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to start the Twitch Module."));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch start"));

        return comp;
    }

    public static ChatComponent manageAccounts() {
        ChatComponent comp = new ChatComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[✔]" + ChatColor.RESET + ChatColor.YELLOW + " Manage accounts" );
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to add/remove accounts"));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch accounts"));

        return comp;
    }

    public static ChatComponent accountLink() {
        ChatComponent comp = new ChatComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "\n[+]" + ChatColor.RESET + ChatColor.GREEN + " Add account");
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to link another account"));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch link"));

        return comp;
    }

    public static ChatComponent accountUnlink() {
        ChatComponent comp = new ChatComponent(ChatColor.RED + "" + ChatColor.BOLD + "[❌]" + ChatColor.RESET + ChatColor.RED + " Remove all accounts");
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to unlink all accounts"));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch unlink"));

        return comp;
    }
}
