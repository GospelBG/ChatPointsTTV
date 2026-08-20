package me.gosdev.chatpointsttv.Events;

import java.util.Comparator;

public class EventComparator implements Comparator<Event> {
    @Override
    public int compare(Event o1, Event o2) {
        if (o1.getType() != o2.getType()) throw new java.lang.UnsupportedOperationException("Cannot compare " + o1.getType().toString() + " events with " + o2.getType().toString());

        try {
            int difference = Integer.parseInt(o2.getEvent()) - Integer.parseInt(o1.getEvent());

            if (difference == 0) throw new NumberFormatException(); // If value matches compare target channel (go straight to catch)
            return difference;
        } catch (NumberFormatException e) {
            if (o1.getChannel().equals(CPTTV_EventHandler.EVERYONE) && !o2.getChannel().equals(CPTTV_EventHandler.EVERYONE)) return 1;
            if (o2.getChannel().equals(CPTTV_EventHandler.EVERYONE) && !o1.getChannel().equals(CPTTV_EventHandler.EVERYONE)) return -1;
            return 0;
        }

    }

}
