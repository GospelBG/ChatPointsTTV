package me.gosdev.chatpointsttv.Rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import me.gosdev.chatpointsttv.ChatPointsTTV;
public class Rewards {
    public static enum rewardType {
        FOLLOW,
        CHANNEL_POINTS,
        CHEER,
        SUB,
        GIFT
    };

    public static Map<rewardType, ArrayList<Reward>> rewards = new HashMap<rewardType, ArrayList<Reward>>();

    public static ArrayList<Reward> getRewards(rewardType type) {
        if (rewards.get(type) != null) return rewards.get(type); // Give stored dictionary if it was already fetched

        ConfigurationSection config_value = ChatPointsTTV.getPlugin().config.getConfigurationSection(type.toString().toUpperCase() + "_REWARDS");
        ArrayList<Reward> reward_list = new ArrayList<>();
        if (type == rewardType.FOLLOW) {
            List<String> follow_rewards = ChatPointsTTV.getPlugin().config.getStringList(type.toString().toUpperCase() + "_REWARDS");
            if (follow_rewards == null || follow_rewards.isEmpty()) return null;
            reward_list.add(new Reward(type, null, follow_rewards));
        } else {
            Set<String> keys = config_value.getKeys(false);
            if (config_value == null || keys.size() == 0) return null;
            Iterator<?> iterator = keys.iterator();
            for (int i = 0; i < keys.size(); i++) {
                String key = iterator.next().toString();

                //log.info(key);
                //log.info(config_value.getList(key).toString());
                reward_list.add(new Reward(type, key, config_value.getStringList(key)));
            }
        }

        rewards.put(type, reward_list);
        return reward_list;
    }
}
