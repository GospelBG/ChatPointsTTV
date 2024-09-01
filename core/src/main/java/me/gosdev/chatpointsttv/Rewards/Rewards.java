package me.gosdev.chatpointsttv.Rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.gosdev.chatpointsttv.ChatPointsTTV;
public class Rewards {
    public static enum rewardType {
        FOLLOW,
        CHANNEL_POINTS,
        CHEER,
        SUB,
        GIFT
    };

    public static final String EVERYONE = "*";

    public static Map<rewardType, ArrayList<Reward>> rewards = new HashMap<rewardType, ArrayList<Reward>>();

    public static ArrayList<Reward> getRewards(rewardType type) {
        //if (rewards.get(type) != null) return rewards.get(type); // Give stored dictionary if it was already fetched
        FileConfiguration config = ChatPointsTTV.getPlugin().config;

        ConfigurationSection config_rewards = config.getConfigurationSection(type.toString().toUpperCase() + "_REWARDS");
        ArrayList<Reward> reward_list = new ArrayList<>();

        if (type == rewardType.FOLLOW) {
            if (config_rewards == null && config.getStringList(type.toString().toUpperCase() + "_REWARDS") != null) {
                // Global events
                reward_list.add(new Reward(type, EVERYONE, null, config.getStringList(type.toString().toUpperCase() + "_REWARDS")));
            } else {
                // Streamer specific events
                Set<String> keys = config_rewards.getKeys(false);
                for (String channel : keys) {
                    reward_list.add(new Reward(type, channel.equals("default") ? EVERYONE : channel, null, config_rewards.getStringList(channel)));
                }
            }
            rewards.put(type, reward_list);
            return reward_list;

        } else if (config_rewards != null) {
            Set<String> keys = config_rewards.getKeys(false);

            for (String key : keys) {
                ConfigurationSection channelSection = config_rewards.getConfigurationSection(key);
                if (channelSection == null) {
                    // No channel specified
                    reward_list.add(new Reward(type, EVERYONE, key, config_rewards.getStringList(key)));
                } else {
                    // Streamer specific event
                    Set<String> channelKeys = channelSection.getKeys(false);
                    for (String channel : channelKeys) {
                        reward_list.add(new Reward(type, channel.equals("default") ? EVERYONE : channel, key, channelSection.getStringList(channel)));
                    }
                }
            }
            rewards.put(type, reward_list);
            return reward_list;
        }

        return null;
    }
}
