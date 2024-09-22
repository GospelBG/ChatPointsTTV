package me.gosdev.chatpointsttv.Rewards;

import java.util.List;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;

public class Reward {
    rewardType type;
    private String event;
    private List<String> cmds;
    private String channel;
    private String channelId;

    public Reward (rewardType type, String channel, String event, List<String> cmds) {
        this.type = type;
        this.channel = channel;
        
        channelId = channel.equals(Rewards.EVERYONE) ? "*" : ChatPointsTTV.getPlugin().getUserId(channel);

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