package me.gosdev.chatpointsttv.Twitch;

import java.time.Instant;
import java.util.Collections;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Events.EventManager;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.FollowerLog;

public class TwitchEvents {
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent event) {
        /*List<String> replacedCmds = new ArrayList<>();
        for (String cmd : reward.getCommands()) {
            replacedCmds.add(cmd.replace("{TEXT}", event.getUserInput()));
        }*/

        //if (!replacedCmds.isEmpty()) reward = reward.withCommands(replacedCmds);

        EventManager.onEvent(new EventInformation(TwitchEventType.CHANNEL_POINTS, event.getBroadcasterUserName(), event.getUserName()).setEvent(event.getReward().getTitle()).setExtra(event.getUserInput()));
        
        if (!event.getBroadcasterUserId().equals("0")) { // Test events have broadcaster ID of 0
            try { // Try to mark the redemption as fulfilled. If the reward was not created by ChatPointsTTV or the streamer has no affiliate privileges, fail silently
                ChatPointsTTV.getTwitch().getClient().getHelix().updateRedemptionStatus(
                    ChatPointsTTV.getAccounts().getTwitchOAuth(event.getBroadcasterUserId()).getAccessToken(),
                    event.getBroadcasterUserId(),
                    event.getReward().getId(),
                    Collections.singletonList(event.getId()),
                    RedemptionStatus.FULFILLED).execute();
            } catch (Exception e) {}
        }
    }

    public void onFollow(ChannelFollowEvent event) {
        if (FollowerLog.isEnabled && !event.getFollowedAt().equals(Instant.ofEpochMilli(0))) { // Test events always have epoch time of 0
            if (FollowerLog.wasFollowing(Platforms.TWITCH, event.getBroadcasterUserId(), event.getUserId())) return;
            FollowerLog.addFollower(Platforms.TWITCH, event.getBroadcasterUserId(), event.getUserId());
        }
        EventManager.onEvent(new EventInformation(TwitchEventType.FOLLOW, event.getBroadcasterUserName(), event.getUserName()));
    }

    public void onCheer(ChannelChatMessageEvent event) {
        if (event.getCheer() == null) return;
        String message = event.getMessage() != null ? event.getMessage().getCleanedText() : null;
        Integer amount = event.getCheer().getBits();

        EventManager.onEvent(new EventInformation(TwitchEventType.CHEER, event.getBroadcasterUserName(), event.getChatterUserName()).setAmount(amount).setExtra(message));
    }

    public void onSub(ChannelChatNotificationEvent event) {
        SubscriptionPlan tier;
        String message = event.getMessage() != null ? event.getMessage().getCleanedText() : null;

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
                ChatPointsTTV.log.warn("Couldn't fetch sub type!");
                return;
            
        }

        EventManager.onEvent(new EventInformation(TwitchEventType.SUB, event.getBroadcasterUserName(), event.getChatterUserName()).setEvent(tier.toString()).setExtra(message));
    }

    public void onSubGift(ChannelChatNotificationEvent event) {
        Integer amount = event.getCommunitySubGift().getTotal();

        EventManager.onEvent(new EventInformation(TwitchEventType.GIFT, event.getBroadcasterUserName(), event.getChatterUserName()).setAmount(amount));
    }

    public void onRaid(ChannelRaidEvent event) {
        String raiderName = event.getFromBroadcasterUserName();
        Integer amount = event.getViewers();

        EventManager.onEvent(new EventInformation(TwitchEventType.RAID, raiderName, event.getToBroadcasterUserName()).setAmount(amount));
    }
}
