package me.gosdev.chatpointsttv.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.helix.domain.ModeratedChannel;
import com.github.twitch4j.helix.domain.ModeratedChannelList;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.UserList;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;

public class TwitchUtils {
    private static final TwitchClient twitch = ChatPointsTTV.getTwitch();
    public static List<String> getModeratedChannelIDs(String auth, String userId) throws HystrixRuntimeException {
        String cursor = null;
        List<String> modsOutput = new ArrayList<>();

        do {
            ModeratedChannelList moderatorList = twitch.getClient().getHelix().getModeratedChannels(
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
    public static String getUserId(String username) throws IllegalArgumentException {
        try {
            UserList resultList = twitch.getClient().getHelix().getUsers(ChatPointsTTV.getTwitch().oauth.getAccessToken(), null, Arrays.asList(username)).execute();
            if (resultList.getUsers().isEmpty()) {
                throw new NullPointerException("Couldn't fetch user: " + username);
            }
            return resultList.getUsers().get(0).getId();
        } catch (HystrixRuntimeException e) {
            if (e.getFailureType().equals(HystrixRuntimeException.FailureType.COMMAND_EXCEPTION)) {
                throw new IllegalArgumentException("Invalid username: " + username);
            } else {
                throw e;
            }
        }
    }
    public static Boolean isLive(String accessToken, String username) throws IllegalArgumentException {
        try {
            StreamList request = twitch.getClient().getHelix().getStreams(accessToken, null, null, null, null, null, null, Arrays.asList(username)).execute();
            return !request.getStreams().isEmpty();
        } catch (HystrixRuntimeException e) {
            if (e.getFailureType().equals(HystrixRuntimeException.FailureType.COMMAND_EXCEPTION)) {
                throw new IllegalArgumentException("Invalid username: " + username);
            } else {
                throw e;
            }
        }
    }
}
