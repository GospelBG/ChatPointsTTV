package me.gosdev.chatpointsttv.Twitch;

import java.util.Optional;

import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Utils.*;

import com.github.philippheuer.credentialmanager.domain.DeviceAuthorization;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.events.EventSubEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;

public class TwitchCommands {
    private static final String helpMsg = Translatable.getString("twitch.help.header", "  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " ----------\n") +
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

    public static void link(GenericSender sender) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.not_started", "Twitch"));
            return;
        }

        Boolean shouldHideCode = ChatPointsTTV.getInstance().config.getGeneralConfig().getHideLoginCodes();

        sender.sendMessage(ChatColor.GRAY + Translatable.getString("twitch.message.wait"));
        DeviceAuthorization auth = TwitchAuth.authorize(sender);

        ChatComponent comp = new ChatComponent(Translatable.getString("twitch.link.header", "\n  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " -------------\n\n"));
        if (sender.equals(ChatPointsTTV.getConsole())) {
            comp.addExtra(new ChatComponent(ChatColor.LIGHT_PURPLE + Translatable.getString("twitch.link.instructions", "" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + TwitchAuth.VERIFICATION_URL + ChatColor.LIGHT_PURPLE, "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode())));
        } else {
            ChatComponent button = new ChatComponent("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + ChatColor.UNDERLINE + Translatable.getString("twitch.link.interactive.button", "[", "]") + " ");
            button.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, auth.getVerificationUri()));
            button.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("generic.tooltip.open_browser")));

            ChatComponent code;
            comp.addExtra(button);

            String link = "" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + TwitchAuth.VERIFICATION_URL + ChatColor.RESET + ChatColor.LIGHT_PURPLE;
            if (shouldHideCode) {
                comp.addExtra(ChatColor.LIGHT_PURPLE + " " + Translatable.getString("twitch.link.interactive.no_code.text", "\n" + ChatColor.GRAY + ChatColor.ITALIC, "\n\n" + ChatColor.LIGHT_PURPLE, link) + " ");
                code = new ChatComponent("" + ChatColor.DARK_PURPLE + ChatColor.OBFUSCATED + ChatColor.BOLD + "ABCDEFGH");
                code.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode()));

                code.addExtra(" " + ChatColor.GRAY + ChatColor.ITALIC + Translatable.getString("twitch.link.interactive.no_code.hover"));
            } else {
                comp.addExtra(ChatColor.LIGHT_PURPLE + Translatable.getString("twitch.link.interactive.text", link, "\n\n" + ChatColor.GRAY + "   ➡ "));
                code = new ChatComponent("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode());
            }
            comp.addExtra(code);
            comp.addExtra("\n");
        }
        sender.sendMessage(comp);
        
    }

    public static void reload(GenericSender sender) {
        if (!ChatPointsTTV.getTwitch().reloading.compareAndSet(false, true)) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.already_reloading", Translatable.getString("generic.twitch_module")));
            return;
        }

        ChatPointsTTV.getTwitch().stop(sender);
        try {
            ChatPointsTTV.getTwitch().stopThread.join();
        } catch (InterruptedException ex) {
        }
        ChatPointsTTV.getInstance().enableTwitch(sender);

        sender.sendMessage(ChatPointsTTV.msgPrefix + Translatable.getString("generic.message.success.reload", "Twitch"));
    }

    public static void help(GenericSender sender) {
        sender.sendMessage(helpMsg);

        if (!sender.equals(ChatPointsTTV.getConsole())){
            ChatComponent docsTip = new ChatComponent(Translatable.getString("generic.help.tip.commands", "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, "" + ChatColor.RESET + ChatColor.GRAY).replaceAll("\\[.*", ""));

            ChatComponent link = new ChatComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + Translatable.getString("generic.help.tip.commands").replaceAll("(?:^|\\])[^\\[]*\\[?", "")); // Get button text
            link.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, "https://gosdev.me/chatpointsttv/commands/twitch"));
            link.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("generic.tooltip.open_browser")));
            docsTip.addExtra(link);
            docsTip.addExtra(ChatColor.GRAY + Translatable.getString("generic.help.tip.commands").replaceAll(".*\\]", ""));

            sender.sendMessage(docsTip);
        }
    }

    public static void start(GenericSender sender) {
        if (ChatPointsTTV.getTwitch().reloading.get()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.still_starting", "Twitch"));
            return;
        }
        if (ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.already_started", "Twitch"));
            return;
        }

        ChatPointsTTV.getInstance().enableTwitch(sender);
    }

    public static void stop(GenericSender sender) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.already_stopped", "Twitch"));
            return;
        }
        if (ChatPointsTTV.getTwitch().reloading.get()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.already_reloading", Translatable.getString("generic.twitch_module")));
        }

        ChatPointsTTV.getTwitch().stop(sender);
    }

    public static void accounts(GenericSender sender) {
        java.util.ArrayList<String> channels = new java.util.ArrayList<>();

        ChatComponent msg = new ChatComponent( Translatable.getString("twitch.accounts.header", "\n  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " ----------\n\n"));
        
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.not_started", "Twitch"));
            return;
        }
        
        for (Channel i : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
            channels.add(i.getChannelUsername());
        }

        ChatComponent footer;
        if (sender.equals(ChatPointsTTV.getConsole())) {
            footer = new ChatComponent(ChatColor.ITALIC + "\n" + Translatable.getString("twitch.accounts.footer.console"));
        } else {
            footer = TwitchButtonComponents.accountLink();
            if (!channels.isEmpty()) {
                footer.addExtra(ChatColor.GRAY + "  -  ");
                footer.addExtra(TwitchButtonComponents.accountUnlink());
            }
        }
        
        if (sender.equals(ChatPointsTTV.getConsole())) {
            for (String channel : channels) {
                msg.addExtra(ChatColor.GRAY + "  -  " + channel + "\n");
            }
        } else {
            for (String channel : channels) {
                ChatComponent deleteButton = new ChatComponent(ChatColor.RED + "  [❌]");
                deleteButton.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("twitch.accounts.tooltip.unlink")));
                deleteButton.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/twitch unlink " + channel));
                msg.addExtra(deleteButton);
                msg.addExtra(new ChatComponent("  " + channel + "\n"));
            }
        }
        
        if (channels.isEmpty()) {
            msg.addExtra(ChatColor.GRAY + "  " + Translatable.getString("twitch.accounts.no_accounts") + "\n");
        }
        
        msg.addExtra(footer);
        msg.addExtra("\n");
        sender.sendMessage(msg);
    }

    public static void displayStatus(GenericSender sender) {
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
            Translatable.getString("commands.status.header.twitch", "  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " ----------\n") +
            ChatColor.LIGHT_PURPLE + Translatable.getString("commands.status.plugin_version") + " " + ChatColor.RESET + "v" + ChatPointsTTV.getInstance().version + "\n" +
            ChatColor.LIGHT_PURPLE + Translatable.getString("commands.status.accounts.twitch") + " " + ChatColor.RESET + strChannels + "\n" +
            "\n"
        );
        
        String currentState;
        if (ChatPointsTTV.getTwitch().isStarted()) {
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                currentState = ChatColor.GREEN + "" + ChatColor.BOLD + Translatable.getString("commands.status.connected");
            } else {
                currentState = ChatColor.YELLOW + "" + ChatColor.BOLD + Translatable.getString("commands.status.unlinked");
            }
        } else {
            currentState = ChatColor.RED + "" + ChatColor.BOLD + Translatable.getString("commands.status.stopped");
        }

        ChatComponent status = new ChatComponent(ChatColor.LIGHT_PURPLE + Translatable.getString("commands.status.connection_status") + " " + currentState);
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

    public static void test(GenericSender sender, String[] cmdInput) {
        if (!ChatPointsTTV.getTwitch().isStarted() ) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.not_started", "Twitch"));
            return;
        }

        if (!ChatPointsTTV.getTwitch().isAccountConnected()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("commands.test.no_accounts.twitch"));
            return;
        }
        
        if (cmdInput.length < 2) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /twitch test <type> ...");
            return;
        }

        EventManager eventManager = ChatPointsTTV.getTwitch().getClient().getEventManager();
        EventSubEvent event;

        String[] args = LocalizationUtils.parseQuotes(cmdInput);

        try {
            switch (args[1].toLowerCase()) {
                case "channelpoints":
                    if (args.length < 5) {
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /twitch test channelpoints <redeemer> <channel> <reward> [userInput]");
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
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /twitch test follow <user> <channel>");
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
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /twitch test cheer <user> <channel> <amount>");
                        return;
                    }
    
                    String cheerUser = args[2];
                    String cheerChannel = args[3];
                    int cheerAmount;
    
                    try {
                        cheerAmount = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.test.invalid_amount", Translatable.getString("twitch.generic.cheer", args[4])));
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
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /twitch test sub <user> <channel> <plan>");
                        return;
                    }
    
                    String subUser = args[2];
                    String subChannel = args[3];
                    SubscriptionPlan subTier;
    
                    try {
                        subTier = SubscriptionPlan.valueOf(args[4].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.test.invalid_name", Translatable.getString("twitch.generic.sub_tier", args[4])));
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
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /twitch test subgift <user> <channel> <amount>");
                        return;
                    }
    
                    String giftChatter = args[2];
                    String giftChannel = args[3];
                    int giftAmount;
    
                    try {
                        giftAmount = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.test.invalid_amount", Translatable.getString("twitch.generic.gifted_subs", args[4])));
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
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /twitch test raid <raider> <channel> <viewer count>");
                        return;
                    }
    
                    String raidUser = args[2];
                    String raidChannel = args[3];
                    int raidViewers;
    
                    try {
                        raidViewers = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.test.invalid_amount", Translatable.getString("twitch.generic.viewer", args[4])));
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
                    sender.sendMessage(ChatColor.RED + Translatable.getString("commands.test.unknown_event", args[1]));
                    return;
            }
            eventManager.publish(event);
            sender.sendMessage(ChatColor.GREEN + Translatable.getString("commands.test.success"));

        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    public static void createReward(GenericSender sender, String username) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.not_started", "Twitch"));
            return;
        }

        if (!ChatPointsTTV.getTwitch().isAccountConnected()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("twitch.message.create_reward.no_account"));
            return;
        }

        String userId = ChatPointsTTV.getTwitch().getListenedChannels().get(username).getChannelId();
        if (ChatPointsTTV.getTwitch().createChannelPointRewards(ChatPointsTTV.getTwitch().credentialManager.get(userId))) {
            sender.sendMessage(ChatColor.GREEN + Translatable.getString("twitch.message.create_reward.success"));
        } else {
            sender.sendMessage(ChatColor.RED + Translatable.getString("twitch.message.create_reward.failed"));
        }
    }


}
