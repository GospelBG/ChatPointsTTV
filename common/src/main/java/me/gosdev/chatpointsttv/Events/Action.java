package me.gosdev.chatpointsttv.Events;

import java.util.ArrayList;
import java.util.List;

import me.gosdev.chatpointsttv.Actions.CustomMsgAction;
import me.gosdev.chatpointsttv.ChatPointsTTV;

public class Action {
    private final EventType type;
    private final String event;
    private final List<String> actions;
    private final String channel;
    private String customMsg;

    public Action(EventType type, String channel, String event, List<String> actions) {
        this.type = type;
        this.channel = channel.equalsIgnoreCase("default") ? EventManager.EVERYONE : channel.toLowerCase();

        this.event = event;
        this.actions = new ArrayList<>();

        for (String action : actions) {
            String[] parts = action.split(" ", 2);
            if (parts.length > 0 && parts[0].equalsIgnoreCase(CustomMsgAction.ACTION_NAME)) {
                String msg = parts.length == 2 ? parts[1] : "";
                if (msg.isBlank()) {
                    ChatPointsTTV.log.error("ChatPointsTTV: CUSTOM_MSG is blank. Falling back to default string.");
                    customMsg = null;
                } else {
                    customMsg = msg;
                }
            } else {
                this.actions.add(action);
            }
        }
    }

    public String getEvent() {
        return event;
    }
    public List<String> getRawActions() {
        return actions;
    }
    public EventType getType() {
        return type;
    }
    public String getChannel() {
        return channel;
    }
    public String getTargetChannel() {
        return channel;
    }
    public String getCustomMsg() {
        return customMsg;
    }
}