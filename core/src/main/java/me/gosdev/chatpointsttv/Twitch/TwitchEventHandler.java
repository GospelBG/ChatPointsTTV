package me.gosdev.chatpointsttv.Twitch;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events;
import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.TwitchUtils;
import me.gosdev.chatpointsttv.Utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class TwitchEventHandler {
    ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    Utils utils = ChatPointsTTV.getUtils();

    public static Boolean rewardBold;
    Boolean logEvents = plugin.config.getBoolean("LOG_EVENTS");
    ChatColor action_color = ChatPointsTTV.getChatColors().get("ACTION_COLOR");
    ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");

    public void onChannelPointsRedemption(RewardRedeemedEvent event) {
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getRedemption().getUser().getDisplayName() + " has redeemed " + event.getRedemption().getReward().getTitle() + " in " + TwitchUtils.getUsername(event.getRedemption().getChannelId()));
        if (plugin.getTwitch().ignoreOfflineStreamers) {
            for (Channel channel : plugin.getTwitch().getListenedChannels().values()) {
                if (channel.getChannelId().equals(event.getRedemption().getChannelId()) && !channel.isLive()) return; // Return if channel matches and it's offline.
            }
        }
        ChannelPointsRedemption redemption = event.getRedemption();

        String custom_string = ChatPointsTTV.getRedemptionStrings().get("REDEEMED_STRING");
        Events.showIngameAlert(redemption.getUser().getDisplayName(), custom_string, redemption.getReward().getTitle(), action_color, user_color, rewardBold);

        for (Reward reward : Rewards.getRewards(rewardType.CHANNEL_POINTS)) {
            if (!reward.getEvent().equalsIgnoreCase(redemption.getReward().getTitle())) continue;
            if (!reward.getTargetId().equals(redemption.getChannelId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;

            for (String cmd : reward.getCommands()) {
                String[] parts = cmd.split(" ", 2);

                if (parts.length <= 1) {
                    plugin.log.warning("Invalid command: " + parts[0]);
                    continue;
                }

                Events.runAction(parts[0], parts[1].replaceAll("\\{TEXT\\}", redemption.getUserInput()), redemption.getUser().getDisplayName());
            }
            return;
        }
    }

    public void onFollow(ChannelFollowEvent event) {
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getUserName() + " started following " + event.getBroadcasterUserName());
        if (plugin.getTwitch().ignoreOfflineStreamers) {
            for (Channel channel : plugin.getTwitch().getListenedChannels().values()) {
                if (channel.getChannelId().equals(event.getBroadcasterUserId()) && !channel.isLive()) return; // Return if channel matches and it's offline.
            }
        }
        String chatter = event.getUserName();
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("FOLLOWED_STRING");
        Events.showIngameAlert(chatter, custom_string, "", action_color, user_color, rewardBold);
        for (Reward reward : Rewards.getRewards(rewardType.FOLLOW)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;

            for (String cmd : reward.getCommands()) {
                String[] parts = cmd.split(" ", 2);

                if (parts.length <= 1) {
                    plugin.log.warning("Invalid command: " + parts[0]);
                    continue;
                }

                Events.runAction(parts[0], parts[1], event.getUserName());
            }
            return;    
        }
    }

    public void onCheer(ChannelChatMessageEvent event) {
        if (event.getCheer() == null) return;
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getChatterUserName() + " cheered " + event.getCheer().getBits() + " bits to " + event.getBroadcasterUserName() + "!");
        if (plugin.getTwitch().ignoreOfflineStreamers) {
            for (Channel channel : plugin.getTwitch().getListenedChannels().values()) {
                if (channel.getChannelId().equals(event.getBroadcasterUserId()) && !channel.isLive()) return; // Return if channel matches and it's offline.
            }
        }

        String chatter = event.getChatterUserName();
        int amount = event.getCheer().getBits();
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("CHEERED_STRING");

        Events.showIngameAlert(chatter, custom_string, amount + " bits", action_color, user_color, rewardBold);

        ArrayList<Reward> rewards = Rewards.getRewards(rewardType.CHEER);
        for (Reward reward : rewards) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;
            try {
                if (amount >= Integer.parseInt(reward.getEvent())) {
                    for (String cmd : reward.getCommands()) {
                        String[] parts = cmd.split(" ", 2);
    
                        if (parts.length <= 1) {
                            plugin.log.warning("Invalid command: " + parts[0]);
                            continue;
                        }
    
                        Events.runAction(parts[0], parts[1].replaceAll("\\{AMOUNT\\}", String.valueOf(amount)), event.getChatterUserName());
                    }
                    return;
                }
    
            } catch (NumberFormatException e) {
                plugin.log.warning("Invalid cheer amount: " + reward.getEvent());
                return;
            }
        }
    }

    public void onSub(ChannelChatNotificationEvent event) {
        String chatter = event.getChatterUserName();
        SubscriptionPlan tier;

        switch (event.getNoticeType()) {
            case SUB:
                if (event.getSub().isPrime()) tier = SubscriptionPlan.TWITCH_PRIME;
                else tier = event.getSub().getSubTier();
                break;
            case RESUB:
                if (event.getResub().isPrime()) tier = SubscriptionPlan.TWITCH_PRIME;
                else tier = event.getResub().getSubTier();
                break;
            default:
                plugin.log.warning("Couldn't fetch sub type!");
                return;
            
        }

        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getChatterUserName() + " has subscribed to " + event.getBroadcasterUserName() + " with a " + TwitchUtils.PlanToString(tier) + " sub!"); 
        if (plugin.getTwitch().ignoreOfflineStreamers) {
            for (Channel channel : plugin.getTwitch().getListenedChannels().values()) {
                if (channel.getChannelId().equals(event.getBroadcasterUserId()) && !channel.isLive()) return; // Return if channel matches and it's offline.
            }
        }

        String custom_string = ChatPointsTTV.getRedemptionStrings().get("SUB_STRING");
        Events.showIngameAlert(chatter, custom_string,TwitchUtils.PlanToString(tier) + " sub", action_color, user_color, rewardBold);

        for (Reward reward : Rewards.getRewards(rewardType.SUB)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;

            if (reward.getEvent().equals(TwitchUtils.PlanToConfig(tier))) {
                for (String cmd : reward.getCommands()) {
                    String[] parts = cmd.split(" ", 2);

                    if (parts.length <= 1) {
                        plugin.log.warning("Invalid command: " + parts[0]);
                        continue;
                    }

                    Events.runAction(parts[0], parts[1], event.getChatterUserName());
                }
                return;
            }
        }
    }

    public void onSubGift(ChannelChatNotificationEvent event) {
        String chatter = event.getChatterUserName();
        int amount = event.getCommunitySubGift().getTotal();
        String tier = TwitchUtils.PlanToString(event.getCommunitySubGift().getSubTier());

        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getChatterUserName() + " has gifted " + amount  + " " + tier + " subs in " + event.getBroadcasterUserName() + "'s' channel!"); 
        if (plugin.getTwitch().ignoreOfflineStreamers) {
            for (Channel channel : plugin.getTwitch().getListenedChannels().values()) {
                if (channel.getChannelId().equals(event.getBroadcasterUserId()) && !channel.isLive()) return; // Return if channel matches and it's offline.
            }
        }
        
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("GIFT_STRING");
        ArrayList<Reward> rewards = Rewards.getRewards(rewardType.GIFT);

        Events.showIngameAlert(chatter, custom_string, tier, action_color, user_color, rewardBold);
        
        for (Reward reward : rewards) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;
            if (amount >= Integer.valueOf(reward.getEvent())) {
                for (String cmd : reward.getCommands()) {
                    String[] parts = cmd.split(" ", 2);
    
                    if (parts.length <= 1) {
                        plugin.log.warning("Invalid command: " + parts[0]);
                        continue;
                    }
    
                    Events.runAction(parts[0], parts[1].replaceAll("\\{AMOUNT\\}", String.valueOf(amount)), event.getChatterUserName());                
                }
                return;
            }
        }
    }

    public void onRaid(ChannelRaidEvent event) {
        String raiderName = event.getFromBroadcasterUserName();
        Integer amount = event.getViewers();

        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), raiderName + " has raided " + event.getToBroadcasterUserName()  + " with a viewer count of " + amount + "!"); 
        if (plugin.getTwitch().ignoreOfflineStreamers) {
            for (Channel channel : plugin.getTwitch().getListenedChannels().values()) {
                if (channel.getChannelId().equals(event.getFromBroadcasterUserId()) && !channel.isLive()) return; // Return if channel matches and it's offline.
            }
        }

        String custom_string = ChatPointsTTV.getRedemptionStrings().get("RAIDED_STRING").replace("{CHANNEL}", event.getToBroadcasterUserName());
        ArrayList<Reward> rewards = Rewards.getRewards(rewardType.RAID);


        Events.showIngameAlert(raiderName, custom_string, amount.toString(), action_color, user_color, rewardBold);

        for (Reward reward : rewards) {
            if (!reward.getTargetId().equals(event.getToBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;
            if (amount >= Integer.valueOf(reward.getEvent())) {
                for (String cmd : reward.getCommands()) {
                    String[] parts = cmd.split(" ", 2);
    
                    if (parts.length <= 1) {
                        plugin.log.warning("Invalid command: " + parts[0]);
                        continue;
                    }
    
                    Events.runAction(parts[0], parts[1].replaceAll("\\{AMOUNT\\}", String.valueOf(amount)), raiderName);                
                }
                return;
            }
        }
    }
}
