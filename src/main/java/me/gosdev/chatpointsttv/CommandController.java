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
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;

public class CommandController implements TabExecutor {
    private final BaseComponent helpMsg = new ComponentBuilder("  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ChatPointsTTV Help" + ChatColor.RESET + " -------------\n" + 
        ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("cpttv").getUsage() + ChatColor.RESET + "\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv status: " + ChatColor.RESET + "Displays information about the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv reload: " + ChatColor.RESET + "Restarts the plugin along with all modules and reloads configuration files.\n" +
        ChatColor.LIGHT_PURPLE + "/cpttv set <player> <permission> [true|false|unset]: " + ChatColor.RESET + "Sets a ChatPointsTTV permission.\n" +
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
                    plugin.reload(sender);
                    return true;

                case "set":
                    setPermission(sender, args);
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
        BaseComponent msg = new TextComponent("  ----------  " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "ChatPointsTTV Status" + ChatColor.RESET + " ----------\n");
        
        TextComponent updButton = new TextComponent("\n" + ChatColor.GRAY +  "  → " + ChatColor.GREEN + ChatColor.UNDERLINE + "Update Available!");
        updButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create())); 
        updButton.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VersionCheck.download_url));


        msg.addExtra(ChatColor.LIGHT_PURPLE + "Plugin Version: " + ChatColor.WHITE + ChatPointsTTV.getPlugin().getDescription().getVersion());
        if (!VersionCheck.runningLatest) msg.addExtra(updButton);

        TextComponent twitchStatus = new TextComponent(ChatColor.LIGHT_PURPLE + "\n\nTwitch Module: " + ChatColor.RESET);
        if (ChatPointsTTV.getTwitch().isStarted()) {
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                twitchStatus.addExtra("" + ChatColor.GREEN + ChatColor.BOLD + "LINKED");
            } else {
                twitchStatus.addExtra("" + ChatColor.YELLOW + ChatColor.BOLD + "ENABLED");
            }
        } else {
            twitchStatus.addExtra(""  + ChatColor.RED + ChatColor.BOLD + "DISABLED");
        }

        TextComponent tiktokStatus = new TextComponent(ChatColor.LIGHT_PURPLE + "\nTikTok Module: " + ChatColor.RESET);
        if (ChatPointsTTV.getTikTok().isStarted()) {
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

    private void setPermission(CommandSender sender, String[] args) {
        if (args.length < 3) {
            help(sender);
            return;
        }

        String targetName = args[1];
        String perm = args[2];
        Boolean state = true;

        if (args.length == 4) {
            if (args[3].equalsIgnoreCase("unset")) state = null;
            else if (args[3].equalsIgnoreCase("true")) state = true;
            else if (args[3].equalsIgnoreCase("false")) state = false;
            else {
                sender.sendMessage(ChatColor.RED + "Invalid state: " + args[3]);
                return;
            }
        }

        try {
            ChatPointsTTV.permissions.valueOf(perm.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid permission: " + perm);
            return;
        }

        if (targetName.equals("@s")) {
            if (sender.equals(Bukkit.getConsoleSender())) {
                sender.sendMessage(ChatColor.RED + "No entity was found.");
                return;
            }
            targetName = sender.getName();

        }

        if (targetName.equals("@a")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                setAttachment(p, "chatpointsttv." + perm, state);
            }
        } else {
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Couldn't find player " + targetName + ".");
                return;
            }
            setAttachment(target, "chatpointsttv." + perm, state);
        }

        sender.sendMessage(ChatColor.GREEN + "Permission has been set successfully!");
    }

    private void setAttachment(Player p, String key, Boolean state) {
        if (state != null) {
            PermissionAttachment attachment = p.addAttachment(ChatPointsTTV.getPlugin(), key, state);
            p.setMetadata(key, new FixedMetadataValue(ChatPointsTTV.getPlugin(), attachment));
        } else {
            PermissionAttachment attachment = (PermissionAttachment) p.getMetadata(key).get(0).value();

            if (attachment != null) {
                p.removeAttachment(attachment);
                p.removeMetadata(key, ChatPointsTTV.getPlugin());
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
            available.add("set");
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length == 2) {
                available.add("@s");
                available.add("@a");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    available.add(p.getName());
                }
            } else if (args.length == 3) {
                for (ChatPointsTTV.permissions p : ChatPointsTTV.permissions.values()) {
                    available.add(p.permission_id.replace("chatpointsttv.", ""));
                }
            } else if (args.length == 4) {
                available.add("true");
                available.add("false");
                available.add("unset");
            }
        }

        for (String s : available) {
            if (s.startsWith(args[args.length - 1])) {
                result.add(s);
            }
        }

        return result;
    }
}
