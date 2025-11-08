package me.gosdev.chatpointsttv.Twitch;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TwitchButtonComponents {
    public static TextComponent clientStop() {
        TextComponent comp = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.RED + " Stop Twitch Client");
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to stop all Twitch events.").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch stop"));

        return comp;
    }

    public static TextComponent clientStart() {
        TextComponent comp = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.GREEN + " Start Twitch Client");
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to start the Twitch client.").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch start"));

        return comp;
    }

    public static TextComponent manageAccounts() {
        TextComponent comp = new TextComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[✔]" + ChatColor.RESET + ChatColor.YELLOW + " Manage accounts" );
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to add/remove accounts").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch accounts"));

        return comp;
    }

    public static TextComponent accountLink() {
        TextComponent comp = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "\n[+]" + ChatColor.RESET + ChatColor.GREEN + " Add account");
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to link another account").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch link"));

        return comp;
    }

    public static TextComponent accountUnlink() {
        TextComponent comp = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[❌]" + ChatColor.RESET + ChatColor.RED + " Remove all accounts");
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink all accounts").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch unlink"));

        return comp;
    }
}
