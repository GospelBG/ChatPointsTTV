package me.gosdev.chatpointsttv.Rewards;

import java.util.Comparator;

public class RewardComparator implements Comparator<Reward> {
    @Override
    public int compare(Reward o1, Reward o2) {
        if (o1.getType() != o2.getType()) throw new java.lang.UnsupportedOperationException("Cannot compare " + o1.getType().toString() + " rewards with " + o2.getType().toString());

        try {
            int difference = Integer.parseInt(o1.getEvent()) - Integer.parseInt(o2.getEvent());

            if (difference == 0) throw new NumberFormatException(); // If value matches compare target channel (go straight to catch)
            return difference;
        } catch (NumberFormatException e) {
            if (o1.getChannel().equals(Rewards.EVERYONE) && !o2.getChannel().equals(Rewards.EVERYONE)) return 1;
            if (o2.getChannel().equals(Rewards.EVERYONE) && !o1.getChannel().equals(Rewards.EVERYONE)) return -1;
            return 0;
        }

    }

}
