package me.gosdev.chatpointsttv.Rewards;

import java.util.List;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Twitch.TwitchUtils;

public class Reward {
    private final rewardType type;
    private final String event;
    private final List<String> cmds;
    private final String channel;
    private String channelId;

    public Reward (rewardType type, String channel, String event, List<String> cmds) {
        this.type = type;
        this.channel = channel;

        try {
            channelId = channel.equals(Rewards.EVERYONE) ? "*" : TwitchUtils.getUserId(channel);
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
    public rewardType getType() {
        return type;
    }
    public String getChannel() {
        return channel;
    }
    public String getTargetId() {
        return channelId;
    }
}