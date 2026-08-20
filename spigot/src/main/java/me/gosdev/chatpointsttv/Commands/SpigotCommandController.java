package me.gosdev.chatpointsttv.Commands;

import java.util.ArrayList;
import java.util.List;

import me.gosdev.chatpointsttv.CommandController;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;

import me.gosdev.chatpointsttv.ChatPointsTTVSpigot;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Spigot.SpigotSender;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Utils.ChatColor;

public class SpigotCommandController implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        SpigotSender genericSender = new SpigotSender(sender);

        if (ChatPointsTTV.getInstance().isReloading()) {
            sender.sendMessage(ChatPointsTTV.msgPrefix + ChatColor.RED + " The plugin is currently reloading. Please wait a moment.");
            return true;
        } else if (args.length == 0) {
            CommandController.help(genericSender);
            return true;
        } else {
            switch (args[0]) {
                case "status":
                    CommandController.status(genericSender);
                    return true;

                case "reload":
                    ChatPointsTTV.getInstance().reload(genericSender);
                    return true;

                case "help":
                    CommandController.help(genericSender);
                    return true;

                case "set":
                    setPermission(genericSender, args);
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command: /cpttv " + args[0]);
                    CommandController.help(genericSender);
                    return true;
            }
        }
    }

    private void setPermission(GenericSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /cpttv set <player> <permission> [true|false|unset]");
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
            if (sender.isConsole()) {
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
            PermissionAttachment attachment = p.addAttachment(ChatPointsTTVSpigot.getPlugin(), key, state);
            p.setMetadata(key, new FixedMetadataValue(ChatPointsTTVSpigot.getPlugin(), attachment));
        } else {
            if (p.hasMetadata(key)) {
                PermissionAttachment attachment = (PermissionAttachment) p.getMetadata(key).get(0).value();
                if (attachment != null) {
                    p.removeAttachment(attachment);
                    p.removeMetadata(key, ChatPointsTTVSpigot.getPlugin());
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
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
