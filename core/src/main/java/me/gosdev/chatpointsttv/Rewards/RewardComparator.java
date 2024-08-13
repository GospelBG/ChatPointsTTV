package me.gosdev.chatpointsttv.Rewards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;

public class RewardComparator implements Comparator<Reward> {
    @Override
    public int compare(Reward o1, Reward o2) {
        if (o1.getType() != o2.getType()) throw new java.lang.UnsupportedOperationException("Cannot compare " + o1.getType().toString() + " rewards with " + o2.getType().toString());
        if (o1.getType() == rewardType.TWITCH_CHANNEL_POINTS || o1.getType() == rewardType.TWITCH_FOLLOW || o1.getType() == rewardType.TWITCH_SUB) throw new UnsupportedOperationException("Cannot sort " + o1.getType().toString() + " rewards.");
        if (o2.getType() == rewardType.TWITCH_CHANNEL_POINTS || o2.getType() == rewardType.TWITCH_FOLLOW || o2.getType() == rewardType.TWITCH_SUB) throw new UnsupportedOperationException("Cannot sort " + o1.getType().toString() + " rewards.");

        List<Integer> amounts = new ArrayList<Integer>();
        Rewards.getRewards(rewardType.TWITCH_CHEER).forEach((reward) -> {
            amounts.add(Integer.parseInt(reward.getEvent()));
        });


        return Integer.parseInt(o1.getEvent()) - Integer.parseInt(o2.getEvent());
    }
}
