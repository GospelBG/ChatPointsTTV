package me.gosdev.chatpointsttv.TikTok;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TikTokButtonComponents {
    public static TextComponent clientStop() {
        TextComponent comp = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.RED + " Stop TikTok Client");
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to stop all TikTok events.").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tiktok stop"));

        return comp;
    }

    public static TextComponent clientStart() {
        TextComponent comp = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.GREEN + " Start TikTok Client");
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to start the TikTok client.").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tiktok start"));

        return comp;
    }

    public static TextComponent manageAccounts() {
        TextComponent comp = new TextComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[✔]" + ChatColor.RESET + ChatColor.YELLOW + " Manage accounts" );
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to add/remove accounts").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tiktok accounts"));

        return comp;
    }

    public static TextComponent accountLink() {
        TextComponent comp = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "\n[+]" + ChatColor.RESET + ChatColor.GREEN + " Link profile");
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to link another account").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tiktok link"));

        return comp;
    }

    public static TextComponent accountUnlink() {
        TextComponent comp = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[❌]" + ChatColor.RESET + ChatColor.RED + " Unlink all profiles");
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink all accounts").create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tiktok unlink"));

        return comp;
    }
}
