package me.gosdev.chatpointsttv.Rewards;

import java.util.Comparator;

import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;

public class RewardComparator implements Comparator<Reward> {
    @Override
    public int compare(Reward o1, Reward o2) {
        if (o1.getType() != o2.getType()) throw new java.lang.UnsupportedOperationException("Cannot compare " + o1.getType().toString() + " rewards with " + o2.getType().toString());

        if (o1.getChannel().equals(Rewards.EVERYONE) && !o2.getChannel().equals(Rewards.EVERYONE)) return 1;
        if (o2.getChannel().equals(Rewards.EVERYONE) && !o1.getChannel().equals(Rewards.EVERYONE)) return -1;

        if (o1.getType() == rewardType.CHANNEL_POINTS || o1.getType() == rewardType.FOLLOW || o1.getType() == rewardType.SUB) return 0;
        if (o2.getType() == rewardType.CHANNEL_POINTS || o2.getType() == rewardType.FOLLOW || o2.getType() == rewardType.SUB) return 0;

        return Integer.parseInt(o2.getEvent()) - Integer.parseInt(o1.getEvent());
    }

}
