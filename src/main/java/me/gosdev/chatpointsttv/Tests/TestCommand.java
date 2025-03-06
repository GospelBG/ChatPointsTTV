package me.gosdev.chatpointsttv.Tests;

import java.util.ArrayList;
import java.util.Optional;

import org.bukkit.command.CommandSender;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.common.enums.SubscriptionPlan;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.ChatColor;

public class TestCommand {
    private static final EventManager eventManager = ChatPointsTTV.getPlugin().getTwitch().getClient().getEventManager();

    public static void test(CommandSender sender, String[] cmdInput) {
        if (cmdInput.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /twitch test <type> ...");
            return;
        }

        ArrayList<String> args =  new ArrayList<>();
        for (int i = 0; i < cmdInput.length; i++) {
            String arg = cmdInput[i];
            // Check if the argument starts with a quote and does not end with an escaped quote
            if (arg.startsWith("\"") && !arg.endsWith("\\\"")) {
            StringBuilder sb = new StringBuilder(arg.substring(1));
            // Continue appending arguments until the closing quote is found
            while (i + 1 < cmdInput.length && !(cmdInput[i + 1].endsWith("\"") && !cmdInput[i + 1].endsWith("\\\""))) {
                sb.append(" ").append(cmdInput[++i]);
            }
            // Append the last part of the quoted argument
            if (i + 1 < cmdInput.length) {
                sb.append(" ").append(cmdInput[++i], 0, cmdInput[i].length() - 1);
            }
            // Add the complete quoted argument to the args list
            args.add(sb.toString().replace("\\\"", "\""));
            } else {
            // Add the argument to the args list, replacing escaped quotes
            args.add(arg.replace("\\\"", "\""));
            }
        }

        switch (args.get(1).toLowerCase()) {
            case "channelpoints":
                if (args.size() < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /twitch test channelpoints <redeemer> <channel> <reward> [userInput]");
                    return;
                }

                String pointsChatter = args.get(2);
                String pointsChannel = args.get(3);
                String pointsReward = args.get(4);
                String userInput;

                if (args.size() <= 5) {
                    userInput = null;
                } else {
                    for (int i = 6; i < args.size(); i++) {
                        args.set(5, args.get(5) + " " + args.get(i)); // All arguments after index 4 are considered user input
                    }
                    userInput = args.get(5);
                }
                try {
                    eventManager.publish(EventTest.ChannelPointsRedemptionEvent(pointsChannel, pointsChatter, pointsReward, userInput != null ? Optional.of(userInput) : Optional.empty()));
                } catch (NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return;
                }
                break;
            case "follow":
                if (args.size() < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /twitch test follow <user> <channel>");
                    return;
                }

                String followUser = args.get(2);
                String followChannel = args.get(3);

                try {
                    eventManager.publish(EventTest.FollowEvent(followChannel, followUser));
                } catch (NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return;
                }
                break;

            case "cheer":
                if (args.size() < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /twitch test cheer <user> <channel> <amount>");
                    return;
                }

                String cheerUser = args.get(2);
                String cheerChannel = args.get(3);
                int cheerAmount;

                try {
                    cheerAmount = Integer.parseInt(args.get(4));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid cheer amount: " + args.get(4));
                    return;
                }

                try {
                    eventManager.publish(EventTest.CheerEvent(cheerChannel, cheerUser, cheerAmount));
                } catch (NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return;
                }
                break;

            case "sub":
                if (args.size() < 6) {
                    sender.sendMessage(ChatColor.RED + "Usage: /twitch test sub <user> <channel> <plan> <months>");
                    return;
                }

                String subUser = args.get(2);
                String subChannel = args.get(3);
                SubscriptionPlan subTier;
                int subMonths;

                try {
                    subTier = SubscriptionPlan.valueOf(args.get(4).toUpperCase());
                    subMonths = Integer.parseInt(args.get(5));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount of months: " + args.get(5));
                    return;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid subscription tier: " + args.get(4));
                    return;
                }
                

                try {
                    eventManager.publish(EventTest.SubEvent(subChannel, subUser, subTier, subMonths));
                } catch (NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return;
                }
                break;

            case "subgift":
                if (args.size() < 6) {
                    sender.sendMessage(ChatColor.RED + "Usage: /twitch test subgift <user> <channel> <tier> <amount>");
                    return;
                }

                String giftChatter = args.get(2);
                String giftChannel = args.get(3);
                SubscriptionPlan giftTier;
                int giftAmount;

                try {
                    giftAmount = Integer.parseInt(args.get(5));
                    giftTier = SubscriptionPlan.valueOf(args.get(4).toUpperCase());
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid gifted subs amount: " + args.get(5));
                    return;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid subscription tier: " + args.get(4));
                    return;
                }
                
                try {
                    eventManager.publish(EventTest.SubGiftEvent(giftChannel, giftChatter, giftTier, giftAmount));
                } catch (NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return;
                }
                break;

            case "raid":
                if (args.size() < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /twitch test raid <raider> <channel> <viewer count>");
                    return;
                }

                String raidUser = args.get(2);
                String raidChannel = args.get(3);
                int raidViewers;

                try {
                    raidViewers = Integer.parseInt(args.get(4));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid viewer amount: " + args.get(4));
                    return;
                }
                
                try {
                    eventManager.publish(EventTest.RaidReward(raidChannel, raidUser, raidViewers));
                } catch (NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return;
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown test type: " + args.get(1));
                return;
        }

        sender.sendMessage(ChatColor.GREEN + "Test event sent!");
    }
}
