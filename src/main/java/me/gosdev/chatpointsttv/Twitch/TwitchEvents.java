package me.gosdev.chatpointsttv.Twitch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.CPTTV_EventHandler;
import me.gosdev.chatpointsttv.Events.Event;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.FollowerLog;

public class TwitchEvents {
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent event) {
        for (Event reward : CPTTV_EventHandler.getActions(ChatPointsTTV.getTwitch().getConfig(), TwitchEventType.CHANNEL_POINTS)) {
            if (!reward.getEvent().equalsIgnoreCase(event.getReward().getTitle())) continue;
            if (!reward.getTargetChannel().equals(event.getBroadcasterUserLogin()) && !reward.getTargetChannel().equals(CPTTV_EventHandler.EVERYONE)) continue;

            List<String> replacedCmds = new ArrayList<>();
            for (String cmd : reward.getCommands()) {
                replacedCmds.add(cmd.replace("{TEXT}", event.getUserInput()));
            }

            if (!replacedCmds.isEmpty()) reward = reward.withCommands(replacedCmds);

            CPTTV_EventHandler.onEvent(Platforms.TWITCH, TwitchEventType.CHANNEL_POINTS, reward, event.getUserName(), event.getBroadcasterUserName(), Optional.of(event.getReward().getTitle()), Optional.empty());
            
            try { // Try to mark the redemption as fulfilled. If the reward was not created by ChatPointsTTV or the streamer has no affiliate privileges, fail silently
                ChatPointsTTV.getTwitch().getClient().getHelix().updateRedemptionStatus(
                    ChatPointsTTV.getTwitch().credentialManager.get(event.getBroadcasterUserId()).getAccessToken(),
                    event.getBroadcasterUserId(),
                    event.getReward().getId(),
                    Collections.singletonList(event.getId()),
                    RedemptionStatus.FULFILLED).execute();
            } catch (Exception e) {}

            return;
        }
    }

    public void onFollow(ChannelFollowEvent event) {
        if (FollowerLog.isEnabled && !event.getFollowedAt().equals(Instant.ofEpochMilli(0))) { // Test events always have epoch time of 0
            if (FollowerLog.wasFollowing(Platforms.TWITCH, event.getBroadcasterUserId(), event.getUserId())) return;
            FollowerLog.addFollower(Platforms.TWITCH, event.getBroadcasterUserId(), event.getUserId());
        }
        for (Event reward : CPTTV_EventHandler.getActions(ChatPointsTTV.getTwitch().getConfig(), TwitchEventType.FOLLOW)) {
            if (!reward.getTargetChannel().equals(event.getBroadcasterUserLogin()) && !reward.getTargetChannel().equals(CPTTV_EventHandler.EVERYONE)) continue;

            CPTTV_EventHandler.onEvent(Platforms.TWITCH, TwitchEventType.FOLLOW, reward, event.getUserName(), event.getBroadcasterUserName(), Optional.empty(), Optional.empty());
            return;    
        }
    }

    public void onCheer(ChannelChatMessageEvent event) {
        if (event.getCheer() == null) return;
        Integer amount = event.getCheer().getBits();

        for (Event reward : CPTTV_EventHandler.getActions(ChatPointsTTV.getTwitch().getConfig(), TwitchEventType.CHEER)) {
            if (!reward.getTargetChannel().equals(event.getBroadcasterUserLogin()) && !reward.getTargetChannel().equals(CPTTV_EventHandler.EVERYONE)) continue;
            try {
                if (amount >= Integer.valueOf(reward.getEvent())) {
                    CPTTV_EventHandler.onEvent(Platforms.TWITCH, TwitchEventType.CHEER, reward, event.getChatterUserName(), event.getBroadcasterUserName(), Optional.empty(), Optional.of(amount));
                    return;
                }
    
            } catch (NumberFormatException e) {
                ChatPointsTTV.log.warning("Invalid cheer amount: " + reward.getEvent());
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
                ChatPointsTTV.log.warning("Couldn't fetch sub type!");
                return;
            
        }

        for (Event reward : CPTTV_EventHandler.getActions(ChatPointsTTV.getTwitch().getConfig(), TwitchEventType.SUB)) {
            if (!reward.getTargetChannel().equals(event.getBroadcasterUserLogin()) && !reward.getTargetChannel().equals(CPTTV_EventHandler.EVERYONE)) continue;

            if (reward.getEvent().equals(TwitchUtils.PlanToConfig(tier))) {
                CPTTV_EventHandler.onEvent(Platforms.TWITCH, TwitchEventType.SUB, reward, chatter, event.getBroadcasterUserName(), Optional.of(TwitchUtils.PlanToString(tier)), Optional.empty());
                return;
            }
        }
    }

    public void onSubGift(ChannelChatNotificationEvent event) {
        Integer amount = event.getCommunitySubGift().getTotal();

        for (Event reward : CPTTV_EventHandler.getActions(ChatPointsTTV.getTwitch().getConfig(), TwitchEventType.GIFT)) {
            if (!reward.getTargetChannel().equals(event.getBroadcasterUserLogin()) && !reward.getTargetChannel().equals(CPTTV_EventHandler.EVERYONE)) continue;
            if (amount >= Integer.valueOf(reward.getEvent())) {
                CPTTV_EventHandler.onEvent(Platforms.TWITCH, TwitchEventType.GIFT, reward, event.getChatterUserName(), event.getBroadcasterUserName(), Optional.empty(), Optional.of(amount));
                return;
            }
        }
    }

    public void onRaid(ChannelRaidEvent event) {
        String raiderName = event.getFromBroadcasterUserName();
        Integer amount = event.getViewers();

        for (Event reward : CPTTV_EventHandler.getActions(ChatPointsTTV.getTwitch().getConfig(), TwitchEventType.RAID)) {
            if (!reward.getTargetChannel().equals(event.getToBroadcasterUserLogin()) && !reward.getTargetChannel().equals(CPTTV_EventHandler.EVERYONE)) continue;
            if (amount >= Integer.valueOf(reward.getEvent())) {
                CPTTV_EventHandler.onEvent(Platforms.TWITCH, TwitchEventType.RAID, reward, raiderName, event.getToBroadcasterUserName(), Optional.empty(), Optional.of(amount));
                return;
            }
        }
    }
}
