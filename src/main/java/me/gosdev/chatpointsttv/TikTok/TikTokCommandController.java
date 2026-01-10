package me.gosdev.chatpointsttv.TikTok;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.LocalizationUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TikTokCommandController implements TabExecutor {

    private final BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV TikTok Help" + ChatColor.RESET + " ----------\n" + 
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
                if (ChatPointsTTV.getTikTok().isReloading.get()) {
                    sender.sendMessage(ChatColor.RED + "TikTok Module is still starting. Please wait.");
                    return true;
                }
                if (ChatPointsTTV.getTikTok().started) {
                    sender.sendMessage(ChatColor.RED + "TikTok Module is already started.");
                    return true;
                }

                sender.sendMessage("Enabling TikTok module...");

                ChatPointsTTV.enableTikTok(sender);
                return true;

            case "stop":
                if (!ChatPointsTTV.getTikTok().started) {
                    sender.sendMessage(ChatColor.RED + "TikTok Module is stopped.");
                    return true;
                }
                if (ChatPointsTTV.getTikTok().isReloading.get()) {
                    sender.sendMessage(ChatColor.RED + "TikTok Module is still stopping. Please wait.");
                    return true;
                }
                
                sender.sendMessage("Disabling TikTok module...");

                ChatPointsTTV.getTikTok().stop(sender);
                return true;

            case "status":
                displayStatus(sender, ChatPointsTTV.getPlugin());
                return true;

            case "reload":
                if (!ChatPointsTTV.getTikTok().isReloading.compareAndSet(false, true)) {
                    sender.sendMessage(ChatColor.RED + "TikTok module is reloading!");
                    return true;
                }
                sender.sendMessage("Reloading ChatPointsTTV...");
                ChatPointsTTV.getTikTok().stop(sender);
                ChatPointsTTV.enableTikTok(sender);
                return true;

            case "link":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok link <username>");
                    return true;
                }
                if (ChatPointsTTV.getTikTok().isReloading.get()) {
                    sender.sendMessage(ChatColor.RED + "TikTok Module is still starting. Please wait.");
                    return true;
                }
                if (!ChatPointsTTV.getTikTok().isStarted()) {
                    sender.sendMessage(ChatColor.RED + "You must start the TikTok Module first!");
                    return true;
                }

                Bukkit.getScheduler().runTaskAsynchronously(ChatPointsTTV.getPlugin(), () -> {
                    ChatPointsTTV.getTikTok().link(sender, args[1], true);
                });
                return true;

            case "unlink":
                if (args.length < 1 || args.length > 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok unlink [username]");
                    return true;
                }
                if (args.length == 1) {
                   for (String acc : ChatPointsTTV.getTikTok().getClients().keySet()) {
                        ChatPointsTTV.getTikTok().unlink(acc, true);
                   }
                   sender.sendMessage(ChatColor.GREEN + "All accounts have been unlinked successfully!");
                } else {
                    if (ChatPointsTTV.getTikTok().getClients().containsKey(args[1].toLowerCase())) {
                        ChatPointsTTV.getTikTok().unlink(args[1], true);
                        sender.sendMessage(ChatColor.GREEN + "TikTok account " + args[1] + " unlinked successfully!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Couldn't find " + args[1] + "'s LIVE linked!");
                    }
                }
                return true;

            case "accounts":
                accounts(sender);
                return true;
            
            case "test":
                test(sender, args);
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
                if (ChatPointsTTV.getTikTok().started) {
                    available.add("stop");
                    available.add("link");
                    available.add("accounts");
                    if (ChatPointsTTV.getTikTok().accountConnected) {
                        available.add("test");
                        available.add("accounts");  
                        available.add("unlink");
                    }
                } else {
                    available.add("start");
                }
                break;
            
            case 2:
                if (ChatPointsTTV.getTikTok().started) {
                    if (args[0].equalsIgnoreCase("link")) {
                        available.add("<TikTok Username>");
                    } else if (args[0].equalsIgnoreCase("unlink") && ChatPointsTTV.getTikTok().accountConnected) {
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
                if (ChatPointsTTV.getTikTok().started) {
                    if (args[0].equalsIgnoreCase("test")) {
                        if (args[1].equalsIgnoreCase("follow") || args[1].equalsIgnoreCase("gift") || args[1].equalsIgnoreCase("like") || args[1].equalsIgnoreCase("share")) {
                            available.add("<Chatter Username>");
                        }
                    }
                }
                break;

            case 4:
                if (ChatPointsTTV.getTikTok().started) {
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
                if (ChatPointsTTV.getTikTok().started) {
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
                if (ChatPointsTTV.getTikTok().started) {
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

    public void help(CommandSender p) {
        p.spigot().sendMessage(helpMsg);

        if (!p.equals(Bukkit.getConsoleSender())){
            TextComponent docsTip = new TextComponent("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "\nTip: " + ChatColor.RESET + ChatColor.GRAY + "Check out ");

            TextComponent link = new TextComponent("" + ChatColor.GRAY  + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + "ChatPointsTTV's website");
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gosdev.me/chatpointsttv/commands/tiktok"));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));
            docsTip.addExtra(link);
            docsTip.addExtra(ChatColor.GRAY + " for more information on its commands!");
            
            p.spigot().sendMessage(docsTip);
        }
    }

    private void accounts(CommandSender p) {
        java.util.List<String> accounts = ChatPointsTTV.getAccountsManager().getAccounts(Platforms.TIKTOK);
        
        if (!ChatPointsTTV.getTikTok().started) {
            p.sendMessage(ChatColor.RED + "You must start the TikTok Module first!");
            return;
        }

        TextComponent msg = new TextComponent("\n---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Connected TikTok LIVEs" + ChatColor.RESET + " ----------\n\n");
        
        if (p.equals(Bukkit.getConsoleSender())) {
            for (String account : accounts) {
                if (account.isBlank()) continue;
                msg.addExtra(ChatColor.GRAY + "  -  " + account + "\n");
            }
        } else {
            for (String account : accounts) {
                if (account.isBlank()) continue;
                BaseComponent deleteButton = new ComponentBuilder(ChatColor.RED + "  [❌]").create()[0];
                deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink this LIVE").create()));
                deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tiktok unlink " + account));
                msg.addExtra(deleteButton);
                msg.addExtra(new TextComponent("  " + account + "\n"));
            }
        }

        if (accounts.isEmpty()) {
            msg.addExtra(ChatColor.GRAY + "  There are no connected LIVEs :(\n");
        }

        TextComponent footer;
        if (p.equals(Bukkit.getConsoleSender())) {
            footer = new TextComponent(ChatColor.ITALIC + "\nTo unlink a LIVE, use /tiktok unlink <username>\nTo add a LIVE, use /tiktok link <username>");
        } else {
            footer = TikTokButtonComponents.accountLink();
            if (!accounts.isEmpty()) {
                footer.addExtra(ChatColor.GRAY + "  -  ");
                footer.addExtra(TikTokButtonComponents.accountUnlink());
            }
        }
        
        msg.addExtra(footer);
        msg.addExtra("\n");
        ((org.bukkit.entity.Player) p).spigot().sendMessage(msg);
    }

    private void displayStatus(CommandSender p, ChatPointsTTV plugin) {
        String strChannels = "";
        
        if (ChatPointsTTV.getTikTok().getClients() == null || ChatPointsTTV.getTikTok().getClients().isEmpty()) {
            strChannels = "None";
        } else {
            for (String profile : ChatPointsTTV.getTikTok().getClients().keySet()) {
                strChannels += profile + ", ";
            }
            strChannels = strChannels.subSequence(0, strChannels.length() - 2).toString();
        }

        BaseComponent msg = new ComponentBuilder(
            "---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ChatPointsTTV TikTok Status" + ChatColor.RESET + " ----------\n" +
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" + plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened LIVEs: " + ChatColor.RESET + strChannels + "\n" + 
            "\n"
        ).create()[0];

        String currentState = "";
        if (ChatPointsTTV.getTikTok().started) {
            if (ChatPointsTTV.getTikTok().accountConnected) {
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
            if (ChatPointsTTV.getTikTok().started) {
                msg.addExtra(TikTokButtonComponents.manageAccounts());
                msg.addExtra(ChatColor.GRAY + "  -  ");
                msg.addExtra(TikTokButtonComponents.clientStop());
            } else {
                msg.addExtra(TikTokButtonComponents.clientStart());
            }
        }

        ((org.bukkit.entity.Player) p).spigot().sendMessage(msg);
    }

    private void test(CommandSender sender, String[] cmdInput) {
        if (cmdInput.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /tiktok test <type> ...");
            return;
        }

        LiveClient c = null;
        io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent event;
        String chatter = cmdInput[2];
        Boolean offlineTest = false;

        if (!ChatPointsTTV.getTikTok().started) {
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
                        ChatPointsTTV.getTikTok().getEventHandler().onLike((io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent) event, cmdInput[3].toLowerCase());
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
                    ChatPointsTTV.getTikTok().getEventHandler().onShare((io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent) event, cmdInput[3].toLowerCase());
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
