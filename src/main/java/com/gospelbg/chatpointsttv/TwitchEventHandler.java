package com.gospelbg.chatpointsttv;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.eventsub.domain.chat.NoticeType;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent;
import com.gospelbg.chatpointsttv.ChatPointsTTV.reward_type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;

public class TwitchEventHandler {
    Logger log = ChatPointsTTV.getPlugin().log;
    Boolean listenForCheers = !ChatPointsTTV.getPlugin().config.getConfigurationSection("CHEER_REWARDS").getKeys(true).isEmpty();
    Boolean listenForSubs = !ChatPointsTTV.getPlugin().config.getConfigurationSection("SUB_REWARDS").getKeys(true).isEmpty();
    Boolean listenForGifts = !ChatPointsTTV.getPlugin().config.getConfigurationSection("GIFT_REWARDS").getKeys(true).isEmpty();

    private Integer ignoreSubs = 0;

    public static String PlanToString(SubscriptionPlan plan) {
        switch (plan.toString()) {
            case "Prime":
                return "Tier 1 (Prime)";
            case "1000":
                return "Tier 1";
            case "2000":
                return "Tier 2";
            case "3000":
                return "Tier 3";
            default:
                return null;
        }
    }

    public static String PlanToConfig(SubscriptionPlan plan) {
        switch (plan.toString()) {
            case "Prime":
                return "TWITCH_PRIME";
            case "1000":
                return "TIER1";
            case "2000":
                return "TIER2";
            case "3000":
                return "TIER3";
            default:
                return null;
        }
    }


    @EventSubscriber
    public void onChannelPointsRedemption(ChannelPointsRedemptionEvent event) {
        log.info(event.getRedemption().getUser().getDisplayName() + " has redeemed " + event.getRedemption().getReward().getTitle());
        channelPointRewards(event.getRedemption());
    }

    public void onCheer(ChannelChatMessageEvent event) {
        if (event.getCheer() == null) return;
        ChatPointsTTV.getPlugin().log.info(event.getChatterUserName() + " cheered " + event.getCheer().getBits() + " bits!");
        cheerRewards(event.getChatterUserName(), event.getCheer().getBits());
    }

    public void onEvent(ChannelChatNotificationEvent event) {
        if (listenForGifts && (event.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT | event.getNoticeType() == NoticeType.SUB_GIFT)) {
            ChatPointsTTV.getPlugin().log.info(event.getChatterUserName() + " gifted a sub!"); 
            if (event.getNoticeType() == NoticeType.SUB_GIFT) {
                subGiftRewards(event.getChatterUserName(), 1, event.getSubGift().getSubTier());
            } else if (event.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT) {
                ignoreSubs += event.getCommunitySubGift().getTotal(); // Multiple sub gifting triggers both events
                subGiftRewards(event.getChatterUserName(), event.getCommunitySubGift().getTotal(), event.getCommunitySubGift().getSubTier());
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
                ChatPointsTTV.getPlugin().log.warning("Couldn't fetch sub type!");
                return;
            }
            subRewards(event.getChatterUserName(), tier);
        }
    }

    private void channelPointRewards(ChannelPointsRedemption redemption) {
        if (ChatPointsTTV.getRewards(reward_type.CHANNEL_POINTS).containsKey(redemption.getReward().getTitle())) {
            String custom_string = ChatPointsTTV.getRedemptionStrings().get("REDEEMED_STRING");
            ChatColor title_color = ChatPointsTTV.getChatColors().get("REWARD_NAME_COLOR");
            ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");
            ChatColor isBold = ChatPointsTTV.getPlugin().config.getBoolean("REWARD_NAME_BOLD") ? ChatColor.BOLD : ChatColor.RESET;

            Events.displayTitle(redemption.getUser().getDisplayName(), custom_string ,redemption.getReward().getTitle(), title_color, user_color, isBold, redemption.getUserInput());

            try {
                if(ChatPointsTTV.getRewards(reward_type.CHANNEL_POINTS).get(redemption.getReward().getTitle()).toString().contains("{TEXT}")) {
                    Events.runAction(ChatPointsTTV.getRewards(reward_type.CHANNEL_POINTS).get(redemption.getReward().getTitle()).toString().replace("{TEXT}", redemption.getUserInput()));
                } else {
                    Events.runAction(ChatPointsTTV.getRewards(reward_type.CHANNEL_POINTS).get(redemption.getReward().getTitle()).toString());
                }
                ChatPointsTTV.getPlugin().updateRedemption(redemption.getReward().getId(), redemption.getId(), RedemptionStatus.FULFILLED);
            } catch (Exception e) {
                log.warning(e.toString());
                ChatPointsTTV.getPlugin().updateRedemption(redemption.getReward().getId(), redemption.getId(), RedemptionStatus.FULFILLED);
            }
        }
    }

    private void cheerRewards (String chatter, Integer amount) {
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("CHEERED_STRING");
        ChatColor title_color = ChatPointsTTV.getChatColors().get("CHEER_COLOR");
        ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");
        ChatColor isBold = ChatPointsTTV.getPlugin().config.getBoolean("REWARD_NAME_BOLD") ? ChatColor.BOLD : ChatColor.RESET;

        List<Integer> rewards = new ArrayList<Integer>();
        ChatPointsTTV.getRewards(reward_type.CHEER).forEach((k, v) -> {
            rewards.add(Integer.parseInt(k));
        });
        Arrays.sort(rewards.toArray(), Collections.reverseOrder()); // Sort the array from highest to lowest.

        try {
            for (Integer i : rewards) {
                if (amount >= i) {
                    Events.displayTitle(chatter, custom_string, amount + " bits", title_color, user_color, isBold, null);
                    Events.runAction(ChatPointsTTV.getRewards(reward_type.CHEER).get(Integer.toString(i)).toString());
                    break;
                }
            }
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }

    private void subRewards(String chatter, SubscriptionPlan tier) {
        if (ChatPointsTTV.getRewards(reward_type.SUB).containsKey(PlanToConfig(tier))) {
            String custom_string = ChatPointsTTV.getRedemptionStrings().get("SUB_STRING");
            ChatColor title_color = ChatPointsTTV.getChatColors().get("SUB_COLOR");
            ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");
            ChatColor isBold = ChatPointsTTV.getPlugin().config.getBoolean("REWARD_NAME_BOLD") ? ChatColor.BOLD : ChatColor.RESET;
        
            try {
                Events.displayTitle(chatter, custom_string, PlanToString(tier), title_color, user_color, isBold, null);
                Events.runAction(ChatPointsTTV.getRewards(reward_type.SUB).get(PlanToConfig(tier)).toString());
            } catch (Exception e) {
                log.warning(e.toString());
            }
        }
    }

    private void subGiftRewards(String chatter, Integer amount, SubscriptionPlan tier) {
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("GIFT_STRING");
        ChatColor title_color = ChatPointsTTV.getChatColors().get("GIFT_COLOR");
        ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");
        ChatColor isBold = ChatPointsTTV.getPlugin().config.getBoolean("REWARD_NAME_BOLD") ? ChatColor.BOLD : ChatColor.RESET;

        List<Integer> rewards = new ArrayList<Integer>();
        ChatPointsTTV.getRewards(reward_type.SUB_GIFT).forEach((k, v) -> {
            rewards.add(Integer.parseInt(k));
        });
        Arrays.sort(rewards.toArray(), Collections.reverseOrder()); // Sort the array from highest to lowest.

        try {
            for (Integer i : rewards) {
                if (amount >= i) {
                    Events.displayTitle(chatter, custom_string, amount + " " + tier.toString() + " subs", title_color, user_color, isBold, null);
                    Events.runAction(ChatPointsTTV.getRewards(reward_type.CHEER).get(Integer.toString(i)).toString());
                    break;
                }
            }
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }
}
