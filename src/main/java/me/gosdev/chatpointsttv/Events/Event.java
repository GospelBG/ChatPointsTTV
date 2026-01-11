package me.gosdev.chatpointsttv.Events;

import java.util.ArrayList;
import java.util.List;

import me.gosdev.chatpointsttv.ChatPointsTTV;

public class Event {
    private final EventType type;
    private final String event;
    private final List<String> cmds;
    private final String channel;
    private String customMsg;

    public Event(EventType type, String channel, String event, List<String> cmds) {
        this.type = type;
        this.channel = channel.toLowerCase();

        this.event = event;
        this.cmds = new ArrayList<>();

        for (int i = 0; i < cmds.size(); i++) {
            if (cmds.get(i).startsWith("CUSTOM_MSG")) {
                customMsg = cmds.get(i).replaceFirst("CUSTOM_MSG " , "");

                if (customMsg.isBlank()) {
                    ChatPointsTTV.log.severe("ChatPointsTTV: CUSTOM_MSG is blank. Falling back to default string.");
                    customMsg = null;
                }
            } else {
                this.cmds.add(cmds.get(i));
            }
        }
    }

    public String getEvent() {
        return event;
    }
    public List<String> getCommands() {
        return cmds;
    }
    public Event withCommands(List<String> newCmds) {
        return new Event(this.type, this.channel, this.event, newCmds);
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