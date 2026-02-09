package me.gosdev.chatpointsttv;

import me.gosdev.chatpointsttv.Events.EventType;
import me.gosdev.chatpointsttv.TikTok.TikTokEventType;
import me.gosdev.chatpointsttv.Twitch.TwitchEventType;

public enum Platforms {
    TWITCH("Twitch"),
    TIKTOK("TikTok");

    private final String name;

    private Platforms(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public EventType[] getEventTypes() {
        if (this.name.equals("Twitch")) {
            return TwitchEventType.values();
        } else if (this.name.equals("TikTok")) {
            return TikTokEventType.values();
        }
        return null;
    }
}
