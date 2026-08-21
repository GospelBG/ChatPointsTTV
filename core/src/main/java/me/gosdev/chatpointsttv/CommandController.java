package me.gosdev.chatpointsttv;

import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.ChatComponent;
import me.gosdev.chatpointsttv.Utils.ChatEvent;

public class CommandController {
    private static final ChatComponent helpMsg = new ChatComponent(
        "  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ChatPointsTTV Help" + ChatColor.RESET + " -------------\n" +
        ChatColor.GRAY + "Usage: /cpttv <function>\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv status: " + ChatColor.RESET + "Displays information about the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv reload: " + ChatColor.RESET + "Restarts the plugin along with all modules and reloads configuration files.\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv set <player> <permission> [true|false|unset]: " + ChatColor.RESET + "Sets a ChatPointsTTV permission.\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv help: " + ChatColor.RESET + "Displays this help message.");

    public static void help(GenericSender p) {
        p.sendMessage(helpMsg);

        if (!p.isConsole()) {
            ChatComponent docsTip = new ChatComponent("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "\nTip: " + ChatColor.RESET + ChatColor.GRAY + "Get started easily by taking a look at the ");

            ChatComponent link = new ChatComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + "installation guide.");
            link.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, "https://gosdev.me/chatpointsttv/install"));
            link.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to open in browser"));
            docsTip.addExtra(link);
            
            p.sendMessage(docsTip);
        }
    }

    public static void status(GenericSender p) {
        ChatComponent msg = new ChatComponent("  ----------  " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "ChatPointsTTV Status" + ChatColor.RESET + " ----------\n");
        
        ChatComponent updButton = new ChatComponent("\n" + ChatColor.GRAY +  "  → " + ChatColor.GREEN + ChatColor.UNDERLINE + "Update Available!");
        updButton.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to open in browser")); 
        updButton.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, VersionCheck.download_url));

        msg.addExtra(ChatColor.LIGHT_PURPLE + "Plugin Version: " + ChatColor.WHITE + ChatPointsTTV.getInstance().version);
        if (!VersionCheck.runningLatest) msg.addExtra(updButton);

        ChatComponent twitchStatus = new ChatComponent(ChatColor.LIGHT_PURPLE + "\n\nTwitch Module: " + ChatColor.RESET);
        if (ChatPointsTTV.getTwitch() != null && ChatPointsTTV.getTwitch().isStarted()) {
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                twitchStatus.addExtra("" + ChatColor.GREEN + ChatColor.BOLD + "LINKED");
            } else {
                twitchStatus.addExtra("" + ChatColor.YELLOW + ChatColor.BOLD + "ENABLED");
            }
        } else {
            twitchStatus.addExtra(""  + ChatColor.RED + ChatColor.BOLD + "DISABLED");
        }

        ChatComponent tiktokStatus = new ChatComponent(ChatColor.LIGHT_PURPLE + "\nTikTok Module: " + ChatColor.RESET);
        if (ChatPointsTTV.getTikTok() != null && ChatPointsTTV.getTikTok().isStarted()) {
            if (ChatPointsTTV.getTikTok().isAccountConnected()) {
                tiktokStatus.addExtra("" + ChatColor.GREEN + ChatColor.BOLD + "LINKED");
            } else {
                tiktokStatus.addExtra("" + ChatColor.YELLOW + ChatColor.BOLD + "ENABLED");
            }
        } else {
            tiktokStatus.addExtra(""  + ChatColor.RED + ChatColor.BOLD + "DISABLED");
        }

        msg.addExtra(twitchStatus);
        msg.addExtra(tiktokStatus);

        ChatComponent reloadBtn = new ChatComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[⟳]" + ChatColor.RESET + ChatColor.GREEN + " Reload ChatPointsTTV");
        reloadBtn.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to reload ChatPointsTTV."));
        reloadBtn.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/cpttv reload"));

        ChatComponent docsBtn = new ChatComponent(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[📖]" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " Open Documentation");
        docsBtn.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to open ChatPointsTTV's documentation in your web browser."));
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
