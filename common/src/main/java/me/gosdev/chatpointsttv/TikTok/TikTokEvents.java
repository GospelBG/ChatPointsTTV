package me.gosdev.chatpointsttv.TikTok;

import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Events.EventManager;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.FollowerLog;

public class TikTokEvents {
    public void onLike(TikTokLikeEvent event, String hostName) {
        EventManager.onEvent(new EventInformation(TikTokEventType.LIKE, hostName, event.getUser().getProfileName()));
    }

    public void onGift(TikTokGiftComboEvent event, String hostName) {
        EventManager.onEvent(new EventInformation(TikTokEventType.GIFT, hostName, event.getUser().getProfileName()).setExtra(event.getGift().getName()).setAmount(event.getCombo()));
    }

    public void onFollow(TikTokFollowEvent event, String hostName) {
        if (FollowerLog.isEnabled && event.getUser().getId() != -1) { // uID = -1 -> Test Event
            String hostId = ChatPointsTTV.getTikTok().getClients().get(hostName).getRoomInfo().getHost().getId().toString();

            if (FollowerLog.wasFollowing(Platforms.TIKTOK, hostId, event.getUser().getId().toString())) return;
            FollowerLog.addFollower(Platforms.TIKTOK, hostId, event.getUser().getId().toString());
        }

        EventManager.onEvent(new EventInformation(TikTokEventType.FOLLOW, hostName, event.getUser().getProfileName()));
    }

    public void onShare(TikTokShareEvent event, String hostName) {
        EventManager.onEvent(new EventInformation(TikTokEventType.SHARE, hostName, event.getUser().getProfileName()));
    }
}
