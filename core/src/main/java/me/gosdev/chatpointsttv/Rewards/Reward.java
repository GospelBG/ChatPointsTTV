package me.gosdev.chatpointsttv.Rewards;

import java.util.List;
import java.util.Optional;

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

    public boolean equals(Reward reward, Optional<Boolean> exactMatchOpt) {
        Boolean exactMatch = exactMatchOpt.isPresent() ? exactMatchOpt.get() : false;
        
        if (reward.type == this.type && reward.event.equalsIgnoreCase(this.event)) {
            if (exactMatch) {
                return reward.cmds.equals(this.cmds);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}