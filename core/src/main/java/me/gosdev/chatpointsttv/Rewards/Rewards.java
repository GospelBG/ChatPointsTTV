package me.gosdev.chatpointsttv.Rewards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.gosdev.chatpointsttv.ChatPointsTTV;
public class Rewards {
    private static FileConfiguration config = ChatPointsTTV.getPlugin().config;
    public static enum rewardType {
        FOLLOW,
        CHANNEL_POINTS,
        CHEER,
        SUB,
        GIFT
    };

    public static Map<rewardType, ArrayList<Reward>> rewards = Collections.emptyMap();

    public static Boolean isEnabled(rewardType type) {
        return true;
        //if (config.getConfigurationSection("CHANNEL_POINTS_REWARDS") != null | config.getConfigurationSection("CHANNEL_POINTS_REWARDS").startsWith("MemorySection[path="))
    }

    public static ArrayList<Reward> getRewards(rewardType type) {
        if (rewards.get(type) != null) return rewards.get(type); // Give stored dictionary if it was already fetched

        ConfigurationSection config_value = config.getConfigurationSection(type.toString().toUpperCase() + "_REWARDS");
        ArrayList<Reward> rewards = new ArrayList<>();
        if (type == rewardType.FOLLOW) {
            List<String> follow_rewards = (List<String>) config.getList(type.toString().toUpperCase() + "_REWARDS");
            if (follow_rewards == null || follow_rewards.isEmpty()) return null;
            rewards.add(new Reward(type, null, follow_rewards));
        } else {
            Set<String> keys = config_value.getKeys(false);
            if (config_value == null || keys.size() == 0) return null;
            Iterator<?> iterator = keys.iterator();
            for (int i = 0; i < keys.size(); i++) {
                String key = iterator.next().toString();

                //log.info(key);
                //log.info(config_value.getList(key).toString());
                rewards.add(new Reward(type, key, (List<String>) config_value.getList(key)));
            }
        }

        return rewards;
    }
}
