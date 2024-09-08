package me.gosdev.chatpointsttv;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.domain.chat.NoticeType;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.RewardComparator;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class TwitchEventHandler {
    ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    Utils utils = ChatPointsTTV.getUtils();

    public static Boolean rewardBold;
    Boolean listenForCheers = !plugin.config.getConfigurationSection("CHEER_REWARDS").getKeys(true).isEmpty();
    Boolean listenForSubs = !plugin.config.getConfigurationSection("SUB_REWARDS").getKeys(true).isEmpty();
    Boolean listenForGifts = !plugin.config.getConfigurationSection("GIFT_REWARDS").getKeys(true).isEmpty();
    Boolean logEvents = plugin.config.getBoolean("LOG_EVENTS");
    ChatColor action_color = ChatPointsTTV.getChatColors().get("ACTION_COLOR");
    ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");

    public void onChannelPointsRedemption(RewardRedeemedEvent event) {
        
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getRedemption().getUser().getDisplayName() + " has redeemed " + event.getRedemption().getReward().getTitle() + " in " + plugin.getUsername(event.getRedemption().getChannelId()));
        ChannelPointsRedemption redemption = event.getRedemption();
        for (Reward reward : Rewards.getRewards(rewardType.CHANNEL_POINTS)) {
            if (!reward.getEvent().equalsIgnoreCase(redemption.getReward().getTitle())) continue;
            if (!reward.getTargetId().equals(redemption.getChannelId())) continue;

            String custom_string = ChatPointsTTV.getRedemptionStrings().get("REDEEMED_STRING");
            Events.displayTitle(redemption.getUser().getDisplayName(), custom_string, redemption.getReward().getTitle(), action_color, user_color, rewardBold);
            for (String cmd : reward.getCommands()) {
                String[] parts = cmd.split(" ", 2);
                try {
                    Events.runAction(parts[0], parts[1].replaceAll("\\{TEXT\\}", redemption.getUserInput()), redemption.getUser().getDisplayName());
                } catch (Exception e) {
                    plugin.log.warning(e.toString());
                }
            }
        }
    }

    public void onFollow(ChannelFollowEvent event) {
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getUserName() + " started following " + event.getBroadcasterUserName());
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("FOLLOWED_STRING");
        Events.displayTitle(event.getUserName(), custom_string, "", action_color, user_color, rewardBold);
        for (Reward reward : Rewards.getRewards(rewardType.FOLLOW)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId())) continue;

            for (String cmd : reward.getCommands()) {
                String[] parts = cmd.split(" ", 2);
                try {
                    Events.runAction(parts[0], parts[1], event.getUserName());
                } catch (Exception e) {
                    plugin.log.warning(e.toString());
                }   
            }           
        }
    }

    public void onCheer(ChannelChatMessageEvent event) {
        if (event.getCheer() == null) return;
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getChatterUserName() + " cheered " + event.getCheer().getBits() + " bits to " + event.getBroadcasterUserName() + "!");

        String chatter = event.getChatterUserName();
        int amount = event.getCheer().getBits();
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("CHEERED_STRING");

        // Sort rewards by cheer.
        ArrayList<Reward> rewards = Rewards.getRewards(rewardType.CHEER);
        Collections.sort(rewards, new RewardComparator());

        for (Reward reward : rewards) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId())) continue;

            if (amount >= Integer.parseInt(reward.getEvent())) {
                Events.displayTitle(chatter, custom_string, amount + " bits", action_color, user_color, rewardBold);
                for (String cmd : reward.getCommands()) {
                    String[] parts = cmd.split(" ", 2);
                    try {
                    Events.runAction(parts[0], parts[1], event.getChatterUserName());
                    } catch (Exception e) {
                        plugin.log.warning(e.toString());
                    }
                }
                break;
            }
        } 
    }

    public void onSub(ChannelChatNotificationEvent event) {
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getChatterUserName() + " has subscribed to " + event.getBroadcasterUserName() + " with a " + event.getSub().getSubTier().toString() + " sub!"); 
        
        SubscriptionPlan tier;

        if (event.getNoticeType() == NoticeType.SUB) {
            if (event.getSub().isPrime()) tier = SubscriptionPlan.TWITCH_PRIME;
            else tier = event.getSub().getSubTier();

        } else if (event.getNoticeType() == NoticeType.RESUB) {
            if (event.getResub().isPrime()) tier = SubscriptionPlan.TWITCH_PRIME;
            else tier = event.getResub().getSubTier();

        } else {
            plugin.log.warning("Couldn't fetch sub type!");
            return;
        }

        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getChatterUserName() + " subscribed with a " + ChatPointsTTV.getUtils().PlanToString(tier) + " sub to " + event.getBroadcasterUserName() + "!");
        for (Reward reward : Rewards.getRewards(rewardType.SUB)) {

            if (reward.getEvent().equals(ChatPointsTTV.getUtils().PlanToConfig(tier))) {
                String custom_string = ChatPointsTTV.getRedemptionStrings().get("SUB_STRING");
                
                Events.displayTitle(event.getChatterUserName(), custom_string, ChatPointsTTV.getUtils().PlanToString(tier), action_color, user_color, rewardBold);
                for (String cmd : reward.getCommands()) {
                    String[] parts = cmd.split(" ", 2);
                    try {
                    Events.runAction(parts[0], parts[1], event.getChatterUserName());
                    } catch (Exception e) {
                        plugin.log.warning(e.toString());
                    }
                }
            }
        }
    }

    public void onSubGift(ChannelChatNotificationEvent event) {
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getChatterUserName() + " has gifted  " + event.getSubGift().getCumulativeTotal().toString() + " subs in " + event.getBroadcasterUserName() + "'s' channel!"); 
        
        String chatter = event.getChatterUserName();
        Integer amount = event.getSubGift().getCumulativeTotal();
        String tier = ChatPointsTTV.getUtils().PlanToString(event.getSubGift().getSubTier()); // event.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT ? ChatPointsTTV.getUtils().PlanToString(event.getCommunitySubGift().getSubTier()) : 


        String custom_string = ChatPointsTTV.getRedemptionStrings().get("GIFT_STRING");            
        ArrayList<Reward> rewards = Rewards.getRewards(rewardType.GIFT);
        Collections.sort(rewards, Collections.reverseOrder());

        Events.displayTitle(chatter, custom_string, amount + " " + tier + " subs", action_color, user_color, rewardBold);
        
        for (Reward reward : rewards) {
            for (String cmd : reward.getCommands()) {
                String[] parts = cmd.split(" ", 2);
                try {
                Events.runAction(parts[0], parts[1], event.getChatterUserName());
                } catch (Exception e) {
                    plugin.log.warning(e.toString());
                }
            }
            break;
        }
    }
}
