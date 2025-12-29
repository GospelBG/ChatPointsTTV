package me.gosdev.chatpointsttv.Twitch;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.events.EventSubEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Utils.LocalizationUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TwitchCommandController implements TabExecutor {
    private final BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV Twitch Help" + ChatColor.RESET + " ----------\n" + 
        ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("twitch").getUsage() + ChatColor.RESET + "\n" +
        ChatColor.LIGHT_PURPLE + "/twitch accounts: " + ChatColor.RESET + "Manage linked accounts.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch link: " + ChatColor.RESET + "Use this command to link a Twitch account.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch unlink [username]: " + ChatColor.RESET + "Removes an account and the stored credentials. If a username is not provided all accounts will be unlinked.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch status: " + ChatColor.RESET + "Displays information about the plugin and the Twitch client.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch start: " + ChatColor.RESET + "Starts the Twitch client and logs in to any saved accounts.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch stop: " + ChatColor.RESET + "Stops the Twitch client. All incoming events will be ignored.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files.\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch test <type> <...>: " + ChatColor.RESET + "Mocks an event.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch help: " + ChatColor.RESET + "Displays this help message.").create()[0];
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ChatPointsTTV plugin = ChatPointsTTV.getPlugin();

        if (args.length == 0) {
            help(sender);
            return true;
        
        } else {
            switch (args[0]) {
                case "link":
                    if ((args.length > 1)) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch link");
                        return true;
                    }
                    TwitchAuth.getDeviceCode(plugin, sender);                    
                    return true;

                case "reload":
                    reload(sender);
                    return true;

                case "help":
                    help(sender);
                    return true;

                case "unlink":
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            ChatPointsTTV.getTwitch().linkThread.join();
                        } catch (InterruptedException | NullPointerException e) {}
                        
                        ChatPointsTTV.getTwitch().unlink(sender, args.length == 2 ? Optional.of(args[1]) : Optional.empty());
                    });
                    return true;

                case "accounts":
                    accounts(sender);
                    return true;
                    
                case "status":
                    displayStatus(sender, plugin);
                    return true;

                case "stop":
                    if (!ChatPointsTTV.getTwitch().isStarted()) {
                        sender.sendMessage(ChatColor.RED + "Twitch client is already stopped.");
                        return true;
                    }
                    ChatPointsTTV.getTwitch().stop(sender);
                    return true;

                case "start":
                    if (ChatPointsTTV.getTwitch().isStarted()) {
                        sender.sendMessage(ChatColor.RED + "Twitch client is already started.");
                        return true;
                    }
                    ChatPointsTTV.getTwitch().enable(sender);
                    sender.sendMessage(ChatPointsTTV.msgPrefix + "Twitch client has started successfully!");
                    return true;

                case "test":
                    test(sender, args);
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command: /twitch " + args[0]);
                    help(sender);
                    return true;
            }
        }
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
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("unlink")) {
            if (args[0].equalsIgnoreCase("unlink")) {
                for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    available.add(channel.getChannelUsername().toLowerCase());
                }
            }
        } else if (ChatPointsTTV.getTwitch().isAccountConnected() && args.length >= 2 && args[0].equalsIgnoreCase("test")) { // Test Command Arguments
            if (args.length == 2) {
                available.add("channelpoints");
                available.add("cheer");
                available.add("sub");
                available.add("follow");
                available.add("subgift");
                available.add("raid");
            } else if (args.length == 3) {
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

    private void reload(CommandSender p) {
        ChatPointsTTV.getTwitch().stop(p);
        ChatPointsTTV.getTwitch().enable(p);
    }

    private void help(CommandSender p) {
        p.spigot().sendMessage(helpMsg);

        if (!p.equals(Bukkit.getConsoleSender())){
            TextComponent docsTip = new TextComponent("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "\nTip: " + ChatColor.RESET + ChatColor.GRAY + "Check out ");

            TextComponent link = new TextComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + "ChatPointsTTV's website");
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gosdev.me/chatpointsttv/commands/twitch"));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));
            docsTip.addExtra(link);
            docsTip.addExtra(ChatColor.GRAY + " for more information on its commands!");
            
            p.spigot().sendMessage(docsTip);
        }
    }

    private void accounts(CommandSender p) {
        java.util.ArrayList<String> channels = new java.util.ArrayList<>();
        TextComponent msg = new TextComponent("\n---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Connected Accounts" + ChatColor.RESET + " ----------\n\n");
        
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            p.sendMessage(ChatColor.RED + "You must start the Twitch Client first!");
            return;
        }
        
        for (Channel i : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
            channels.add(i.getChannelUsername());
        }
        
        TextComponent footer = null;
        if (p.equals(Bukkit.getConsoleSender())) {
            footer = new TextComponent(ChatColor.ITALIC + "\nTo unlink an account, use /twitch unlink <channel>\nTo add an account, use /twitch link");
        } else {
            footer = TwitchButtonComponents.accountLink();
            if (!channels.isEmpty()) {
                footer.addExtra(ChatColor.GRAY + "  -  ");
                footer.addExtra(TwitchButtonComponents.accountUnlink());
            }
        }
        
        if (p.equals(Bukkit.getConsoleSender())) {
            for (String channel : channels) {
                msg.addExtra(ChatColor.GRAY + "  -  " + channel + "\n");
            }
        } else {
            for (String channel : channels) {
                BaseComponent deleteButton = new ComponentBuilder(ChatColor.RED + "  [❌]").create()[0];
                deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink this account").create()));
                deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch unlink " + channel));
                msg.addExtra(deleteButton);
                msg.addExtra(new TextComponent("  " + channel + "\n"));
            }
        }
        
        if (channels.isEmpty()) {
            msg.addExtra(ChatColor.GRAY + "  There are no connected accounts :(\n");
        }
        
        msg.addExtra(footer);
        msg.addExtra("\n");
        p.spigot().sendMessage(msg);
    }

    private void displayStatus(CommandSender p, ChatPointsTTV plugin) {
        String strChannels = "";
        
        if (ChatPointsTTV.getTwitch().getListenedChannels() == null || ChatPointsTTV.getTwitch().getListenedChannels().isEmpty()) {
            strChannels = "None";
        } else {
            for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                ChatColor color = channel.isLive() ? ChatColor.DARK_RED : ChatColor.GRAY;
                strChannels += color + channel.getChannelUsername() + ChatColor.RESET + ", ";
            }
            strChannels = strChannels.subSequence(0, strChannels.length() - 2).toString();
        }
        
        BaseComponent msg = new ComponentBuilder(
            "---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ChatPointsTTV Twitch status" + ChatColor.RESET + " ----------\n" +
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" + plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" +
            "\n"
        ).create()[0];
        
        String currentState = "";
        if (ChatPointsTTV.getTwitch().isStarted()) {
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                currentState = ChatColor.GREEN + "" + ChatColor.BOLD + "CONNECTED";
            } else {
                currentState = ChatColor.YELLOW + "" + ChatColor.BOLD + "UNLINKED";
            }
        } else {
            currentState = ChatColor.RED + "" + ChatColor.BOLD + "STOPPED";
        }
        
        BaseComponent status = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "Connection status: " + currentState).create()[0];
        msg.addExtra(status);
        
        if (!p.equals(Bukkit.getConsoleSender())) {
            msg.addExtra("\n\n");
            if (ChatPointsTTV.getTwitch().isStarted()) {
                msg.addExtra(TwitchButtonComponents.manageAccounts());
                msg.addExtra(ChatColor.GRAY + "  -  ");
                msg.addExtra(TwitchButtonComponents.clientStop());
            } else {
                msg.addExtra(TwitchButtonComponents.clientStart());
            }
        }
        
        p.spigot().sendMessage(msg);
    }

    private void test(CommandSender sender, String[] cmdInput) {
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
                            args[5] = args[5] + " " + args[i];
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
                    if (args.length != 4) {
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
                    if (args.length != 5) {
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
                    if (args.length != 5) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch test sub <user> <channel> <plan>");
                        return;
                    }
    
                    String subUser = args[2];
                    String subChannel = args[3];
                    SubscriptionPlan subTier;
    
                    try {
                        subTier = SubscriptionPlan.valueOf(args[4].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid subscription tier: " + args[4]);
                        return;
                    }
                    
    
                    try {
                        event = TwitchEventTest.SubEvent(subChannel, subUser, subTier);
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    break;
    
                case "subgift":
                    if (args.length != 5) {
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
                    if (args.length != 5) {
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
}
