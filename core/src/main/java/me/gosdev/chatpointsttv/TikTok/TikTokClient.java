package me.gosdev.chatpointsttv.TikTok;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TikTokClient {
    private Boolean accountConnected = false;

    private TikTokEventHandler eventHandler = new TikTokEventHandler();

    private static ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    private static Utils utils = ChatPointsTTV.getUtils();
    private static LiveClient client;

    public static LiveClient getClient() {
        return client;
    }
    public Boolean isAccountConected() {
        return accountConnected;
    }

    public void link(CommandSender p) {
        LiveClientBuilder builder = TikTokLive.newClient(plugin.config.getString("TIKTOK_CHANNEL_USERNAME"));
        if (Rewards.getRewards(rewardType.TIKTOK_GIFT) != null) {
            builder.onGiftCombo((liveClient, event) -> {
                eventHandler.onGift(event);
            });
        }
        if (Rewards.getRewards(rewardType.TIKTOK_FOLLOW) != null) {
            builder.onFollow((liveClient, event) -> {
                eventHandler.onFollow(event);
            });
        }
        if (Rewards.getRewards(rewardType.TIKTOK_SHARE) != null) {
            builder.onShare((liveClient, event) -> {
                eventHandler.onShare(event);
            });
        }
        if (plugin.config.getBoolean("SHOW_CHAT")) {
            builder.onComment((liveClient, event) -> {
                if (!plugin.chatBlacklist.contains(event.getUser().getName())) {
                    BaseComponent[] components = new BaseComponent[] {
                        new ComponentBuilder(ChatColor.DARK_PURPLE + event.getUser().getProfileName() + ": ").create()[0],
                        new ComponentBuilder(event.getText()).create()[0]
                    };
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission(permissions.BROADCAST.permission_id)) {
                            utils.sendMessage(player, components);
                        }
                    }
                }
            });
        }

        client = builder.build();
        client.connectAsync().whenComplete((LiveClient client, Throwable ex) -> {
            accountConnected = true;
            utils.sendMessage(p, "TikTok connection done!");
        });
    }

    public void unlink(CommandSender p) {
        client.disconnect();
        accountConnected = false;
        p.sendMessage(ChatColor.GREEN + "TikTok disconnected successfully!");

    }
    
}
