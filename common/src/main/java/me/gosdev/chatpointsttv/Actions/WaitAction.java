package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.Events.EventInformation;

public class WaitAction implements BaseAction {
    public static final String ACTION_NAME = "WAIT";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    private final Integer time;
    public WaitAction(Integer time) {
        this.time = time;
    }

    @Override
    public void run(EventInformation ei) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
        }
    }

}
