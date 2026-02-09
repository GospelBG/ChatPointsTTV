package me.gosdev.chatpointsttv.Chat;

import me.gosdev.chatpointsttv.Utils.ChatColor;

public class TikTokButtonComponents {
    public static ChatComponent clientStop() {
        ChatComponent comp = new ChatComponent(ChatColor.RED + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.RED + " Stop TikTok Module");
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to stop listening to incoming TikTok events."));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/tiktok stop"));

        return comp;
    }

    public static ChatComponent clientStart() {
        ChatComponent comp = new ChatComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.GREEN + " Start TikTok Module");
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to start the TikTok Module."));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/tiktok start"));

        return comp;
    }

    public static ChatComponent manageAccounts() {
        ChatComponent comp = new ChatComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[✔]" + ChatColor.RESET + ChatColor.YELLOW + " Manage accounts" );
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to add/remove accounts"));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/tiktok accounts"));

        return comp;
    }

    public static ChatComponent accountLink() {
        ChatComponent comp = new ChatComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "\n[+]" + ChatColor.RESET + ChatColor.GREEN + " Link profile");
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to link another account"));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.COMPLETE_COMMAND, "/tiktok link "));

        return comp;
    }

    public static ChatComponent accountUnlink() {
        ChatComponent comp = new ChatComponent(ChatColor.RED + "" + ChatColor.BOLD + "[❌]" + ChatColor.RESET + ChatColor.RED + " Unlink all profiles");
        comp.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to unlink all accounts"));
        comp.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/tiktok unlink"));

        return comp;
    }
}
