package me.gosdev.chatpointsttv.TikTok;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import me.gosdev.chatpointsttv.Chat.SpigotSender;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Commands.TikTokCommands;

public class TikTokCommandController implements TabExecutor {
    private TikTokCommands genericCommands = new TikTokCommands();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return genericCommands.onCommand(new SpigotSender(sender), args);
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
