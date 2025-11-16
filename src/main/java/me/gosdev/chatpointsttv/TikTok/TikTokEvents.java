package me.gosdev.chatpointsttv.TikTok;

import java.util.Optional;

import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.CPTTV_EventHandler;
import me.gosdev.chatpointsttv.Events.Event;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.FollowerLog;

public class TikTokEvents {
    public void onLike(TikTokLikeEvent event, String hostName) {
        for (Event reward : CPTTV_EventHandler.getActions(TikTokClient.getConfig(), TikTokEventType.LIKE)) {
            try {
                if (event.getLikes() >= Integer.parseInt(reward.getEvent())) {
                    CPTTV_EventHandler.onEvent(Platforms.TIKTOK, TikTokEventType.LIKE, reward, event.getUser().getName(), hostName, Optional.of(String.valueOf(event.getLikes())));
                    return;
                }
    
            } catch (NumberFormatException e) {
                ChatPointsTTV.log.warning("Invalid like combo amount: " + reward.getEvent());
                return;
            }
        }
    }

    public void onGift(TikTokGiftComboEvent event) {
        ChatPointsTTV.log.info("Combo:" + String.valueOf(event.getCombo()) + " " + event.getComboState());
        for (Event reward : CPTTV_EventHandler.getActions(TikTokClient.getConfig(), TikTokEventType.GIFT)) {
            if (event.getGift().getName().equalsIgnoreCase(reward.getEvent()) || reward.getEvent().equalsIgnoreCase("any")) {
                CPTTV_EventHandler.onEvent(Platforms.TIKTOK, TikTokEventType.GIFT, reward, event.getUser().getProfileName(), event.getToUser().getProfileName(), Optional.of(event.getGift().getName() + " x" + event.getCombo()));
                return;
            }
        }
    }

    public void onFollow(TikTokFollowEvent event, String hostName) {
        if (FollowerLog.isEnabled && event.getUser().getId() != -1) { // uID = -1 -> Test Event
            if (FollowerLog.wasFollowing(Platforms.TIKTOK, Long.toString(event.getRoomId()), event.getUser().getId().toString())) return;
            FollowerLog.addFollower(Platforms.TIKTOK, Long.toString(event.getRoomId()), event.getUser().getId().toString());
        }

        for (Event reward : CPTTV_EventHandler.getActions(TikTokClient.getConfig(), TikTokEventType.FOLLOW)) {
            if (!reward.getTargetId().equals(Long.toString(event.getRoomId())) && !reward.getTargetId().equals(CPTTV_EventHandler.EVERYONE)) continue;

            CPTTV_EventHandler.onEvent(Platforms.TIKTOK, TikTokEventType.FOLLOW, reward, event.getUser().getProfileName(), hostName, Optional.empty());
            return;
        }
    }

    public void onShare(TikTokShareEvent event, String hostName) {
        for (Event reward : CPTTV_EventHandler.getActions(TikTokClient.getConfig(), TikTokEventType.SHARE)) {
            if (!reward.getTargetId().equals(Long.toString(event.getRoomId())) && !reward.getTargetId().equals(CPTTV_EventHandler.EVERYONE)) continue;

            CPTTV_EventHandler.onEvent(Platforms.TIKTOK, TikTokEventType.SHARE, reward, event.getUser().getProfileName(), hostName, Optional.empty());
            return;
        }
    }
}
