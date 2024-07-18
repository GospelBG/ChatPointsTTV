package me.gosdev.chatpointsttv.TikTok;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events;
import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Utils.Utils;

public class TikTokEventHandler {
    ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    Utils utils = ChatPointsTTV.getUtils();

    Boolean logEvents = plugin.config.getBoolean("LOG_EVENTS");

    ChatColor action_color = ChatPointsTTV.getChatColors().get("ACTION_COLOR");
    ChatColor user_color = ChatPointsTTV.getChatColors().get("USER_COLOR");

    public void onGift(TikTokGiftComboEvent event) {
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getUser().getName() + " has gifted " + event.getCombo() + " " + event.getGift().getName());

        String user = event.getUser().getProfileName();
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("GIFT_STRING");

        for (Reward reward : Rewards.getRewards(rewardType.TIKTOK_GIFT)) {
            if (event.getGift().getName().equalsIgnoreCase(reward.getEvent())) {
                Events.displayTitle(user, custom_string, "1 x " + event.getGift().getName(), action_color, user_color, plugin.rewardBold);
                for (String cmd : reward.getCommands()) {
                    String[] parts = cmd.split(" ", 2);
                    try {
                        Events.runAction(parts[0], parts[1], event.getUser().getProfileName());
                    } catch (Exception e) {
                        plugin.log.warning(e.toString());
                    }
                }
            }
        }
    }

    public void onFollow(TikTokFollowEvent event) {
        if (logEvents) utils.sendMessage(Bukkit.getConsoleSender(), event.getUser().getName() + " has started following!");

        String user = event.getUser().getProfileName();
        String custom_string = ChatPointsTTV.getRedemptionStrings().get("FOLLOWED_STRING");

        Events.displayTitle(user, custom_string, "", action_color, user_color, plugin.rewardBold);
            for (String cmd : Rewards.getRewards(rewardType.TIKTOK_FOLLOW).get(0).getCommands()) {
                String[] parts = cmd.split(" ", 2);
                try {
                    Events.runAction(parts[0], parts[1], event.getUser().getProfileName());
                } catch (Exception e) {
                    plugin.log.warning(e.toString());
                }
            }
    }
}
