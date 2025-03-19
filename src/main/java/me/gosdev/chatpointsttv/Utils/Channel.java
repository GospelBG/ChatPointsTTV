package me.gosdev.chatpointsttv.Utils;

import java.util.List;

import com.github.twitch4j.eventsub.EventSubSubscription;

public class Channel {
    private String channelName;
    private String channelId;
    private boolean live;
    private List<EventSubSubscription> subs;

    public Channel (String name, String id, boolean live) {
        this.channelName = name;
        this.live = live;
        this.channelId = id;
    }
    public String getChannelUsername() {
        return channelName;
    }
    public String getChannelId() {
        return channelId;
    }
    public List<EventSubSubscription> getSubs() {
        return subs;
    }
    public boolean isLive() {
        return live;
    }
    public void updateStatus(boolean live) {
        this.live = live;
    }
    public void setSubscriptions(List<EventSubSubscription> subs) {
        this.subs = subs;
    }
}