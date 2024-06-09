package com.gospelbg.chatpointsttv;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.pubsub.domain.ChannelBitsData;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.domain.SubGiftData;
import com.github.twitch4j.pubsub.domain.SubscriptionData;
import com.github.twitch4j.pubsub.events.ChannelBitsEvent;
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent;
import com.github.twitch4j.pubsub.events.ChannelSubGiftEvent;
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent;
import com.gospelbg.chatpointsttv.ChatPointsTTV.reward_type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;

public class TwitchEventHandler {
    Logger log = ChatPointsTTV.getPlugin().log;

    @EventSubscriber
    public void onChannelPointsRedemption(ChannelPointsRedemptionEvent event) {
        log.info(event.getRedemption().getUser().getDisplayName() + " has redeemed " + event.getRedemption().getReward().getTitle());
        channelPointRewards(event.getRedemption());
    }

    @EventSubscriber
    public void onCheer(ChannelBitsEvent event) {
        log.info(event.getData().getUserName() + " has cheered " +  event.getData().getBitsUsed() + "bits");
        cheerRewards(event.getData());
    }

    @EventSubscriber
    public void onSub(ChannelSubscribeEvent event) {
        log.info(event.getData().getDisplayName() + " has subscribed using a " + event.getData().getSubPlanName() + " sub");
        subRewards(event.getData());
    }

    @EventSubscriber
    public void onGift(ChannelSubGiftEvent event) {
        log.info(event.getData().getDisplayName() + " has gifted " + Integer.toString(event.getData().getCount()) + " subs");
        subGiftRewards(event.getData());
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
                redemption.setStatus("FULFILLED");
            } catch (Exception e) {
                log.warning(e.toString());
                redemption.setStatus("CANCELLED"); // Probably unhandled by Twitch4J
            }
        }
    }

    private void cheerRewards (ChannelBitsData donation) {
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
                if (donation.getBitsUsed() >= i) {
                    Events.displayTitle(donation.getUserName(), custom_string, donation.getBitsUsed() + " bits", title_color, user_color, isBold, null);
                    Events.runAction(ChatPointsTTV.getRewards(reward_type.CHEER).get(Integer.toString(i)).toString());
                    break;
                }
            }
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }

    private void subRewards(SubscriptionData sub) {
        if (ChatPointsTTV.getRewards(reward_type.SUB).containsKey(sub.getSubPlanName())) {
            String custom_string = ChatPointsTTV.getRedemptionStrings().get("SUB_STRING");
            ChatColor title_color = ChatPointsTTV.getChatColors().get("SUB_COLOR");
            ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");
            ChatColor isBold = ChatPointsTTV.getPlugin().config.getBoolean("REWARD_NAME_BOLD") ? ChatColor.BOLD : ChatColor.RESET;
    
            try {
                Events.displayTitle(sub.getDisplayName(), custom_string, sub.getSubPlanName(), title_color, user_color, isBold, null);
                Events.runAction(ChatPointsTTV.getRewards(reward_type.SUB).get(sub.getSubPlanName()).toString());
            } catch (Exception e) {
                log.warning(e.toString());
            }
        }
    }

    private void subGiftRewards(SubGiftData gift) {
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
                if (gift.getCount() >= i) {
                    Events.displayTitle(gift.getDisplayName(), custom_string, gift.getCount() + " " + gift.getTier().toString() + " subs", title_color, user_color, isBold, null);
                    Events.runAction(ChatPointsTTV.getRewards(reward_type.CHEER).get(Integer.toString(i)).toString());
                    break;
                }
            }
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }
}
