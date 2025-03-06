package me.gosdev.chatpointsttv.Tests;

import java.time.Instant;
import java.util.Optional;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import static com.github.twitch4j.common.util.TypeConvert.jsonToObject;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.domain.ChannelPointsReward;
import com.github.twitch4j.pubsub.domain.ChannelPointsUser;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Utils.TwitchUtils;

public class EventTest {
    
    
    @SuppressWarnings("deprecation")
    public static RewardRedeemedEvent ChannelPointsRedemptionEvent(String channel, String chatter, String title, Optional<String> userInput) { //TODO: Move away from PubSub
        ChannelPointsRedemption redemption = new ChannelPointsRedemption();
        ChannelPointsReward reward = new ChannelPointsReward();
        ChannelPointsUser redeemer = new ChannelPointsUser();
        redeemer.setDisplayName(chatter);

        reward.setTitle(title);

        redemption.setChannelId(TwitchUtils.getUserId(channel));
        redemption.setUser(redeemer);
        if (userInput.isPresent()) {
            redemption.setUserInput(userInput.get());
        }

        redemption.setReward(reward);

        return new RewardRedeemedEvent(Instant.now(), redemption);
    }

    public static ChannelFollowEvent FollowEvent(String channel, String chatter) {
        ChannelFollowEvent event = jsonToObject(
            "{\"user_id\":\"" + TwitchUtils.getUserId(chatter) +
            "\",\"user_login\":\"" + chatter.toLowerCase() +
            "\",\"user_name\":\"" + chatter +
            "\",\"broadcaster_user_id\":\"" + TwitchUtils.getUserId(channel) +
            "\",\"broadcaster_user_login\":\"" + channel.toLowerCase() +
            "\",\"broadcaster_user_name\":\"" + channel +
            "\",\"followed_at\":\"" + Instant.now().toString() +"\"}",
        ChannelFollowEvent.class);

        return event;
    }

    public static ChannelChatMessageEvent CheerEvent(String channel, String chatter, int amount) {
        ChannelChatMessageEvent event = jsonToObject(
            "{\"chatter_user_id\":\"" + TwitchUtils.getUserId(chatter) +
            "\",\"chatter_user_login\":\"" + chatter.toLowerCase() +
            "\",\"chatter_user_name\":\"" + chatter +
            "\",\"broadcaster_user_id\":\"" + TwitchUtils.getUserId(channel) +
            "\",\"broadcaster_user_login\":\"" + channel.toLowerCase() +
            "\",\"broadcaster_user_name\":\"" + channel +
            "\",\"cheer\": {\"bits\": " + amount +"}}", 
        ChannelChatMessageEvent.class);
        
        return event;
    }

    public static ChannelChatNotificationEvent SubEvent(String channel, String chatter, SubscriptionPlan plan, int months) {
        ChannelChatNotificationEvent event = jsonToObject(
            "{\"chatter_user_id\":\"" + TwitchUtils.getUserId(chatter) +
            "\",\"chatter_user_login\":\"" + chatter.toLowerCase() +
            "\",\"chatter_user_name\":\"" + chatter +
            "\",\"broadcaster_user_id\":\"" + TwitchUtils.getUserId(channel) +
            "\",\"broadcaster_user_login\":\"" + channel.toLowerCase() +
            "\",\"broadcaster_user_name\":\"" + channel +
            "\",\"notice_type\": \"sub" +
            "\",\"sub\":{\"sub_tier\":\"" + (plan.equals(SubscriptionPlan.TWITCH_PRIME) ? "1000" : plan.toString()) + 
            "\",\"is_prime\":" + (plan.equals(SubscriptionPlan.TWITCH_PRIME) ? "true" : "false") +
            ",\"duration_months\":" + months + "}}",
            ChannelChatNotificationEvent.class);

        ChatPointsTTV.getPlugin().log.info(plan.toString());

        return event;
            }

    public static ChannelChatNotificationEvent ResubEvent(String channel, String chatter, SubscriptionPlan plan, int months) {
        ChannelChatNotificationEvent event = jsonToObject(
            "{\"chatter_user_id\":\"" + TwitchUtils.getUserId(chatter) +
            "\",\"chatter_user_login\":\"" + chatter.toLowerCase() +
            "\",\"chatter_user_name\":\"" + chatter +
            "\",\"broadcaster_user_id\":\"" + TwitchUtils.getUserId(channel) +
            "\",\"broadcaster_user_login\":\"" + channel.toLowerCase() +
            "\",\"broadcaster_user_name\":\"" + channel +
            "\",\"notice_type\": \"sub\"" +
            "\",resub\":{\"sub_tier\":\"" + plan.name() + 
            "\",\"is_prime\":" + (plan.equals(SubscriptionPlan.TWITCH_PRIME) ? "true" : "false") +
            "\",is_gift\": \"false\"" +
            "\",duration_months\":" + months + 
            "\",cumulative_months\":" + months + 
            "\",streak_months\":" + months + "}}",
            ChannelChatNotificationEvent.class);

            return event;
            }

    public static ChannelChatNotificationEvent SubGiftEvent(String channel, String chatter, SubscriptionPlan plan, int amount) {
        ChannelChatNotificationEvent event = jsonToObject(
            "{\"chatter_user_id\":\"" + TwitchUtils.getUserId(chatter) +
            "\",\"chatter_user_login\":\"" + chatter.toLowerCase() +
            "\",\"chatter_user_name\":\"" + chatter +
            "\",\"broadcaster_user_id\":\"" + TwitchUtils.getUserId(channel) +
            "\",\"broadcaster_user_login\":\"" + channel.toLowerCase() +
            "\",\"broadcaster_user_name\":\"" + channel +
            "\",\"notice_type\": \"community_sub_gift" +
            "\",\"community_sub_gift\":{\"sub_tier\":\"" + plan + 
            "\",\"total\":" + amount + "}}",
            ChannelChatNotificationEvent.class);

            return event;
    }

    public static ChannelRaidEvent RaidReward(String channel, String raider, int viewers) {
        ChannelRaidEvent event = jsonToObject(
            "{\"from_broadcaster_user_id\":\"" + TwitchUtils.getUserId(raider) +
            "\",\"from_broadcaster_user_login\":\"" + raider.toLowerCase() +
            "\",\"from_broadcaster_user_name\":\"" + raider +
            "\",\"to_broadcaster_user_id\":\"" + TwitchUtils.getUserId(channel) +
            "\",\"to_broadcaster_user_login\":\"" + channel.toLowerCase() +
            "\",\"to_broadcaster_user_name\":\"" + channel +
            "\",\"viewers\": \"" + viewers + "\"}",
            ChannelRaidEvent.class);

        return event;
    }
}
