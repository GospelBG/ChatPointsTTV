package me.gosdev.chatpointsttv.Rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import me.gosdev.chatpointsttv.ChatPointsTTV;
public class Rewards {
    public enum rewardType {
        TWITCH_FOLLOW,
        TWITCH_CHANNEL_POINTS,
        TWITCH_CHEER,
        TWITCH_SUB,
        TWITCH_GIFT,
        
        TIKTOK_FOLLOW,
        TIKTOK_GIFT,
        TIKTOK_SHARE
    };

    public static Map<rewardType, ArrayList<Reward>> rewards = new HashMap<rewardType, ArrayList<Reward>>();

    public static ArrayList<Reward> getRewards(rewardType type) {
        if (rewards.get(type) != null) return rewards.get(type); // Give stored dictionary if it was already fetched

        ConfigurationSection config_value = ChatPointsTTV.getPlugin().config.getConfigurationSection(type.toString().toUpperCase() + "_REWARDS");
        ArrayList<Reward> reward_list = new ArrayList<>();
        if (type == rewardType.TWITCH_FOLLOW || type == rewardType.TIKTOK_FOLLOW || type == rewardType.TIKTOK_SHARE) { // If it doesen't need a "key"
            List<String> follow_rewards = ChatPointsTTV.getPlugin().config.getStringList(type.toString().toUpperCase() + "_REWARDS");
            if (follow_rewards == null || follow_rewards.isEmpty()) return null;
            reward_list.add(new Reward(type, null, follow_rewards));
        } else {
            Set<String> keys = config_value.getKeys(false);
            if (config_value == null || keys.size() == 0) return null;
            Iterator<?> iterator = keys.iterator();
            for (int i = 0; i < keys.size(); i++) {
                String key = iterator.next().toString();

                reward_list.add(new Reward(type, key, config_value.getStringList(key)));
            }
        }

        rewards.put(type, reward_list);
        return reward_list;
    }

    public static Reward getReward(rewardType type, Optional<String> eventQuery, Optional<ArrayList<String>> cmdsQuery) {
        ArrayList<String> cmds = cmdsQuery.isPresent() ? cmdsQuery.get() : null;
        String event = eventQuery.isPresent() ? eventQuery.get() : null;
        
        ArrayList<Reward> rewards = getRewards(type);

        for (Reward reward : rewards) {
            if (cmds == null || cmds.equals(reward.getCommands())) {
                if (event == null || event.equalsIgnoreCase(reward.getEvent())) {
                    return reward;
                }
            }
        }
        return null;
    }
}
