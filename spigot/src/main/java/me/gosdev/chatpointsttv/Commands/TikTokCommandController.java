package me.gosdev.chatpointsttv.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.gosdev.chatpointsttv.TikTok.TikTokCommands;
import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.Translatable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import me.gosdev.chatpointsttv.Spigot.SpigotSender;
import me.gosdev.chatpointsttv.ChatPointsTTV;

public class TikTokCommandController implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        SpigotSender genericSender = new SpigotSender(sender);

        if (args.length == 0) {
            TikTokCommands.help(genericSender);
            return true;
        }

        switch(args[0]) {
            case "start":
                TikTokCommands.start(genericSender);
                return true;

            case "stop":
                TikTokCommands.stop(genericSender);
                return true;

            case "status":
                TikTokCommands.displayStatus(genericSender);
                return true;

            case "reload":
                TikTokCommands.reload(genericSender);
                return true;

            case "link":
                if (args.length != 2) {
                    genericSender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /tiktok link <username>");
                    return true;
                }
                TikTokCommands.link(genericSender, args[1]);
                return true;

            case "unlink":
                if (args.length < 1 || args.length > 2) {
                    genericSender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /tiktok unlink [username]");
                    return true;
                }
                TikTokCommands.unlink(genericSender, args.length == 2 ? Optional.of(args[1]) : Optional.empty());
                return true;

            case "accounts":
                TikTokCommands.accounts(genericSender);
                return true;

            case "test":
                if (args.length < 4) {
                    genericSender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + ": /tiktok test <type> ...");
                    return true;
                }
                TikTokCommands.test(genericSender, args);
                return true;

            case "help":
                TikTokCommands.help(genericSender);
                return true;

            default:
                genericSender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /tiktok " + args[0]);
                TikTokCommands.help(genericSender);
                return false;
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        ArrayList<String> available = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();

        switch (args.length) {
            case 1:
                available.add("help");
                available.add("reload");
                available.add("status");
                if (ChatPointsTTV.getTikTok().isStarted()) {
                    available.add("stop");
                    available.add("link");
                    available.add("accounts");
                    if (ChatPointsTTV.getTikTok().isAccountConnected()) {
                        available.add("test");
                        available.add("accounts");  
                        available.add("unlink");
                    }
                } else {
                    available.add("start");
                }
                break;
            
            case 2:
                if (ChatPointsTTV.getTikTok().isStarted()) {
                    if (args[0].equalsIgnoreCase("link")) {
                        available.add("<TikTok Username>");
                    } else if (args[0].equalsIgnoreCase("unlink") && ChatPointsTTV.getTikTok().isAccountConnected()) {
                        available.addAll(ChatPointsTTV.getTikTok().getClients().keySet());
                    } else if (args[0].equalsIgnoreCase("test")) {
                        available.add("follow");
                        available.add("like");
                        available.add("gift");
                        available.add("share");
                    }
                }
                break;

            case 3:
                if (ChatPointsTTV.getTikTok().isStarted()) {
                    if (args[0].equalsIgnoreCase("test")) {
                        if (args[1].equalsIgnoreCase("follow") || args[1].equalsIgnoreCase("gift") || args[1].equalsIgnoreCase("like") || args[1].equalsIgnoreCase("share")) {
                            available.add("<Chatter Username>");
                        }
                    }
                }
                break;

            case 4:
                if (ChatPointsTTV.getTikTok().isStarted()) {
                    if (args[0].equalsIgnoreCase("test")) {
                        if (args[1].equalsIgnoreCase("follow") || args[1].equalsIgnoreCase("gift") || args[1].equalsIgnoreCase("like") || args[1].equalsIgnoreCase("share")) {
                            if (ChatPointsTTV.getTikTok().listenedProfiles != null || !ChatPointsTTV.getTikTok().listenedProfiles.isEmpty()) {
                                available.addAll(ChatPointsTTV.getTikTok().listenedProfiles);
                            } else {
                                available.add("<Streamer Username>");
                            }
                        }
                    }
                }
                break;

            case 5:
                if (ChatPointsTTV.getTikTok().isStarted()) {
                    if (args[0].equalsIgnoreCase("test")) {
                        if (args[1].equalsIgnoreCase("gift")) {
                            available.add("<Gift>");
                        } else if (args[1].equalsIgnoreCase("like")) {
                            available.add("<Amount>");
                        }
                    }
                }
                break;

            case 6:
                if (ChatPointsTTV.getTikTok().isStarted()) {
                    if (args[0].equalsIgnoreCase("test")) {
                        if (args[1].equalsIgnoreCase("gift")) {
                            available.add("<Amount>");
                        }
                    }
                }
                break;
        }
            
        for (String s : available) {
            if (s.replace("\"", "").toLowerCase().startsWith(args[args.length - 1].replace("\"", "").toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }

}
