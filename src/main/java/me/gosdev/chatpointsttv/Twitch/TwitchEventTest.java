package me.gosdev.chatpointsttv.Twitch;

import java.time.Instant;
import java.util.Optional;

import org.json.JSONObject;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import static com.github.twitch4j.common.util.TypeConvert.jsonToObject;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;

public class TwitchEventTest {
    public static CustomRewardRedemptionAddEvent ChannelPointsRedemptionEvent(String channel, String chatter, String title, Optional<String> userInput) {
        JSONObject json = new JSONObject();
        JSONObject reward = new JSONObject();

        json.put("user_id", TwitchUtils.getUserId(chatter));
        json.put("user_login", chatter.toLowerCase());
        json.put("user_name", chatter);

        json.put("broadcaster_user_id", TwitchUtils.getUserId(channel));
        json.put("broadcaster_user_login", channel.toLowerCase());
        json.put("broadcaster_user_name", channel);
        
        json.put("user_input", userInput.orElse(""));
        json.put("status", "unfullfilled");
        reward.put("id", "0");
        reward.put("title", title);
        reward.put("cost", "123");

        json.put("reward", reward);

        CustomRewardRedemptionAddEvent event = jsonToObject(json.toString(), CustomRewardRedemptionAddEvent.class);
        return event;
    }

    public static ChannelFollowEvent FollowEvent(String channel, String chatter) {
        JSONObject json = new JSONObject();

        json.put("user_id", TwitchUtils.getUserId(chatter));
        json.put("user_login", chatter.toLowerCase());
        json.put("user_name", chatter);

        json.put("broadcaster_user_id", TwitchUtils.getUserId(channel));
        json.put("broadcaster_user_login", channel.toLowerCase());
        json.put("broadcaster_user_name", channel);

        json.put("followed_at", Instant.ofEpochMilli(0).toString());

        ChannelFollowEvent event = jsonToObject(json.toString(), ChannelFollowEvent.class);
        return event;
    }

    public static ChannelChatMessageEvent CheerEvent(String channel, String chatter, Integer amount) {
        JSONObject json = new JSONObject();

        json.put("chatter_user_id", TwitchUtils.getUserId(chatter));
        json.put("chatter_user_login", chatter.toLowerCase());
        json.put("chatter_user_name", chatter);

        json.put("broadcaster_user_id", TwitchUtils.getUserId(channel));
        json.put("broadcaster_user_login", channel.toLowerCase());
        json.put("broadcaster_user_name", channel);

        json.put("cheer", new JSONObject().put("bits", amount.toString()));

        ChannelChatMessageEvent event = jsonToObject(json.toString(), ChannelChatMessageEvent.class);
        return event;
    }

    public static ChannelChatNotificationEvent SubEvent(String channel, String chatter, SubscriptionPlan plan) {
        JSONObject json = new JSONObject();
        JSONObject sub = new JSONObject();

        json.put("chatter_user_id", TwitchUtils.getUserId(chatter));
        json.put("chatter_user_login", chatter.toLowerCase());
        json.put("chatter_user_name", chatter);

        json.put("broadcaster_user_id", TwitchUtils.getUserId(channel));
        json.put("broadcaster_user_login", channel.toLowerCase());
        json.put("broadcaster_user_name", channel);

        json.put("notice_type", "sub");

        sub.put("sub_tier", plan.equals(SubscriptionPlan.TWITCH_PRIME) ? "1000" : plan.toString());
        sub.put("is_prime", plan.equals(SubscriptionPlan.TWITCH_PRIME) ? "true" : "false");
        sub.put("duration_months", "1");

        json.put("sub", sub);

        ChannelChatNotificationEvent event = jsonToObject(json.toString(), ChannelChatNotificationEvent.class);
        return event;
    }

    public static ChannelChatNotificationEvent SubGiftEvent(String channel, String chatter, Integer amount) {
        JSONObject json = new JSONObject();
        JSONObject sub = new JSONObject();


        json.put("chatter_user_id", TwitchUtils.getUserId(chatter));
        json.put("chatter_user_login", chatter.toLowerCase());
        json.put("chatter_user_name", chatter);

        json.put("broadcaster_user_id", TwitchUtils.getUserId(channel));
        json.put("broadcaster_user_login", channel.toLowerCase());
        json.put("broadcaster_user_name", channel);

        json.put("notice_type", "community_sub_gift");

        sub.put("sub_tier", "1000");
        sub.put("total", amount);

        json.put("community_sub_gift", sub);

        ChannelChatNotificationEvent event = jsonToObject(json.toString(), ChannelChatNotificationEvent.class);
        return event;
    }

    public static ChannelRaidEvent RaidReward(String channel, String raider, Integer viewers) {
        JSONObject json = new JSONObject();

        json.put("from_broadcaster_user_id", TwitchUtils.getUserId(raider));
        json.put("from_broadcaster_user_login", raider.toLowerCase());
        json.put("from_broadcaster_user_name", raider);

        json.put("to_broadcaster_user_id", TwitchUtils.getUserId(channel));
        json.put("to_broadcaster_user_login", channel.toLowerCase());
        json.put("to_broadcaster_user_name", channel);

        json.put("viewers", viewers.toString());

        ChannelRaidEvent event = jsonToObject(json.toString(), ChannelRaidEvent.class);
        return event;
    }
}
