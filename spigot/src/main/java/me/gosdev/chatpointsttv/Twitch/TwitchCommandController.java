package me.gosdev.chatpointsttv.Twitch;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.twitch4j.common.enums.SubscriptionPlan;

import me.gosdev.chatpointsttv.Chat.SpigotSender;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Commands.TwitchCommands;

public class TwitchCommandController implements TabExecutor {
    private TwitchCommands genericCommands = new TwitchCommands();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return genericCommands.onCommand(new SpigotSender(sender), args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        ArrayList<String> available = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();

        if (args.length == 1) {
            available.add("help");
            available.add("reload");
            available.add("status");
            if (ChatPointsTTV.getTwitch().isStarted()) {
                available.add("link");
                available.add("stop");
                available.add("accounts");
            } else {
                available.add("start");
            }
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                available.add("test");
                available.add("accounts");
                available.add("unlink");
                available.add("createreward");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("createreward")) {
                available.addAll(ChatPointsTTV.getTwitch().getListenedChannels().keySet());
            } else if (args[0].equalsIgnoreCase("unlink")) {
                for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    available.add(channel.getChannelUsername().toLowerCase());
                }
            } else if (args[0].equalsIgnoreCase("test")) {
                available.add("channelpoints");
                available.add("cheer");
                available.add("sub");
                available.add("follow");
                available.add("subgift");
                available.add("raid");
            }
        } else if (ChatPointsTTV.getTwitch().isAccountConnected() && args.length > 2 && args[0].equalsIgnoreCase("test")) { // Test Command Arguments
            if (args.length == 3) {
                    available.add("<Chatter Name>");
            } else if (args.length == 4) {
                if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                    available.addAll(ChatPointsTTV.getTwitch().getListenedChannels().keySet());
                }
                available.add("<Streamer Channel>");
            } else if (args.length == 5) {
                switch (args[1].toLowerCase()) {
                    case "channelpoints":
                        available.add("<Reward Name>");
                        break;
                    case "cheer":
                        available.add("<Amount>");
                        break;

                    case "sub":
                        for (SubscriptionPlan plan : EnumSet.allOf(SubscriptionPlan.class)) {
                            if (plan.equals(SubscriptionPlan.NONE)) continue;
                            available.add(plan.name());
                        }
                        break;
                    
                    case "subgift":
                        available.add("<Amount>");
                        break;

                    case "raid":
                        available.add("<Viewers>");
                        break;
                }
            } else if (args.length > 5) {
                switch (args[1].toLowerCase()) {
                    case "channelpoints":
                        int rewardNameEnd = 4;
                        if (args[4].startsWith("\"")) { // Check if the reward name starts with a quote. If so, wait to find the closing quote
                            for (int i = 5; i < args.length; i++) {
                                rewardNameEnd = i;
                                if (args[i].endsWith("\"")) break;
                            }
                        }
                        if (args.length > rewardNameEnd + 1) {
                            available.add("[User Input]");
                        }
                        break;
                }
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
