package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.Events.EventInformation;

// Dummy Action class for Custom MSG Command
public class CustomMsgAction implements BaseAction {
    public final static String ACTION_NAME = "CUSTOM_MSG";
    private final String message;

    public CustomMsgAction(String message) {
        this.message = message;
    }

    public String getCustomMessage() {
        return message;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }
    
    @Override
    public void run(EventInformation ei) {
    }
}
