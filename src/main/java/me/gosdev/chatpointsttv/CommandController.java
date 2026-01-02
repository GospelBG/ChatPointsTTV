package me.gosdev.chatpointsttv;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandController implements TabExecutor {
    private final BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV Help" + ChatColor.RESET + " ----------\n" + 
        ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("cpttv").getUsage() + ChatColor.RESET + "\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv status: " + ChatColor.RESET + "Displays information about the plugin and the cpttv client.\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files.\n" + 
        ChatColor.LIGHT_PURPLE + "/cpttv help: " + ChatColor.RESET + "Displays this help message.").create()[0];

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
        if (plugin.isReloading()) {
            sender.sendMessage(ChatPointsTTV.msgPrefix + ChatColor.RED + " The plugin is currently reloading. Please wait a moment.");
            return true;
        } else if (args.length == 0) {
            help(sender);
            return true;
        } else {
            switch (args[0]) {
                case "status":
                    status(sender);
                    return true;

                case "reload":
                    if (plugin.isReloading()) {
                        sender.sendMessage(ChatColor.RED + "ChatPointsTTV is already reloading!");
                        return true;
                    }
                    plugin.reload(sender);
                    return true;

                case "help":
                    help(sender);
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command: /cpttv " + args[0]);
                    help(sender);
                    return true;
                    
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> available = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();

        if (args.length == 1) {
            available.add("help");
            available.add("reload");
            available.add("status");
        }

        for (String s : available) {
            if (s.startsWith(args[args.length - 1])) {
                result.add(s);
            }
        }

        return result;
    }

    private void help(CommandSender p) {
        p.spigot().sendMessage(helpMsg);

        if (!p.equals(Bukkit.getConsoleSender())){
            TextComponent docsTip = new TextComponent("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "\nTip: " + ChatColor.RESET + ChatColor.GRAY + "Get started easily by taking a look at the ");

            TextComponent link = new TextComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + "installation guide.");
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gosdev.me/chatpointsttv/install"));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));
            docsTip.addExtra(link);
            
            p.spigot().sendMessage(docsTip);
        }
    }

    private void status(CommandSender p) {
        BaseComponent msg = new TextComponent("---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "ChatPointsTTV Status" + ChatColor.RESET + " ----------\n");
        
        TextComponent updButton = new TextComponent("\n" + ChatColor.GRAY +  "  → " + ChatColor.GREEN + ChatColor.UNDERLINE + "Update Available!");
        updButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create())); 
        updButton.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VersionCheck.download_url));


        msg.addExtra(ChatColor.LIGHT_PURPLE + "Plugin Version: " + ChatColor.WHITE + ChatPointsTTV.getPlugin().getDescription().getVersion());
        if (!VersionCheck.runningLatest) msg.addExtra(updButton);

        TextComponent twitchStatus = new TextComponent(ChatColor.LIGHT_PURPLE + "\n\nTwitch Client: " + ChatColor.RESET);
        if (ChatPointsTTV.getTwitch().isStarted()) {
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                twitchStatus.addExtra("" + ChatColor.GREEN + ChatColor.BOLD + "LINKED");
            } else {
                twitchStatus.addExtra("" + ChatColor.YELLOW + ChatColor.BOLD + "ENABLED");
            }
        } else {
            twitchStatus.addExtra(""  + ChatColor.RED + ChatColor.BOLD + "DISABLED");
        }

        TextComponent tiktokStatus = new TextComponent(ChatColor.LIGHT_PURPLE + "\nTikTok Client: " + ChatColor.RESET);
        if (ChatPointsTTV.getTikTok().started) {
            if (ChatPointsTTV.getTikTok().accountConnected) {
                tiktokStatus.addExtra("" + ChatColor.GREEN + ChatColor.BOLD + "LINKED");
            } else {
                tiktokStatus.addExtra("" + ChatColor.YELLOW + ChatColor.BOLD + "ENABLED");
            }
        } else {
            tiktokStatus.addExtra(""  + ChatColor.RED + ChatColor.BOLD + "DISABLED");
        }


        msg.addExtra(twitchStatus);
        msg.addExtra(tiktokStatus);

        TextComponent reloadBtn = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[⟳]" + ChatColor.RESET + ChatColor.GREEN + " Reload ChatPointsTTV");
        reloadBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to reload ChatPointsTTV.").create()));
        reloadBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cpttv reload"));

        TextComponent docsBtn = new TextComponent(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[📖]" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " Open Documentation");
        docsBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ChatPointsTTV's documentation in your web browser.").create()));
        docsBtn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gosdev.me/chatpointsttv/config/"));

        msg.addExtra("\n\n");
        msg.addExtra(reloadBtn);
        msg.addExtra(ChatColor.GRAY + " - ");
        msg.addExtra(docsBtn);

        p.spigot().sendMessage(msg);
    }
}
