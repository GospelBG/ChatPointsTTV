package me.gosdev.chatpointsttv.Utils;

import java.io.IOException;
import java.util.List;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ConfigFile;
import me.gosdev.chatpointsttv.Platforms;

public class FollowerLog {
    private static ConfigFile accounts;
    public static boolean isEnabled = false;

    public static void start() {
        isEnabled = true;
    }

    public static void stop() {
        if (!isEnabled) return;
        save();
        isEnabled = false;
    }

    public static void setAccountsFile(ConfigFile accountsFile) {
        accounts = accountsFile;
    }

    public static boolean wasFollowing(Platforms platform, String channelId, String userId) {
        return accounts.getStringList(platform.name().toLowerCase()+"."+channelId).contains(userId);
    }

    public static void addFollower(Platforms platform, String channelId, String followerId) {
        List<String> list = accounts.getStringList(platform.name().toLowerCase() + "." + channelId);
        list.add(followerId);
        accounts.set(platform.name().toLowerCase() + "." + channelId, list);
        save();
    }

    public static void populateList(Platforms platform, String channelId, List<String> followerList) {
        List<String> cachedList = accounts.getStringList(platform.name().toLowerCase() + "." + channelId);

        for (String uid : followerList) {
            if (!cachedList.contains(uid)) {
                cachedList.add(uid);
            }
        }
        accounts.set(platform.name().toLowerCase() + "." + channelId, cachedList);
        save();
    }

    static void save() {
        try {
            accounts.save();
        } catch (IOException e) {
            ChatPointsTTV.log.error("Failed to save followers file: " + e.getMessage());
        }
    }
}
