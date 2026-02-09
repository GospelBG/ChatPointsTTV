package me.gosdev.chatpointsttv.Commands;

import java.util.ArrayList;
import java.util.Optional;

import com.github.philippheuer.credentialmanager.domain.DeviceAuthorization;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.events.EventSubEvent;

import me.gosdev.chatpointsttv.Chat.ChatComponent;
import me.gosdev.chatpointsttv.Chat.ChatEvent;
import me.gosdev.chatpointsttv.Chat.TwitchButtonComponents;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Twitch.Channel;
import me.gosdev.chatpointsttv.Twitch.TwitchAuth;
import me.gosdev.chatpointsttv.Twitch.TwitchEventTest;
import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.LocalizationUtils;
public class TwitchCommands {

    private final String helpMsg = "  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ChatPointsTTV Twitch Help" + ChatColor.RESET + " ----------\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch accounts: " + ChatColor.RESET + "Manage linked accounts.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch link: " + ChatColor.RESET + "Use this command to link a Twitch account.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch unlink [username]: " + ChatColor.RESET + "Removes an account and the stored credentials. If a username is not provided all accounts will be unlinked.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch status: " + ChatColor.RESET + "Displays information about the plugin and the Twitch module.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch createreward <username>: " + ChatColor.RESET + "Creates a new custom channel reward.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch start: " + ChatColor.RESET + "Starts the Twitch module and logs in to any saved accounts.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch stop: " + ChatColor.RESET + "Stops the Twitch module. All incoming events will be ignored.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files.\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch test <type> <...>: " + ChatColor.RESET + "Mocks an event.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch help: " + ChatColor.RESET + "Displays this help message.";
    
    public boolean onCommand(GenericSender sender, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        
        } else {
            switch (args[0]) {
                case "start":
                    start(sender);
                    return true;

                case "stop":
                    ChatPointsTTV.getTwitch().stop(sender);
                    return true;

                case "status":
                    displayStatus(sender);
                    return true;

                case "reload":
                    reload(sender);
                    return true;

                case "link":
                    if ((args.length > 1)) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch link");
                        return true;
                    }
                    link(sender);
                    return true;

                case "unlink":
                    ChatPointsTTV.getTwitch().unlink(sender, args.length == 2 ? Optional.of(args[1]) : Optional.empty());
                    return true;

                case "accounts":
                    accounts(sender);
                    return true;
                    
                case "test":
                    test(sender, args);
                    return true;

                case "createreward":
                    if (args.length != 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch createreward <username>");
                        return true;
                    }
                    createReward(sender, args[1]);                    
                    return true;

                case "help":
                    help(sender);
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command: /twitch " + args[0]);
                    help(sender);
                    return true;
            }
        }
    }

    private void link(GenericSender sender) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + "You must start the Twitch Module first!");
            return;
        }

        //*Bukkit.getScheduler().runTaskAsynchronously(SpigotPlugin.getPlugin(), () -> {
            Boolean shouldHideCode = ChatPointsTTV.getConfig().getBoolean("HIDE_LOGIN_CODES", false);

            sender.sendMessage(ChatColor.GRAY + "Please wait...");
            DeviceAuthorization auth = TwitchAuth.authorize(sender);

            ChatComponent comp = new ChatComponent("\n  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "Twitch Account Linking" + ChatColor.RESET + " -------------\n\n");
            if (sender.equals(ChatPointsTTV.getConsole())) {
                comp.addExtra(new ChatComponent(ChatColor.LIGHT_PURPLE + "Go to " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + TwitchAuth.VERIFICATION_URL + ChatColor.LIGHT_PURPLE + " and enter the code: " + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode()));
            } else {
                ChatComponent button = new ChatComponent("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + ChatColor.UNDERLINE + "[Click here]");
                button.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, auth.getVerificationUri()));
                button.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to open in browser"));

                ChatComponent code;
                comp.addExtra(button);
                if (shouldHideCode) {
                    comp.addExtra(ChatColor.LIGHT_PURPLE + " to login with Twitch.\n" + ChatColor.GRAY + ChatColor.ITALIC + "Careful! Clicking the button above will show your device code as part of the link");
                    comp.addExtra(ChatColor.LIGHT_PURPLE + "\n\nYou may also go to " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + TwitchAuth.VERIFICATION_URL + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " and enter this code: ");
                    code = new ChatComponent("" + ChatColor.DARK_PURPLE + ChatColor.OBFUSCATED + ChatColor.BOLD + "ABCDEFGH");
                    code.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode()));

                    code.addExtra("" + ChatColor.GRAY + ChatColor.ITALIC + " (Hover to view)");
                } else {
                    comp.addExtra(ChatColor.LIGHT_PURPLE + " or go to " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + TwitchAuth.VERIFICATION_URL + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " and enter this code:\n\n" + ChatColor.GRAY + "   ➡ ");
                    code = new ChatComponent("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode());
                }
                comp.addExtra(code);
                comp.addExtra("\n");
            }
            sender.sendMessage(comp);
        //});
        
    }

    private void reload(GenericSender sender) {
        if (!ChatPointsTTV.getTwitch().reloading.compareAndSet(false, true)) {
            sender.sendMessage(ChatColor.RED + "Twitch Module is already reloading!");
            return;
        }
        //Bukkit.getScheduler().runTaskAsynchronously(SpigotPlugin.getPlugin(), () -> {
            ChatPointsTTV.getTwitch().stop(sender);
            try {
                ChatPointsTTV.getTwitch().stopThread.join();
            } catch (InterruptedException ex) {
            }
            ChatPointsTTV.enableTwitch();

            sender.sendMessage(ChatPointsTTV.PREFIX + "Twitch Module reloaded!");
        //});
    }

    private void help(GenericSender sender) {
        sender.sendMessage(helpMsg);

        if (!sender.equals(ChatPointsTTV.getConsole())){
            ChatComponent docsTip = new ChatComponent("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "\nTip: " + ChatColor.RESET + ChatColor.GRAY + "Check out ");

            ChatComponent link = new ChatComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + "ChatPointsTTV's website");
            link.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, "https://gosdev.me/chatpointsttv/commands/twitch"));
            link.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to open in browser"));
            docsTip.addExtra(link);
            docsTip.addExtra(ChatColor.GRAY + " for more information on its commands!");
            
            sender.sendMessage(docsTip);
        }
    }

    private void start(GenericSender sender) {
        if (ChatPointsTTV.getTwitch().reloading.get()) {
            sender.sendMessage(ChatColor.RED + "Twitch Module is already starting.");
            return;
        }
        if (ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + "Twitch Module is already started.");
            return;
        }
        ChatPointsTTV.enableTwitch();
    }

    private void accounts(GenericSender sender) {
        ArrayList<String> channels = new ArrayList<>();
        ChatComponent msg = new ChatComponent("\n  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Connected Twitch Accounts" + ChatColor.RESET + " ----------\n\n");
        
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + "You must start the Twitch Module first!");
            return;
        }
        
        for (Channel i : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
            channels.add(i.getChannelUsername());
        }
        
        ChatComponent footer;
        if (sender.equals(ChatPointsTTV.getConsole())) {
            footer = new ChatComponent(ChatColor.ITALIC + "\nTo unlink an account, use /twitch unlink <channel>\nTo add an account, use /twitch link");

            for (String channel : channels) {
                msg.addExtra(ChatColor.GRAY + "  -  " + channel + "\n");
            }            
        } else {
            footer = TwitchButtonComponents.accountLink();
            if (!channels.isEmpty()) {
                footer.addExtra(ChatColor.GRAY + "  -  ");
                footer.addExtra(TwitchButtonComponents.accountUnlink());
            }

            for (String channel : channels) {
                ChatComponent deleteButton = new ChatComponent(ChatColor.RED + "  [❌]");
                deleteButton.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to unlink this account"));
                deleteButton.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch unlink " + channel));
                msg.addExtra(deleteButton);
                msg.addExtra(new ChatComponent("  " + channel + "\n"));
            }
        }
        
        if (channels.isEmpty()) {
            msg.addExtra(ChatColor.GRAY + "  There are no connected accounts :(\n");
        }
        
        msg.addExtra(footer);
        msg.addExtra("\n");
        sender.sendMessage(msg);
    }

    private void displayStatus(GenericSender sender) {
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
        
        ChatComponent msg = new ChatComponent(
            "  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ChatPointsTTV Twitch Status" + ChatColor.RESET + " ----------\n" +
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" + ChatPointsTTV.getLoader().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" +
            "\n"
        );
        
        String currentState;
        if (ChatPointsTTV.getTwitch().isStarted()) {
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                currentState = ChatColor.GREEN + "" + ChatColor.BOLD + "CONNECTED";
            } else {
                currentState = ChatColor.YELLOW + "" + ChatColor.BOLD + "UNLINKED";
            }
        } else {
            currentState = ChatColor.RED + "" + ChatColor.BOLD + "STOPPED";
        }
        
        ChatComponent status = new ChatComponent(ChatColor.LIGHT_PURPLE + "Connection status: " + currentState);
        msg.addExtra(status);
        
        if (!sender.equals(ChatPointsTTV.getConsole())) {
            msg.addExtra("\n\n");
            if (ChatPointsTTV.getTwitch().isStarted()) {
                msg.addExtra(TwitchButtonComponents.manageAccounts());
                msg.addExtra(ChatColor.GRAY + "  -  ");
                msg.addExtra(TwitchButtonComponents.clientStop());
            } else {
                msg.addExtra(TwitchButtonComponents.clientStart());
            }
        }
        
        sender.sendMessage(msg);
    }

    private void test(GenericSender sender, String[] cmdInput) {
        if (!ChatPointsTTV.getTwitch().isStarted() ) {
            sender.sendMessage(ChatColor.RED + "You must start the Twitch Module first!");
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
                    Optional<String> cheerMessage = Optional.empty();
    
                    try {
                        cheerAmount = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid cheer amount: " + args[4]);
                        return;
                    }

                    if (args.length > 5) cheerMessage = Optional.of(args[5]);
    
                    try {
                        event = TwitchEventTest.CheerEvent(cheerChannel, cheerUser, cheerAmount, cheerMessage);
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                        return;
                    }
                    break;
    
                case "sub":
                    if (args.length < 5) {
                        sender.sendMessage(ChatColor.RED + "Usage: /twitch test sub <user> <channel> <plan>");
                        return;
                    }
    
                    String subUser = args[2];
                    String subChannel = args[3];
                    SubscriptionPlan subTier;
                    Optional<String> subMessage = Optional.empty();
    
                    try {
                        subTier = SubscriptionPlan.valueOf(args[4].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid subscription tier: " + args[4]);
                        return;
                    }

                    if (args.length > 5) subMessage = Optional.of(args[5]);
                    
                    try {
                        event = TwitchEventTest.SubEvent(subChannel, subUser, subTier, subMessage);
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

    private void createReward(GenericSender sender, String username) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + "You must start the Twitch Module first!");
            return;
        }

        if (!ChatPointsTTV.getTwitch().isAccountConnected()) {
            sender.sendMessage(ChatColor.RED + "You must link a Twitch account in order to create channel point rewards!");
            return;
        }

        new Thread (() -> {
            String userId = ChatPointsTTV.getTwitch().getListenedChannels().get(username).getChannelId();
            if (ChatPointsTTV.getTwitch().createChannelPointRewards(ChatPointsTTV.getAccounts().getTwitchOAuth(userId))) {
                sender.sendMessage(ChatColor.GREEN + "A new Channel Point Reward has been created. Check your Twitch Dashboard!");
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to create a new Channel Point Reward. Check the server console for more information.");
            }
        }).run();
    }

    /*public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
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
    }*/
}
