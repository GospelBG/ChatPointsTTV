package me.gosdev.chatpointsttv.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;

public class FollowerLog {
    static FileConfiguration followersYaml;
    static File followersFile;
    public static boolean isEnabled = false;

    public static void start() {
        if (isEnabled) return;
        followersFile = new File(ChatPointsTTV.getPlugin().getDataFolder(), "followers.yml");
        followersYaml = YamlConfiguration.loadConfiguration(followersFile);
        for (Platforms p : Platforms.values()) {
            if (followersYaml.getConfigurationSection(p.name().toLowerCase()) == null) {
                followersYaml.createSection(p.name().toLowerCase());
            }
        }
        isEnabled = true;
    }

    public static void stop() {
        if (!isEnabled) return;
        save();

        followersFile = null;
        followersYaml = null;
        isEnabled = false;
    }

    public static boolean wasFollowing(Platforms platform, String channelId, String userId) {
        return followersYaml.getStringList(platform.name().toLowerCase()+"."+channelId).contains(userId);
    }

    public static void addFollower(Platforms platform, String channelId, String followerId) {
        List<String> list = followersYaml.getConfigurationSection(platform.name().toLowerCase()).getStringList(channelId);
        list.add(followerId);
        followersYaml.getConfigurationSection(platform.name().toLowerCase()).set(channelId, list);
        save();
    }

    public static void populateList(Platforms platform, String channelId, List<String> followerList) {
        List<String> cachedList = followersYaml.getConfigurationSection(platform.name().toLowerCase()).getStringList(channelId);

        for (String uid : followerList) {
            if (!cachedList.contains(uid)) {
                cachedList.add(uid);
            }
        }
        followersYaml.getConfigurationSection(platform.name().toLowerCase()).set(channelId, cachedList);
        save();
    }

    static void save() {
        try {
            followersYaml.save(followersFile);
        } catch (IOException e) {
            ChatPointsTTV.log.severe("Failed to save followers.yml: " + e.getMessage());
        }
    }
}