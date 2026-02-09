package me.gosdev.chatpointsttv.Events;

import java.util.Comparator;

public class EventComparator implements Comparator<Action> {
    @Override
    public int compare(Action o1, Action o2) {
        if (o1.getType() != o2.getType()) throw new java.lang.UnsupportedOperationException("Cannot compare " + o1.getType().toString() + " events with " + o2.getType().toString());

        try {
            int difference = Integer.parseInt(o2.getEvent()) - Integer.parseInt(o1.getEvent());

            if (difference == 0) throw new NumberFormatException(); // If value matches compare target channel (go straight to catch)
            return difference;
        } catch (NumberFormatException e) {
            if (o1.getChannel().equals(EventManager.EVERYONE) && !o2.getChannel().equals(EventManager.EVERYONE)) return 1;
            if (o2.getChannel().equals(EventManager.EVERYONE) && !o1.getChannel().equals(EventManager.EVERYONE)) return -1;
            return 0;
        }

    }

}
