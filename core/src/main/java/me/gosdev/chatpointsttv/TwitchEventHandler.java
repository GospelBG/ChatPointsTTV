package me.gosdev.chatpointsttv;

import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.eventsub.domain.chat.NoticeType;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.RewardComparator;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.ChatColor;

public class TwitchEventHandler {
    ChatPointsTTV plugin = ChatPointsTTV.getPlugin();


    public static Boolean rewardBold;
    Boolean listenForCheers = !plugin.config.getConfigurationSection("CHEER_REWARDS").getKeys(true).isEmpty();
    Boolean listenForSubs = !plugin.config.getConfigurationSection("SUB_REWARDS").getKeys(true).isEmpty();
    Boolean listenForGifts = !plugin.config.getConfigurationSection("GIFT_REWARDS").getKeys(true).isEmpty();
    Boolean logEvents = plugin.config.getBoolean("LOG_EVENTS");
    ChatColor action_color = ChatPointsTTV.getChatColors().get("ACTION_COLOR");
    ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");

    private Integer ignoreSubs = 0;

    public void onChannelPointsRedemption(RewardRedeemedEvent event) {
        if (event.getRedemption().getStatus() == RedemptionStatus.UNFULFILLED.toString()) return;
        if (logEvents) plugin.log.info(event.getRedemption().getUser().getDisplayName() + " has redeemed " + event.getRedemption().getReward().getTitle());
        ChannelPointsRedemption redemption = event.getRedemption();
        for (Reward reward : Rewards.getRewards(rewardType.CHANNEL_POINTS)) {
            if (!reward.getEvent().equalsIgnoreCase(redemption.getReward().getTitle())) continue;
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
            plugin.updateRedemption(redemption, RedemptionStatus.FULFILLED);
        }
        //redemption.setStatus("FULFILLED");
        
    }

    public void onFollow(FollowEvent event) {
        if (logEvents) plugin.log.info(event.getUser() + " started following you");
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("FOLLOW_STRING");
        Events.displayTitle(event.getUser().getName(), custom_string, "", action_color, user_color, rewardBold);
        for (String cmd : Rewards.getRewards(rewardType.FOLLOW).get(0).getCommands()) {
            String[] parts = cmd.split(" ", 2);
            try {
                Events.runAction(parts[0], parts[1], event.getUser().getName());
            } catch (Exception e) {
                plugin.log.warning(e.toString());
            }
            
        }
    }

    public void onCheer(ChannelChatMessageEvent event) {
        if (event.getCheer() == null) return;
        if (logEvents) plugin.log.info(event.getChatterUserName() + " cheered " + event.getCheer().getBits() + " bits!");

        String chatter = event.getChatterUserName();
        int amount = event.getCheer().getBits();
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("CHEERED_STRING");

        // Sort rewards by cheer.
        ArrayList<Reward> rewards = Rewards.getRewards(rewardType.CHEER);
        Collections.sort(rewards, new RewardComparator());

        for (Reward i : rewards) {
            ChatPointsTTV.getPlugin().log.info(i.getEvent());
            if (amount >= Integer.parseInt(i.getEvent())) {
                Events.displayTitle(chatter, custom_string, amount + " bits", action_color, user_color, rewardBold);
                for (String cmd : i.getCommands()) {
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

    public void onEvent(ChannelChatNotificationEvent event) {
        if (listenForGifts && (event.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT | event.getNoticeType() == NoticeType.SUB_GIFT)) {
            if (logEvents) plugin.log.info(event.getChatterUserName() + " gifted a sub!"); 

            int amount = 1;
            String chatter = event.getChatterUserName();
            String tier;
            
            ChatPointsTTV.getPlugin().log.info(event.getNoticeType().toString());
            //ignoreSubs += event.getSubGift().getCumulativeTotal(); // Multiple sub gifting triggers both events
            amount = event.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT ? event.getCommunitySubGift().getCumulativeTotal() : event.getSubGift().getCumulativeTotal();
            tier = event.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT ? ChatPointsTTV.utils.PlanToString(event.getCommunitySubGift().getSubTier()) : ChatPointsTTV.utils.PlanToString(event.getSubGift().getSubTier());


            String custom_string = ChatPointsTTV.getRedemptionStrings().get("GIFT_STRING");            
            ArrayList<Reward> rewards = Rewards.getRewards(rewardType.GIFT);
            Collections.sort(rewards, Collections.reverseOrder());
            
            for (Reward i : rewards) {
                if (amount >= Integer.parseInt(i.getEvent())) {
                    Events.displayTitle(chatter, custom_string, amount + " " + tier + " subs", action_color, user_color, rewardBold);
                    for (String cmd : i.getCommands()) {
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
        } else if (listenForSubs && (event.getNoticeType() == NoticeType.SUB | event.getNoticeType() == NoticeType.RESUB)) {
            SubscriptionPlan tier;
            if (ignoreSubs > 0) {
                ignoreSubs -= 1;
                return;
            }

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

            if (logEvents) plugin.log.info(event.getChatterUserName() + " subscribed with a " + ChatPointsTTV.utils.PlanToString(tier) + " sub!");
            for (Reward i : Rewards.getRewards(rewardType.SUB)) {
                if (i.getEvent().equals(ChatPointsTTV.utils.PlanToConfig(tier))) {
                    String custom_string = ChatPointsTTV.getRedemptionStrings().get("SUB_STRING");
                    
                    Events.displayTitle(event.getChatterUserName(), custom_string, ChatPointsTTV.utils.PlanToString(tier), action_color, user_color, rewardBold);
                    for (String cmd : i.getCommands()) {
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
    }
}
