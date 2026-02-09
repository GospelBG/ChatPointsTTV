package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.Events.EventInformation;

public interface BaseAction {
    public String getActionName();
    public void run(EventInformation ei);
}
