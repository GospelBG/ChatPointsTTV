package me.gosdev.chatpointsttv.Events;

import java.util.ArrayList;
import java.util.List;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Twitch.TwitchUtils;

public class Event {
    private final EventType type;
    private final String event;
    private List<String> cmds;
    private final String channel;
    private String customMsg;
    private String channelId = null;

    public Event(EventType type, String channel, String event, List<String> cmds) {
        this.type = type;
        this.channel = channel;

        try {
            channelId = channel.equals(Events.EVERYONE) ? "*" : TwitchUtils.getUserId(channel);
        } catch (NullPointerException e) {
            ChatPointsTTV.log.warning(e.getMessage());
            channelId = null;
        }

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
    public void setCommands(List<String> newCmds) {
        this.cmds = newCmds;
    }
    public EventType getType() {
        return type;
    }
    public String getChannel() {
        return channel;
    }
    public String getTargetId() {
        return channelId;
    }
    public String getCustomMsg() {
        return customMsg;
    }
}