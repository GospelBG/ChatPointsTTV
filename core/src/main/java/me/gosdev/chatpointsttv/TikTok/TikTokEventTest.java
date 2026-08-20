package me.gosdev.chatpointsttv.TikTok;

import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;
import io.github.jwdeveloper.tiktok.data.models.users.User;

public class TikTokEventTest {
    public static TikTokFollowEvent FollowEvent(String user) {
        return TikTokFollowEvent.of(generateUser(user));
    }

    public static TikTokLikeEvent LikeEvent(String user, Integer amount) {
        return TikTokLikeEvent.of(generateUser(user), amount);
    }

    public static TikTokGiftComboEvent GiftEvent(String user, User host, Gift item, Integer amount) {
        return TikTokGiftComboEvent.of(item, host, generateUser(user), amount, GiftComboStateType.Finished);
    }

    public static TikTokShareEvent ShareEvent(String user) {
        return TikTokShareEvent.of(generateUser(user), 1);
    }

    public static User generateUser(String username) {
        return new User(-1l, username.toLowerCase(), username, null);
    }
}
