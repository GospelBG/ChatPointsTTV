package me.gosdev.chatpointsttv.TikTok;

import java.util.List;
import java.util.Optional;

import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Utils.*;

import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import me.gosdev.chatpointsttv.ChatPointsTTV;


public class TikTokCommands {

    private static final ChatComponent helpMsg = new ChatComponent(Translatable.getString("tiktok.help.header", "  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " ----------\n" ) +
    ChatColor.LIGHT_PURPLE + "/tiktok accounts: " + ChatColor.RESET + Translatable.getString("tiktok.help.accounts") + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok link <username>: " + ChatColor.RESET + Translatable.getString("tiktok.help.link") + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok unlink [username]: " + ChatColor.RESET + Translatable.getString("tiktok.help.unlink") + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok status: " + ChatColor.RESET + Translatable.getString("tiktok.help.status") + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok start: " + ChatColor.RESET + Translatable.getString("tiktok.help.start") + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok stop: " + ChatColor.RESET + Translatable.getString("tiktok.help.stop") + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok reload: " + ChatColor.RESET + Translatable.getString("tiktok.help.reload") + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok test <type> <...>: " + ChatColor.RESET + Translatable.getString("tiktok.help.test") + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok help: " + ChatColor.RESET + Translatable.getString("tiktok.help.help"));

    public static void start(GenericSender sender) {
        if (ChatPointsTTV.getTikTok().reloading.get()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.still_starting", "TikTok"));
            return;
        }
        if (ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.already_started", "TikTok"));
            return;
        }

        sender.sendMessage(Translatable.getString("tiktok.message.starting"));
        ChatPointsTTV.getInstance().enableTikTok(sender);
    }

    public static void help(GenericSender sender) {
        sender.sendMessage(helpMsg);

        if (!sender.isConsole()) {
            ChatComponent docsTip = new ChatComponent(Translatable.getString("generic.help.tip.commands", "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, "" + ChatColor.RESET + ChatColor.GRAY).replaceAll("\\[.*", ""));

            ChatComponent link = new ChatComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + Translatable.getString("generic.help.tip.commands").replaceAll("(?:^|\\])[^\\[]*\\[?", "")); // Get button text
            link.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.OPEN_URL, "https://gosdev.me/chatpointsttv/commands/tiktok"));
            link.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("generic.tooltip.open_browser")));
            docsTip.addExtra(link);
            docsTip.addExtra(ChatColor.GRAY + Translatable.getString("generic.help.tip.commands").replaceAll(".*\\]", ""));
            
            sender.sendMessage(docsTip);
        }
    }

    public static void accounts(GenericSender sender) {
        List<String> accounts = ChatPointsTTV.getTikTok().listenedProfiles;
        
        if (!ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.not_started", "TikTok"));
            return;
        }

        ChatComponent msg = new ChatComponent(Translatable.getString("tiktok.accounts.header", "\n  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " -------------\n\n"));
        
        for (String account : accounts) {
            if (sender.equals(ChatPointsTTV.getConsole())) {
                if (account.isBlank()) continue;
                msg.addExtra(ChatColor.GRAY + "  -  @" + account + "\n");
            } else {
                if (account.isBlank()) continue;
                ChatComponent deleteButton = new ChatComponent(ChatColor.RED + "  [❌]");
                deleteButton.setHoverEvent(new ChatEvent.HoverEvent(ChatEvent.HoverAction.SHOW_TEXT, Translatable.getString("tiktok.accounts.tooltip.unlink")));
                deleteButton.setClickEvent(new ChatEvent.ClickEvent(ChatEvent.ClickAction.RUN_COMMAND, "/tiktok unlink " + account));
                msg.addExtra(deleteButton);
                msg.addExtra(new ChatComponent("  @" + account + "\n"));
            }
        }

        if (accounts.isEmpty()) {
            msg.addExtra(ChatColor.GRAY + "  " + Translatable.getString("tiktok.accounts.no_accounts") + "\n");
        }

        ChatComponent footer;
        if (sender.equals(ChatPointsTTV.getConsole())) {
            footer = new ChatComponent(ChatColor.ITALIC + "\n" + Translatable.getString("tiktok.accounts.footer"));
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

    public static void stop(GenericSender sender) {
        if (!ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.already_stopped", "TikTok"));
            return;
        }
        if (ChatPointsTTV.getTikTok().reloading.get()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.still_starting", "TikTok"));
            return;
        }
        
        sender.sendMessage(Translatable.getString("tiktok.message.stopping"));
        ChatPointsTTV.getTikTok().stop(sender);
    }

    public static void reload(GenericSender sender) {
        if (!ChatPointsTTV.getTikTok().reloading.compareAndSet(false, true)) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.already_reloading", Translatable.getString("generic.tiktok_module")));
            return;
        }

        sender.sendMessage(Translatable.getString("tiktok.message.reloading"));
        ChatPointsTTV.getTikTok().stop(sender);

        try {
            ChatPointsTTV.getTikTok().stopThread.join();
        } catch (InterruptedException e) {
        }

        ChatPointsTTV.getInstance().enableTikTok(sender);
    }

    public static void link(GenericSender sender, String username) {
        if (ChatPointsTTV.getTikTok().reloading.get()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.still_starting", "TikTok"));
            return;
        }
        if (!ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.not_started", "TikTok"));
            return;
        }

        ChatPointsTTV.getTikTok().link(sender, username, true);
    }

    public static void unlink(GenericSender sender, Optional<String> channelField) {
        if (channelField.isEmpty()) {
            for (String acc : ChatPointsTTV.getTikTok().getClients().keySet()) {
                ChatPointsTTV.getTikTok().unlink(acc, true);
            }
            sender.sendMessage(ChatColor.GREEN + Translatable.getString("tiktok.message.unlink.success_all"));
        } else {
            if (ChatPointsTTV.getTikTok().getClients().containsKey(channelField.get().toLowerCase())) {
                ChatPointsTTV.getTikTok().unlink(channelField.get(), true);
                sender.sendMessage(ChatColor.GREEN + Translatable.getString("tiktok.message.unlink.success", channelField.get()));
            } else {
                sender.sendMessage(ChatColor.RED + Translatable.getString("tiktok.message.unlink.not_linked", channelField.get()));
            }
        }

    }

    public static void displayStatus(GenericSender sender) {
        String strChannels = "";
        
        if (ChatPointsTTV.getTikTok().getClients() == null || ChatPointsTTV.getTikTok().getClients().isEmpty()) {
            strChannels = Translatable.getString("commands.status.no_accounts");
        } else {
            for (String profile : ChatPointsTTV.getTikTok().getClients().keySet()) {
                strChannels += "@" + profile + ", ";
            }
            strChannels = strChannels.subSequence(0, strChannels.length() - 2).toString();
        }

        ChatComponent msg = new ChatComponent(
            Translatable.getString("commands.status.header.tiktok", "  ---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD, ChatColor.RESET + " ----------\n") +
            ChatColor.LIGHT_PURPLE + Translatable.getString("commands.status.plugin_version") + " " + ChatColor.RESET + "v" + ChatPointsTTV.getInstance().version + "\n" +
            ChatColor.LIGHT_PURPLE + Translatable.getString("commands.status.accounts.tiktok") + " " + ChatColor.RESET + strChannels + "\n" +
            "\n"
        );

        String currentState = "";
        if (ChatPointsTTV.getTikTok().isStarted()) {
            if (ChatPointsTTV.getTikTok().isAccountConnected()) {
                currentState = ChatColor.GREEN + "" + ChatColor.BOLD + Translatable.getString("commands.status.connected");
            } else {
                currentState = ChatColor.YELLOW + "" + ChatColor.BOLD + Translatable.getString("commands.status.unlinked");
            }
        } else {
            currentState = ChatColor.RED + "" + ChatColor.BOLD + Translatable.getString("commands.status.stopped");
        }

        ChatComponent status = new ChatComponent(ChatColor.LIGHT_PURPLE + Translatable.getString("commands.status.connection_status") + " " + currentState);
        msg.addExtra(status);

        if (!sender.isConsole()) {
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

    public static void test(GenericSender sender, String[] cmdInput) {
        LiveClient c = null;
        io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent event;
        String chatter = cmdInput[2];
        Boolean offlineTest = false;

        if (!ChatPointsTTV.getTikTok().isStarted()) {
            sender.sendMessage(ChatColor.RED + Translatable.getString("generic.message.not_started", "TikTok"));
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
                    sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /tiktok test follow <chatter> <host>");
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
                    sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /tiktok test follow <chatter> <host>");
                    return;
                }

                try {
                    event = TikTokEventTest.LikeEvent(chatter, Integer.valueOf(cmdInput[4]));
                    if (offlineTest) {
                        ChatPointsTTV.getTikTok().getEventHandler().onLike((TikTokLikeEvent) event, cmdInput[3].toLowerCase());
                        return;
                    } 
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + Translatable.getString("commands.test.invalid_amount", Translatable.getString("tiktok.generic.like", cmdInput[4])));
                    return;
                }
                break;

            case "gift":
                cmdInput = LocalizationUtils.parseQuotes(cmdInput);
                if (cmdInput.length != 6) {
                    sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /tiktok test follow <chatter> <host>");
                    return;
                }

                if (offlineTest) {
                    ChatPointsTTV.getTikTok().getEventHandler().onGift(TikTokEventTest.GiftEvent(chatter, TikTokEventTest.generateUser(cmdInput[3].toLowerCase()), new Gift(0, cmdInput[4], 0, ""), Integer.valueOf(cmdInput[5])), cmdInput[3].toLowerCase());
                    return;
                } else {
                    try {
                        Gift item = c.getGiftManager().getByName(cmdInput[4]);
                        if (item == Gift.UNDEFINED) {
                            sender.sendMessage(Translatable.getString("commands.test.invalid_name", Translatable.getString("tiktok.generic.gift_item", cmdInput[4])));
                            return;
                        } else {
                            event = TikTokEventTest.GiftEvent(chatter, c.getRoomInfo().getHost(), item, Integer.valueOf(cmdInput[5]));
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + Translatable.getString("commands.test.invalid_amount", Translatable.getString("tiktok.generic.gift_combo", cmdInput[5])));
                        return;
                    }
                }
                
                break;
            
            case "share":
                if (cmdInput.length != 4) {
                    sender.sendMessage(ChatColor.RED + Translatable.getString("commands.usage") + " /tiktok test follow <chatter> <host>");
                    return;
                }

                event = TikTokEventTest.ShareEvent(chatter);
                if (offlineTest) {
                    ChatPointsTTV.getTikTok().getEventHandler().onShare((TikTokShareEvent) event, cmdInput[3].toLowerCase());
                    return;
                } 
                break;

            default:
                Translatable.getString("commands.test.unknown_event", cmdInput[1]);
                return;
        }

        c.publishEvent(event);
        sender.sendMessage(ChatColor.GREEN + Translatable.getString("commands.test.event_sent"));
    }


}
