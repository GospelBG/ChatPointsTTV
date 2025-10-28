package me.gosdev.chatpointsttv.TikTok;

import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastSocialMessage;

public class EventTest {
    public void sendFollowEvent (String host, String user) {
        TikTokFollowEvent e = TikTokFollowEvent.of(user);
        
    }
}
