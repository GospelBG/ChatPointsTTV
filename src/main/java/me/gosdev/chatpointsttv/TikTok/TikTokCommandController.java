package me.gosdev.chatpointsttv.TikTok;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Commands.AccountsCommand;
import me.gosdev.chatpointsttv.Commands.StatusCommand;
import me.gosdev.chatpointsttv.Commands.TestCommand;
import me.gosdev.chatpointsttv.Platforms;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TikTokCommandController implements TabExecutor {

    private final BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV help" + ChatColor.RESET + " ----------\n" + 
    ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("tiktok").getUsage() + ChatColor.RESET + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok accounts: " + ChatColor.RESET + "Manage linked accounts.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok link <username>: " + ChatColor.RESET + "Use this command to connect to a TikTok LIVE.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok unlink [username]: " + ChatColor.RESET + "Disconnects from a user's LIVE. If a username is not provided all accounts will be disconencted.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok status: " + ChatColor.RESET + "Displays information about the plugin and the TikTok client.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok start: " + ChatColor.RESET + "Starts the TikTok client and logs in to any saved accounts.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok stop: " + ChatColor.RESET + "Stops the TikTok client. All incoming events will be ignored.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files.\n" + 
    ChatColor.LIGHT_PURPLE + "/tiktok test <type> <...>: " + ChatColor.RESET + "Mocks an event.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok help: " + ChatColor.RESET + "Displays this help message.").create()[0];

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch(args[0]) {
            case "start":
                if (TikTokClient.isEnabled) {
                    sender.sendMessage(ChatColor.RED + "TikTok client is already started.");
                    return true;
                }

                sender.sendMessage("Enabling TikTok module...");

                TikTokClient.enable(sender);
                return true;

            case "stop":
                if (!TikTokClient.isEnabled) {
                    sender.sendMessage(ChatColor.RED + "TikTok client is already stopped.");
                    return true;
                }
                
                sender.sendMessage("Disabling TikTok module...");

                TikTokClient.stop(sender);
                return true;

            case "status":
                StatusCommand.status(sender, ChatPointsTTV.getPlugin(), Platforms.TIKTOK);
                return true;

            case "reload":
                sender.sendMessage("Reloading ChatPointsTTV...");
                TikTokClient.stop(sender);
                TikTokClient.enable(sender);
                return true;

            case "link":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok link <username>");
                    return true;
                }
                TikTokClient.link(sender, args[1], true);
                return true;

            case "unlink":
                if (args.length < 1 || args.length > 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok unlink <username>");
                    return true;
                }
                if (args.length == 2) {
                   for (String acc : TikTokClient.getClients().keySet()) {
                        TikTokClient.unlink(acc, true);
                   }
                } else {
                    TikTokClient.unlink(args[1], true);
                }
                
                sender.sendMessage(ChatColor.GREEN + "TikTok account " + args[1] + " unlinked successfully!");
                return true;

            case "accounts":
                AccountsCommand.displayAccounts(sender, Platforms.TIKTOK);
                return true;
            
            case "test":
                TestCommand.tiktokTest(sender, args);
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown command: /tiktok " + args[0]);
                help(sender);
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
                if (TikTokClient.isEnabled) {
                    available.add("stop");
                    available.add("link");
                    available.add("accounts");
                    if (TikTokClient.accountConnected) {
                        available.add("test");
                        available.add("accounts");  
                        available.add("unlink");
                    }
                } else {
                    available.add("start");
                }
                break;
            
            case 2:
                if (TikTokClient.isEnabled) {
                    if (args[0].equalsIgnoreCase("link")) {
                        available.add("<TikTok Username>");
                    } else if (args[0].equalsIgnoreCase("unlink") && TikTokClient.accountConnected) {
                        available.addAll(TikTokClient.getClients().keySet());
                    } else if (args[0].equalsIgnoreCase("test")) {
                        available.add("follow");
                        available.add("like");
                        available.add("gift");
                        available.add("share");
                    }
                }
                break;

            case 3:
                if (args[0].equalsIgnoreCase("test")) {
                    if (args[1].equalsIgnoreCase("follow") || args[1].equalsIgnoreCase("gift") || args[1].equalsIgnoreCase("like") || args[1].equalsIgnoreCase("share")) {
                        available.add("<Chatter Username>");
                    }
                }
                break;

            case 4: 
                if (args[0].equalsIgnoreCase("test")) {
                    if (args[1].equalsIgnoreCase("follow") || args[1].equalsIgnoreCase("gift") || args[1].equalsIgnoreCase("like") || args[1].equalsIgnoreCase("share")) {
                        if (!TikTokClient.listenedProfiles.isEmpty()) {
                            available.addAll(TikTokClient.listenedProfiles);
                        } else {
                            available.add("<Streamer Username>");
                        }
                    }
                } 
                break;

            case 5:
                if (args[0].equalsIgnoreCase("test")) {
                    if (args[1].equalsIgnoreCase("gift")) {
                        if (TikTokClient.getClients().get(args[3]) != null) {
                            List<Gift> availableGifts = TikTokClient.getClients().get(args[3]).getGiftManager().toList();
                            for (Gift g : availableGifts) {
                                String name = g.getName();
                                if (name.contains(" ")) { // Surround gift name with quotes if it has 2+ words
                                    name = "\"" + name + "\"";
                                }
                                available.add(name);
                            }
                        } else {
                            available.add("<Gift>");
                        }
                    } else if (args[1].equalsIgnoreCase("like")) {
                        available.add("<Amount>");
                    }
                }
                break;

            case 6:
                if (args[0].equalsIgnoreCase("test")) {
                    if (args[1].equalsIgnoreCase("gift")) {
                        available.add("<Amount>");
                    }
                }
        }
            
        for (String s : available) {
            if (s.startsWith(args[args.length - 1].replace("\"", ""))) {
                result.add(s);
            }
        }
        return result;
    }

    public void help(CommandSender p) {
        p.spigot().sendMessage(helpMsg);
    }
}
