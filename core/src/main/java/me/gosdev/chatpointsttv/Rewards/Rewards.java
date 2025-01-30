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
        GIFT,
        RAID
    };

    public static final String EVERYONE = "*";

    public static Map<rewardType, ArrayList<Reward>> rewards = new HashMap<>();

    public static ArrayList<Reward> getRewards(rewardType type) {
        if (rewards.get(type) != null) return rewards.get(type); // Give stored dictionary if it was already fetched
        FileConfiguration config = ChatPointsTTV.getPlugin().config;

        Object config_obj = config.get(type.toString().toUpperCase() + "_REWARDS");
        ArrayList<Reward> reward_list = new ArrayList<>();

        if (config_obj instanceof ArrayList) { // Should only be non-specific Follow rewards
            if (type != rewardType.FOLLOW) return null; //TODO: ERROR MSG
            reward_list.add(new Reward(type, EVERYONE, null, config.getStringList(type.toString().toUpperCase() + "_REWARDS")));
        } else if (config_obj instanceof  ConfigurationSection) {
            ConfigurationSection config_rewards = (ConfigurationSection) config_obj;
            
            if (type == rewardType.FOLLOW) { // Follow rewards should have one level less
                Set<String> keys = ((ConfigurationSection) config_rewards).getKeys(false);
                for (String channel : keys) {
                    reward_list.add(new Reward(type, channel.equals("default") ? EVERYONE : channel, null, config_rewards.getStringList(channel)));
                }
            } else {
                Set<String> keys = ((ConfigurationSection) config_rewards).getKeys(false);
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
            }
        } else {
            ChatPointsTTV.getPlugin().log.warning("wtf is this: " + config_obj.getClass().getName());
            return null; 
            
        }
        reward_list.sort(new RewardComparator());
        rewards.put(type, reward_list);

        ChatPointsTTV.getPlugin().log.info("Rewards for " + type.toString() + ":");
        if (rewards.get(type) != null && rewards.get(type).size() > 0) {
            for (Reward reward : rewards.get(type)) {
                ChatPointsTTV.getPlugin().log.info(    "Streamer: " + reward.getTargetId() + " Event: " + reward.getEvent() + " Commands: ");
                if (reward.getCommands().size() > 0) {
                    for (String command : reward.getCommands()) {
                        ChatPointsTTV.getPlugin().log.info("        - " + command);
                    }
                }
            }    
        }

        return rewards.get(type);
    }
}
