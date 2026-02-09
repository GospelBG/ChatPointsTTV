package me.gosdev.chatpointsttv.Commands;

import java.util.List;
import java.util.Optional;

import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import me.gosdev.chatpointsttv.Chat.ChatComponent;
import me.gosdev.chatpointsttv.Chat.ChatEvent;
import me.gosdev.chatpointsttv.Chat.TikTokButtonComponents;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.TikTok.TikTokEventTest;
import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.LocalizationUtils;

public class TikTokCommands {

    private final ChatComponent helpMsg = new ChatComponent("  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ChatPointsTTV TikTok Help" + ChatColor.RESET + " ----------\n" + 
    ChatColor.LIGHT_PURPLE + "/tiktok accounts: " + ChatColor.RESET + "Manage linked accounts.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok link <username>: " + ChatColor.RESET + "Use this command to connect to a TikTok LIVE.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok unlink [username]: " + ChatColor.RESET + "Disconnects from a user's LIVE. If a username is not provided all accounts will be disconencted.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok status: " + ChatColor.RESET + "Displays information about the plugin and the TikTok Module.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok start: " + ChatColor.RESET + "Starts the TikTok Module and logs in to any saved accounts.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok stop: " + ChatColor.RESET + "Stops the TikTok Module. All incoming events will be ignored.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files.\n" + 
    ChatColor.LIGHT_PURPLE + "/tiktok test <type> <...>: " + ChatColor.RESET + "Mocks an event.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok help: " + ChatColor.RESET + "Displays this help message.");

    public boolean onCommand(GenericSender sender, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch(args[0]) {
            case "start":
                start(sender);
                return true;

            case "stop":
                stop(sender);
                return true;

            case "status":
                displayStatus(sender);
                return true;

            case "reload":
                reload(sender);
                return true;

            case "link":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok link <username>");
                    return true;
                }
                link(sender, args[1]);
                return true;

            case "unlink":
                if (args.length < 1 || args.length > 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok unlink [username]");
                    return true;
                }
                unlink(sender, args.length == 2 ? Optional.of(args[1]) : Optional.empty());
                return true;

            case "accounts":
                accounts(sender);
                return true;
            
            case "test":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok test <type> ...");
                    return true;
                }
                test(sender, args);
                return true;

            case "help":
                help(sender);
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown command: /tiktok " + args[0]);
                help(sender);
                return false;
        }
    }

    private void start(GenericSender sender) {
        if (ChatPointsTTV.getTikTok().reloading.get()) {
            sender.sendMessage(ChatColor.RED + "TikTok Module is still starting. Please wait.");
            return;
        }
        if (ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + "TikTok Module is already started.");
            return;
        }

        sender.sendMessage("Enabling TikTok module...");
        ChatPointsTTV.enableTikTok();
    }

    private void help(GenericSender sender) {
        sender.sendMessage(helpMsg);

        if (!sender.equals(ChatPointsTTV.getConsole())){
            ChatComponent docsTip = new ChatComponent("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "\nTip: " + ChatColor.RESET + ChatColor.GRAY + "Check out ");

            ChatComponent link = new ChatComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + "ChatPointsTTV's website");
            link.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, "https://gosdev.me/chatpointsttv/commands/tiktok"));
            link.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to open in browser"));
            docsTip.addExtra(link);
            docsTip.addExtra(ChatColor.GRAY + " for more information on its commands!");
            
            sender.sendMessage(docsTip);
        }
    }

    private void accounts(GenericSender sender) {
        List<String> accounts = ChatPointsTTV.getTikTok().listenedProfiles;
        
        if (!ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + "You must start the TikTok Module first!");
            return;
        }

        ChatComponent msg = new ChatComponent("\n  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Connected TikTok LIVEs" + ChatColor.RESET + " -------------\n\n");
        
        for (String account : accounts) {
            if (sender.equals(ChatPointsTTV.getConsole())) {
                if (account.isBlank()) continue;
                msg.addExtra(ChatColor.GRAY + "  -  @" + account + "\n");
            } else {
                if (account.isBlank()) continue;
                ChatComponent deleteButton = new ChatComponent(ChatColor.RED + "  [❌]");
                deleteButton.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, "Click to unlink this LIVE"));
                deleteButton.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/tiktok unlink " + account));
                msg.addExtra(deleteButton);
                msg.addExtra(new ChatComponent("  @" + account + "\n"));
            }
        }

        if (accounts.isEmpty()) {
            msg.addExtra(ChatColor.GRAY + "  There are no connected LIVEs :(\n");
        }

        ChatComponent footer;
        if (sender.equals(ChatPointsTTV.getConsole())) {
            footer = new ChatComponent(ChatColor.ITALIC + "\nTo unlink a LIVE, use /tiktok unlink <username>\nTo add a LIVE, use /tiktok link <username>");
        } else {
            footer = TikTokButtonComponents.accountLink();
            if (!accounts.isEmpty()) {
                footer.addExtra(ChatColor.GRAY + "  -  ");
                footer.addExtra(TikTokButtonComponents.accountUnlink());
            }
        }
        
        msg.addExtra(footer);
        msg.addExtra("\n");
        sender.sendMessage(msg);
    }

    private void stop(GenericSender sender) {
        if (!ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + "TikTok Module is stopped.");
            return;
        }
        if (ChatPointsTTV.getTikTok().reloading.get()) {
            sender.sendMessage(ChatColor.RED + "TikTok Module is still stopping. Please wait.");
            return;
        }
        
        sender.sendMessage("Disabling TikTok module...");

        ChatPointsTTV.getTikTok().stop(sender);
    }

    private void reload(GenericSender sender) {
        if (!ChatPointsTTV.getTikTok().reloading.compareAndSet(false, true)) {
            sender.sendMessage(ChatColor.RED + "TikTok module is reloading!");
            return;
        }

        //Bukkit.getScheduler().runTaskAsynchronously(SpigotPlugin.getPlugin(), () -> {
            sender.sendMessage("Reloading ChatPointsTTV...");
            ChatPointsTTV.getTikTok().stop(sender);

            try {
                ChatPointsTTV.getTikTok().stopThread.join();
            } catch (InterruptedException e) {
            }

            ChatPointsTTV.enableTikTok();
        //});
    }

    private void link(GenericSender sender, String username) {
        if (ChatPointsTTV.getTikTok().reloading.get()) {
            sender.sendMessage(ChatColor.RED + "TikTok Module is still starting. Please wait.");
            return;
        }
        if (!ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + "You must start the TikTok Module first!");
            return;
        }

        //Bukkit.getScheduler().runTaskAsynchronously(SpigotPlugin.getPlugin(), () -> {
            ChatPointsTTV.getTikTok().link(sender, username, true);
        //});
    }

    private void unlink(GenericSender sender, Optional<String> channelField) {
        if (!channelField.isPresent()) {
            for (String acc : ChatPointsTTV.getTikTok().getClients().keySet()) {
                ChatPointsTTV.getTikTok().unlink(acc, true);
            }
            sender.sendMessage(ChatColor.GREEN + "All accounts have been unlinked successfully!");
        } else {
            if (ChatPointsTTV.getTikTok().getClients().containsKey(channelField.get().toLowerCase())) {
                ChatPointsTTV.getTikTok().unlink(channelField.get(), true);
                sender.sendMessage(ChatColor.GREEN + "TikTok account " + channelField.get() + " unlinked successfully!");
            } else {
                sender.sendMessage(ChatColor.RED + "Couldn't find " + channelField.get() + "'s LIVE linked!");
            }
        }

    }

    private void displayStatus(GenericSender sender) {
        String strChannels = "";
        
        if (ChatPointsTTV.getTikTok().getClients() == null || ChatPointsTTV.getTikTok().getClients().isEmpty()) {
            strChannels = "None";
        } else {
            for (String profile : ChatPointsTTV.getTikTok().getClients().keySet()) {
                strChannels += "@" + profile + ", ";
            }
            strChannels = strChannels.subSequence(0, strChannels.length() - 2).toString();
        }

        ChatComponent msg = new ChatComponent(
            "  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ChatPointsTTV TikTok Status" + ChatColor.RESET + " ----------\n" +
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + ChatPointsTTV.getLoader().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened LIVEs: " + ChatColor.RESET + strChannels + "\n" + 
            "\n"
        );

        String currentState = "";
        if (ChatPointsTTV.getTikTok().isStarted()) {
            if (ChatPointsTTV.getTikTok().isAccountConnected()) {
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
            if (ChatPointsTTV.getTikTok().isStarted()) {
                msg.addExtra(TikTokButtonComponents.manageAccounts());
                msg.addExtra(ChatColor.GRAY + "  -  ");
                msg.addExtra(TikTokButtonComponents.clientStop());
            } else {
                msg.addExtra(TikTokButtonComponents.clientStart());
            }
        }

        sender.sendMessage(msg);
    }

    private void test(GenericSender sender, String[] cmdInput) {
        LiveClient c = null;
        io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent event;
        String chatter = cmdInput[2];
        Boolean offlineTest = false;

        if (!ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + "You must start the TikTok Module first!");
            return;
        }

        if (!ChatPointsTTV.getTikTok().getClients().containsKey(cmdInput[3].toLowerCase())) {
            offlineTest = true;
        } else {
            c = ChatPointsTTV.getTikTok().getClients().get(cmdInput[3].toLowerCase());
        }

        switch (cmdInput[1].toLowerCase()) {
            case "follow":
                if (cmdInput.length != 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok test follow <chatter> <host>");
                    return;
                }

                event = TikTokEventTest.FollowEvent(chatter);
                if (offlineTest) {
                    ChatPointsTTV.getTikTok().getEventHandler().onFollow((io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent) event, cmdInput[3].toLowerCase());
                    return;
                } 
                break;

            case "like":
                if (cmdInput.length != 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok test follow <chatter> <host>");
                    return;
                }

                try {
                    event = TikTokEventTest.LikeEvent(chatter, Integer.valueOf(cmdInput[4]));
                    if (offlineTest) {
                        ChatPointsTTV.getTikTok().getEventHandler().onLike((TikTokLikeEvent) event, cmdInput[3].toLowerCase());
                        return;
                    } 
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid Like amount: " + cmdInput[4]);
                    return;
                }
                break;

            case "gift":
                cmdInput = LocalizationUtils.parseQuotes(cmdInput);
                if (cmdInput.length != 6) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok test follow <chatter> <host>");
                    return;
                }

                if (offlineTest) {
                    ChatPointsTTV.getTikTok().getEventHandler().onGift(TikTokEventTest.GiftEvent(chatter, TikTokEventTest.generateUser(cmdInput[3].toLowerCase()), new Gift(0, cmdInput[4], 0, ""), Integer.valueOf(cmdInput[5])), cmdInput[3].toLowerCase());
                    return;
                } else {
                    try {
                        Gift item = c.getGiftManager().getByName(cmdInput[4]);
                        if (item == Gift.UNDEFINED) {
                            sender.sendMessage("Invalid Gift Item name: " + cmdInput[4]);
                            return;
                        } else {
                            event = TikTokEventTest.GiftEvent(chatter, c.getRoomInfo().getHost(), item, Integer.valueOf(cmdInput[5]));
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid Gift Combo amount: " + cmdInput[5]);
                        return;
                    }
                }
                
                break;
            
            case "share":
                if (cmdInput.length != 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok test follow <chatter> <host>");
                    return;
                }

                event = TikTokEventTest.ShareEvent(chatter);
                if (offlineTest) {
                    ChatPointsTTV.getTikTok().getEventHandler().onShare((TikTokShareEvent) event, cmdInput[3].toLowerCase());
                    return;
                } 
                break;

            default:
                return;
        }

        c.publishEvent(event);
        sender.sendMessage(ChatColor.GREEN + "Test event sent!");
    }
}
