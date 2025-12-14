package me.gosdev.chatpointsttv.Commands;

import java.util.ArrayList;
import java.util.Optional;

import org.bukkit.command.CommandSender;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.events.EventSubEvent;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.TikTok.TikTokClient;
import me.gosdev.chatpointsttv.TikTok.TikTokEventTest;
import me.gosdev.chatpointsttv.Twitch.TwitchEventTest;
import net.md_5.bungee.api.ChatColor;

public class TestCommand {
    public static void twitchTest(CommandSender sender, String[] cmdInput) {
        if (!ChatPointsTTV.getTwitch().isStarted() ) {
            sender.sendMessage(ChatColor.RED + "You must start the Twitch Client first!");
            return;
        }

        if (!ChatPointsTTV.getTwitch().isAccountConnected()) {
            sender.sendMessage(ChatColor.RED + "You must link a Twitch account in order to run test events!");
            return;
        }
        
        if (cmdInput.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /twitch test <type> ...");
            return;
        }

        EventManager eventManager = ChatPointsTTV.getTwitch().getClient().getEventManager();
        EventSubEvent event;

        String[] args = LocalizationUtils.parseQuotes(cmdInput);

        try {
            switch (args[1].toLowerCase()) {
                case "channelpoints":
                    if (args.length < 5) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch test channelpoints <redeemer> <channel> <reward> [userInput]");
                        return;
                    }
    
                    String pointsChatter = args[2];
                    String pointsChannel = args[3];
                    String pointsReward = args[4];
                    String userInput;
    
                    if (args.length <= 5) {
                        userInput = null;
                    } else {
                        for (int i = 6; i < args.length; i++) {
                            args[5] = args[5] + " " + args[i]; // All arguments after index 4 are considered user input
                        }
                        userInput = args[5];
                    }
                    try {
                        event = TwitchEventTest.ChannelPointsRedemptionEvent(pointsChannel, pointsChatter, pointsReward, userInput != null ? Optional.of(userInput) : Optional.empty());
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    break;
                case "follow":
                    if (args.length < 4) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch test follow <user> <channel>");
                        return;
                    }
    
                    String followUser = args[2];
                    String followChannel = args[3];
    
                    try {
                        event = TwitchEventTest.FollowEvent(followChannel, followUser);
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    break;
    
                case "cheer":
                    if (args.length < 5) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch test cheer <user> <channel> <amount>");
                        return;
                    }
    
                    String cheerUser = args[2];
                    String cheerChannel = args[3];
                    int cheerAmount;
    
                    try {
                        cheerAmount = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid cheer amount: " + args[4]);
                        return;
                    }
    
                    try {
                        event = TwitchEventTest.CheerEvent(cheerChannel, cheerUser, cheerAmount);
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    break;
    
                case "sub":
                    if (args.length < 6) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch test sub <user> <channel> <plan> <months>");
                        return;
                    }
    
                    String subUser = args[2];
                    String subChannel = args[3];
                    SubscriptionPlan subTier;
                    int subMonths;
    
                    try {
                        subTier = SubscriptionPlan.valueOf(args[4].toUpperCase());
                        subMonths = Integer.parseInt(args[5]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid amount of months: " + args[5]);
                        return;
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid subscription tier: " + args[4]);
                        return;
                    }
                    
    
                    try {
                        event = TwitchEventTest.SubEvent(subChannel, subUser, subTier, subMonths);
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    break;
    
                case "subgift":
                    if (args.length < 5) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch test subgift <user> <channel> <amount>");
                        return;
                    }
    
                    String giftChatter = args[2];
                    String giftChannel = args[3];
                    int giftAmount;
    
                    try {
                        giftAmount = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid gifted subs amount: " + args[4]);
                        return;
                    }
                    
                    try {
                        event = TwitchEventTest.SubGiftEvent(giftChannel, giftChatter, giftAmount);
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    break;
    
                case "raid":
                    if (args.length < 5) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch test raid <raider> <channel> <viewer count>");
                        return;
                    }
    
                    String raidUser = args[2];
                    String raidChannel = args[3];
                    int raidViewers;
    
                    try {
                        raidViewers = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid viewer amount: " + args[4]);
                        return;
                    }
                    
                    try {
                        event = TwitchEventTest.RaidReward(raidChannel, raidUser, raidViewers);
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    break;
    
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown test type: " + args[1]);
                    return;
            }
            eventManager.publish(event);
            sender.sendMessage(ChatColor.GREEN + "Test event sent!");

        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    public static void tiktokTest(CommandSender sender, String[] cmdInput) {
        if (cmdInput.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /tiktok test <type> ...");
            return;
        }

        LiveClient c;
        TikTokEvent event;
        String chatter = cmdInput[2];

        if (!TikTokClient.isEnabled) {
            sender.sendMessage(ChatColor.RED + "You must start the TikTok Client first!");
            return;
        }

        if (!TikTokClient.accountConnected) {
            sender.sendMessage(ChatColor.RED + "You must link a TikTok LIVE in order to run test events!");
            return;
        }
        if (!TikTokClient.getClients().containsKey(cmdInput[3].toLowerCase())) {
            sender.sendMessage(ChatColor.RED + "You need to link @" + cmdInput[3] + "'s LIVE first!");
            return;
        } else {
            c = TikTokClient.getClients().get(cmdInput[3].toLowerCase());
        }

        switch (cmdInput[1].toLowerCase()) {
            case "follow":
                event = TikTokEventTest.FollowEvent(chatter);
                break;

            case "like":
                try {
                    event = TikTokEventTest.LikeEvent(chatter, Integer.valueOf(cmdInput[4]));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid Like amount: " + cmdInput[4]);
                    return;
                }
                break;

            case "gift":
                try {
                    Gift item = c.getGiftManager().getByName(parseQuotes(cmdInput)[4]);
                    if (item == Gift.UNDEFINED) { // Query didn't match with available gifts
                        sender.sendMessage("Invalid Gift Item name: " + cmdInput[4]);
                        return;
                    } else {
                        event = TikTokEventTest.GiftEvent(chatter, c.getRoomInfo().getHost(), item, Integer.valueOf(cmdInput[5]));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid Gift Combo amount: " + cmdInput[5]);
                    return;
                }
                break;
            
            case "share":
                event = TikTokEventTest.ShareEvent(chatter);
                break;

            default:
                return;
        }

        c.publishEvent(event);
        sender.sendMessage(ChatColor.GREEN + "Test event sent!");
    }
}
