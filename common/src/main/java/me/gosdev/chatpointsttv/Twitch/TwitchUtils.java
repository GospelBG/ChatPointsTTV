package me.gosdev.chatpointsttv.Twitch;

import java.util.Arrays;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.helix.domain.StreamList;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import me.gosdev.chatpointsttv.ChatPointsTTV;

//import me.gosdev.chatpointsttv.ChatPointsTTV;

public class TwitchUtils {
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
    public static Boolean isLive(String accessToken, String username) throws IllegalArgumentException {
        try {
            StreamList request = ChatPointsTTV.getTwitch().getClient().getHelix().getStreams(accessToken, null, null, null, null, null, null, Arrays.asList(username)).execute();
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
