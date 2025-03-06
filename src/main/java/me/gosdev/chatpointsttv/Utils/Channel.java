package me.gosdev.chatpointsttv.Utils;

public class Channel {
    private String channelName;
    private String channelId;
    private boolean live;

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
    public boolean isLive() {
        return live;
    }
    public void updateStatus(boolean live) {
        this.live = live;
    }
}