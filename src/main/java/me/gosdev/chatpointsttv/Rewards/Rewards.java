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

    public static ArrayList<Reward> getRewards(FileConfiguration config, rewardType type) {
        if (rewards.get(type) != null) return rewards.get(type); // Give stored dictionary if it was already fetched

        String key = type.toString().toUpperCase() + "_REWARDS";
        ArrayList<Reward> reward_list = new ArrayList<>();

        if (!config.contains(key)) return null; // No configured rewards for this type

        if (type.equals(rewardType.FOLLOW)) {
            if (config.isConfigurationSection(key)) { // Streamer-specific?
                Set<String> keys = (config.getConfigurationSection(key)).getKeys(false);
                for (String channel : keys) {
                    reward_list.add(new Reward(type, channel.equals("default") ? EVERYONE : channel, null, config.getConfigurationSection(key).getStringList(channel)));
                }
            } else if (config.isList(key)) {
                reward_list.add(new Reward(type, EVERYONE, null, config.getStringList(key)));
            } else {
                ChatPointsTTV.log.severe("ChatPointsTTV: Follow actions must be entered as a list (or a configuration section, if targeting specific streamers). Read the docs for more information.");
                return null;
            }
        } else {
            if (config.isConfigurationSection(key)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                Set<String> keys = section.getKeys(false);
                    for (String subkey : keys) {
                        if (!section.isConfigurationSection(subkey) && !section.isList(subkey)) {
                            ChatPointsTTV.log.severe("ChatPointsTTV: Invalid configuration for " + type.toString().toLowerCase() + " (" + subkey + ") actions. Read the docs for more information.");
                            continue;
                        }
                        if (type.equals(rewardType.CHEER) || type.equals(rewardType.GIFT) || type.equals(rewardType.RAID)) {
                            try {
                                Integer.valueOf(subkey);
                            } catch (NumberFormatException e) {
                                ChatPointsTTV.log.severe("ChatPointsTTV: \"" + subkey +  "\" must be a number.");
                                continue;
                            }
                        }

                        ConfigurationSection channelSection = section.getConfigurationSection(subkey);
                        if (channelSection == null) {
                            // No channel specified
                            reward_list.add(new Reward(type, EVERYONE, subkey, section.getStringList(subkey)));
                        } else {
                            // Streamer specific event
                            Set<String> channelKeys = channelSection.getKeys(false);
                            for (String channel : channelKeys) {
                                reward_list.add(new Reward(type, channel.equals("default") ? EVERYONE : channel, subkey, channelSection.getStringList(channel)));
                            }
                        }
                    }
            } else {
                ChatPointsTTV.log.severe("ChatPointsTTV: Invalid configuration for " + type.toString().toLowerCase() + " actions. Read the docs for more information.");
                return null;
            }
        }

        reward_list.sort(new RewardComparator());
        rewards.put(type, reward_list);

        return rewards.get(type);
    }
}
