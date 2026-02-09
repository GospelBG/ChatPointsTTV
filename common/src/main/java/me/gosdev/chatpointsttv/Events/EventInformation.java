package me.gosdev.chatpointsttv.Events;

import me.gosdev.chatpointsttv.Platforms;

public class EventInformation {
    private Platforms platform;
    private EventType eventType;
    private String streamer;
    private String chatter;
    private String event;
    private Integer amount;
    private String extra;

    public EventInformation(EventType eventType, String streamer, String chatter) {
        this.platform = EventManager.getPlatformFromType(eventType);
        this.eventType = eventType;
        this.streamer = streamer;
        this.chatter = chatter;
    }

    public EventInformation setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }

    public EventInformation setEvent(String event) {
        this.event = event;
        return this;
    }

    public EventInformation setExtra(String extra) {
        this.extra = extra;
        return this;
    }

    public Platforms getPlatform() {
        return this.platform;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public String getStreamer() {
        return this.streamer; 
    }

    public String getChatter() {
        return this.chatter;
    }

    public Integer getAmount() {
        return this.amount;
    }

    public String getEvent() {
        return event;
    }

    public String getExtra() {
        return this.extra;
    }
}
