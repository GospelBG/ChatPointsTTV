package me.gosdev.chatpointsttv;

import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.ChatComponent;
import me.gosdev.chatpointsttv.Utils.ChatEvent;
import me.gosdev.chatpointsttv.Utils.Translatable;

public class CommandController {
    private static final ChatComponent helpMsg = new ChatComponent(
        Translatable.getString("commands.help.header", "  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " -------------\n") +
        ChatColor.LIGHT_PURPLE + "/cpttv status: " + ChatColor.RESET + Translatable.getString("commands.help.status") + "\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv reload: " + ChatColor.RESET + Translatable.getString("commands.help.reload")  + "\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv set <player> <permission> [true|false|unset]: " + ChatColor.RESET + Translatable.getString("commands.help.set")  + "\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv help: " + ChatColor.RESET + Translatable.getString("commands.help.help"));

    public static void help(GenericSender p) {
        p.sendMessage(helpMsg);

        if (!p.isConsole()) {
            ChatComponent docsTip = new ChatComponent(Translatable.getString("generic.help.tip.get_started", "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, "" + ChatColor.RESET + ChatColor.GRAY).replaceAll("\\[.*", ""));

            ChatComponent link = new ChatComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + Translatable.getString("generic.help.tip.get_started").replaceAll("(?:^|\\])[^\\[]*\\[?", "")); // Get button text
            link.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, "https://gosdev.me/chatpointsttv/installation"));
            link.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("generic.tooltip.open_browser")));
            docsTip.addExtra(link);
            docsTip.addExtra(ChatColor.GRAY + Translatable.getString("generic.help.tip.get_started").replaceAll(".*\\]", ""));

            p.sendMessage(docsTip);
        }
    }

    public static void status(GenericSender p) {
        ChatComponent msg = new ChatComponent(Translatable.getString("commands.status.header", "  ----------  " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " ----------\n"));
        
        ChatComponent updButton = new ChatComponent("\n" + ChatColor.GRAY +  "  → " + ChatColor.GREEN + ChatColor.UNDERLINE + Translatable.getString("commands.status.update_available"));
        updButton.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("generic.tooltip.open_browser")));
        updButton.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, VersionCheck.download_url));

        msg.addExtra(ChatColor.LIGHT_PURPLE + Translatable.getString("commands.status.plugin_version") + " " + ChatColor.WHITE + ChatPointsTTV.getInstance().version);
        if (!VersionCheck.runningLatest) msg.addExtra(updButton);

        ChatComponent twitchStatus = new ChatComponent(ChatColor.LIGHT_PURPLE + "\n\n" + Translatable.getString("generic.twitch_module") + " " + ChatColor.RESET);
        if (ChatPointsTTV.getTwitch() != null && ChatPointsTTV.getTwitch().isStarted()) {
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                twitchStatus.addExtra("" + ChatColor.GREEN + ChatColor.BOLD + Translatable.getString("commands.status.linked"));
            } else {
                twitchStatus.addExtra("" + ChatColor.YELLOW + ChatColor.BOLD + Translatable.getString("commands.status.enabled"));
            }
        } else {
            twitchStatus.addExtra(""  + ChatColor.RED + ChatColor.BOLD + Translatable.getString("commands.status.disabled"));
        }

        ChatComponent tiktokStatus = new ChatComponent(ChatColor.LIGHT_PURPLE + "\n" + Translatable.getString("generic.twitch_module") +" " + ChatColor.RESET);
        if (ChatPointsTTV.getTikTok() != null && ChatPointsTTV.getTikTok().isStarted()) {
            if (ChatPointsTTV.getTikTok().isAccountConnected()) {
                tiktokStatus.addExtra("" + ChatColor.GREEN + ChatColor.BOLD + Translatable.getString("commands.status.linked"));
            } else {
                tiktokStatus.addExtra("" + ChatColor.YELLOW + ChatColor.BOLD + Translatable.getString("commands.status.enabled"));
            }
        } else {
            tiktokStatus.addExtra(""  + ChatColor.RED + ChatColor.BOLD + Translatable.getString("commands.status.disabled"));
        }

        msg.addExtra(twitchStatus);
        msg.addExtra(tiktokStatus);

        ChatComponent reloadBtn = new ChatComponent(Translatable.getString("button.reload.text", ChatColor.GREEN + "" + ChatColor.BOLD + "[⟳]" + ChatColor.RESET + ChatColor.GREEN));
        reloadBtn.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("button.reload.tooltip")));
        reloadBtn.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/cpttv reload"));

        ChatComponent docsBtn = new ChatComponent(Translatable.getString("button.documentation.text", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[📖]" + ChatColor.RESET + ChatColor.LIGHT_PURPLE));
        docsBtn.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("button.documentation.tooltip")));
        docsBtn.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, "https://gosdev.me/chatpointsttv/config/"));

        msg.addExtra("\n\n");
        msg.addExtra(reloadBtn);
        msg.addExtra(ChatColor.GRAY + " - ");
        msg.addExtra(docsBtn);

        p.sendMessage(msg);
    }

    public static void reload(GenericSender p) {
        ChatPointsTTV.getInstance().reload(p);
    }
}
