package me.gosdev.chatpointsttv.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.helix.domain.ModeratedChannel;
import com.github.twitch4j.helix.domain.ModeratedChannelList;
import com.github.twitch4j.helix.domain.UserList;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import me.gosdev.chatpointsttv.ChatPointsTTV;

public class TwitchUtils {
    public static List<String> getModeratedChannelIDs(String auth, String userId) throws HystrixRuntimeException {
        String cursor = null;
        List<String> modsOutput = new ArrayList<>();

        do {
            ModeratedChannelList moderatorList = ChatPointsTTV.getClient().getHelix().getModeratedChannels(
                    auth,
                    userId,
                    100,
                    cursor
            ).execute();
            cursor = moderatorList.getPagination().getCursor();
            for (ModeratedChannel channel : moderatorList.getChannels()) {
                modsOutput.add(channel.getBroadcasterId());
            }
        } while (cursor != null);
        return modsOutput;
    }

    public static String PlanToString(SubscriptionPlan plan) {
        switch (plan.toString()) {
            case "Prime":
                return "Tier 1 (Prime)";
            case "1000":
                return "Tier 1";
            case "2000":
                return "Tier 2";
            case "3000":
                return "Tier 3";
            default:
                return null;
        }
    }

    public static String PlanToConfig(SubscriptionPlan plan) {
        switch (plan.toString()) {
            case "Prime":
                return "TWITCH_PRIME";
            case "1000":
                return "TIER1";
            case "2000":
                return "TIER2";
            case "3000":
                return "TIER3";
            default:
                return null;
        }
    }
    public static String getUserId(String username) {
        UserList resultList = ChatPointsTTV.getClient().getHelix().getUsers(ChatPointsTTV.oauth.getAccessToken(), null, Arrays.asList(username)).execute();
        if (resultList.getUsers().isEmpty()) {
            throw new NullPointerException("Couldn't fetch user: " + username);
        }
        return resultList.getUsers().get(0).getId();
    }
    public static String getUsername(String userId) {
        UserList resultList = ChatPointsTTV.getClient().getHelix().getUsers(ChatPointsTTV.oauth.getAccessToken(), Arrays.asList(userId), null).execute();
        if (resultList.getUsers().isEmpty()) {
            throw new NullPointerException("Couldn't fetch user ID: " + userId);
        }
        return resultList.getUsers().get(0).getDisplayName();
    }
}
