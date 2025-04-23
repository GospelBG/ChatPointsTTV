package me.gosdev.chatpointsttv.Events;

import java.util.List;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Twitch.TwitchUtils;

public class Event {
    private final EventType type;
    private final String event;
    private final List<String> cmds;
    private final String channel;
    private String channelId;

    public Event (EventType type, String channel, String event, List<String> cmds) {
        this.type = type;
        this.channel = channel;

        try {
            channelId = channel.equals(Events.EVERYONE) ? "*" : TwitchUtils.getUserId(channel);
        } catch (NullPointerException e) {
            ChatPointsTTV.log.warning(e.getMessage());
            channelId = null;
        }

        this.event = event;
        this.cmds = cmds;
    }

    public String getEvent() {
        return event;
    }
    public List<String> getCommands() {
        return cmds;
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
}