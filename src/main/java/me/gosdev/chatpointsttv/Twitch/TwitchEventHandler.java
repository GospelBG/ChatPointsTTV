package me.gosdev.chatpointsttv.Twitch;

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
import me.gosdev.chatpointsttv.Events;
import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Utils.TwitchUtils;

public class TwitchEventHandler {
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent event) {
        for (Reward reward : Rewards.getRewards(ChatPointsTTV.getTwitch().getConfig(), rewardType.CHANNEL_POINTS)) {
            if (!reward.getEvent().equalsIgnoreCase(event.getReward().getTitle())) continue;
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;

            List<String> replacedCmds = new ArrayList<>();
            for (String cmd : reward.getCommands()) {
                replacedCmds.add(cmd.replace("{TEXT}", event.getUserInput()));
            }

            Events.onEvent(rewardType.CHANNEL_POINTS, new Reward(reward.getType(), reward.getChannel(), reward.getEvent(), replacedCmds), event.getUserName(), event.getBroadcasterUserName(), Optional.of(event.getReward().getTitle()));
            return;
        }
    }

    public void onFollow(ChannelFollowEvent event) {
        for (Reward reward : Rewards.getRewards(ChatPointsTTV.getTwitch().getConfig(), rewardType.FOLLOW)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;

            Events.onEvent(rewardType.FOLLOW, reward, event.getUserName(), event.getBroadcasterUserName(), Optional.empty());
            return;    
        }
    }

    public void onCheer(ChannelChatMessageEvent event) {
        if (event.getCheer() == null) return;
        Integer amount = event.getCheer().getBits();

        for (Reward reward : Rewards.getRewards(ChatPointsTTV.getTwitch().getConfig(), rewardType.CHEER)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;
            try {
                if (amount >= Integer.valueOf(reward.getEvent())) {
                    Events.onEvent(rewardType.CHEER, reward, event.getChatterUserName(), event.getBroadcasterUserName(), Optional.of(amount.toString()));
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

        for (Reward reward : Rewards.getRewards(ChatPointsTTV.getTwitch().getConfig(), rewardType.SUB)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;

            if (reward.getEvent().equals(TwitchUtils.PlanToConfig(tier))) {
                Events.onEvent(rewardType.SUB, reward, chatter, event.getBroadcasterUserName(), Optional.of(tier.name() + " sub"));
                return;
            }
        }
    }

    public void onSubGift(ChannelChatNotificationEvent event) {
        Integer amount = event.getCommunitySubGift().getTotal();

        for (Reward reward : Rewards.getRewards(ChatPointsTTV.getTwitch().getConfig(), rewardType.GIFT)) {
            if (!reward.getTargetId().equals(event.getBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;
            if (amount >= Integer.valueOf(reward.getEvent())) {
                Events.onEvent(rewardType.GIFT, reward, event.getChatterUserName(), event.getBroadcasterUserName(), Optional.of(amount.toString()));
                return;
            }
        }
    }

    public void onRaid(ChannelRaidEvent event) {
        String raiderName = event.getFromBroadcasterUserName();
        Integer amount = event.getViewers();

        for (Reward reward : Rewards.getRewards(ChatPointsTTV.getTwitch().getConfig(), rewardType.RAID)) {
            if (!reward.getTargetId().equals(event.getToBroadcasterUserId()) && !reward.getTargetId().equals(Rewards.EVERYONE)) continue;
            if (amount >= Integer.valueOf(reward.getEvent())) {
                Events.onEvent(rewardType.RAID, reward, raiderName, event.getToBroadcasterUserName(), Optional.of(amount.toString()));
                return;
            }
        }
    }
}
