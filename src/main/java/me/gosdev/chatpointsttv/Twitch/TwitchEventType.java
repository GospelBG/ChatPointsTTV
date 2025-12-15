package me.gosdev.chatpointsttv.Twitch;

import me.gosdev.chatpointsttv.Events.EventType;

public enum TwitchEventType implements EventType {
    FOLLOW,
    CHANNEL_POINTS,
    CHEER,
    SUB,
    GIFT,
    RAID
};
