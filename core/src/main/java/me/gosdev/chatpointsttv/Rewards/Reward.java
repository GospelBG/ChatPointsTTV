package me.gosdev.chatpointsttv.Rewards;

import java.util.List;

import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;

public class Reward {
    rewardType type;
    private String event;
    private List<String> cmds;

    public Reward (rewardType type, String event, List<String> cmds) {
        this.type = type;
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
}