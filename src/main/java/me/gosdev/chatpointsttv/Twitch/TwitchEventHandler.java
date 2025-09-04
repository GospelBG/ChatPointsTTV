package me.gosdev.chatpointsttv.Twitch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.Event;
import me.gosdev.chatpointsttv.Events.EventType;
import me.gosdev.chatpointsttv.Events.Events;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.FollowerLog;

public class TwitchEventHandler {
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent event) {
        for (Event reward : Events.getActions(ChatPointsTTV.getTwitch().getConfig(), EventType.CHANNEL_POINTS)) {
            if (!reward.getEvent().equalsIgnoreCase(event.getReward().getTitle())) continue;
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Events.EVERYONE)) continue;

            List<String> replacedCmds = new ArrayList<>();
            for (String cmd : reward.getCommands()) {
                replacedCmds.add(cmd.replace("{TEXT}", event.getUserInput()));
            }

            reward.setCommands(replacedCmds);

            Events.onEvent(Platforms.TWITCH, EventType.CHANNEL_POINTS, reward, event.getUserName(), event.getBroadcasterUserName(), Optional.of(event.getReward().getTitle()));
            return;
        }
    }

    public void onFollow(ChannelFollowEvent event) {
        if (FollowerLog.isEnabled && !event.getFollowedAt().equals(Instant.ofEpochMilli(0))) { // Test events always have epoch time of 0
            if (FollowerLog.wasFollowing(Platforms.TWITCH, event.getBroadcasterUserId(), event.getUserId())) return;
            FollowerLog.addFollower(Platforms.TWITCH, event.getBroadcasterUserId(), event.getUserId());
        }
        for (Event reward : Events.getActions(ChatPointsTTV.getTwitch().getConfig(), EventType.FOLLOW)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Events.EVERYONE)) continue;

            Events.onEvent(Platforms.TWITCH, EventType.FOLLOW, reward, event.getUserName(), event.getBroadcasterUserName(), Optional.empty());
            return;    
        }
    }

    public void onCheer(ChannelChatMessageEvent event) {
        if (event.getCheer() == null) return;
        Integer amount = event.getCheer().getBits();

        for (Event reward : Events.getActions(ChatPointsTTV.getTwitch().getConfig(), EventType.CHEER)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Events.EVERYONE)) continue;
            try {
                if (amount >= Integer.valueOf(reward.getEvent())) {
                    Events.onEvent(Platforms.TWITCH, EventType.CHEER, reward, event.getChatterUserName(), event.getBroadcasterUserName(), Optional.of(amount.toString()));
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

        for (Event reward : Events.getActions(ChatPointsTTV.getTwitch().getConfig(), EventType.SUB)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Events.EVERYONE)) continue;

            if (reward.getEvent().equals(TwitchUtils.PlanToConfig(tier))) {
                Events.onEvent(Platforms.TWITCH, EventType.SUB, reward, chatter, event.getBroadcasterUserName(), Optional.of(event.getSub().getDurationMonths().toString()));
                return;
            }
        }
    }

    public void onSubGift(ChannelChatNotificationEvent event) {
        Integer amount = event.getCommunitySubGift().getTotal();

        for (Event reward : Events.getActions(ChatPointsTTV.getTwitch().getConfig(), EventType.GIFT)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Events.EVERYONE)) continue;
            if (amount >= Integer.valueOf(reward.getEvent())) {
                Events.onEvent(Platforms.TWITCH, EventType.GIFT, reward, event.getChatterUserName(), event.getBroadcasterUserName(), Optional.of(amount.toString()));
                return;
            }
        }
    }

    public void onRaid(ChannelRaidEvent event) {
        String raiderName = event.getFromBroadcasterUserName();
        Integer amount = event.getViewers();

        for (Event reward : Events.getActions(ChatPointsTTV.getTwitch().getConfig(), EventType.RAID)) {
            if (!reward.getTargetId().equals(event.getToBroadcasterUserId()) && !reward.getTargetId().equals(Events.EVERYONE)) continue;
            if (amount >= Integer.valueOf(reward.getEvent())) {
                Events.onEvent(Platforms.TWITCH, EventType.RAID, reward, raiderName, event.getToBroadcasterUserName(), Optional.of(amount.toString()));
                return;
            }
        }
    }
}
