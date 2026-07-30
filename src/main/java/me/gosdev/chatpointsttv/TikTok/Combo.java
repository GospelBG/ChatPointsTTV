package me.gosdev.chatpointsttv.TikTok;

import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class Combo {
    private final int TIMEOUT = 5;
    private int timer = 0;

    private final String userName;
    private final String userId;
    private final String hostName;
    private int count;

    public Combo(TikTokLikeEvent eventInfo, String hostName, int count) {
        this.userName = eventInfo.getUser().getName().toString();
        this.userId = eventInfo.getUser().getId().toString();
        this.hostName = hostName;
        this.count = count;

        countdown();
    }

    public void add(int count) {
        timer = TIMEOUT;
        this.count += count;
    }

    public int getCount() {
        return count;
    }

    public String getUserName() {
        return userName;
    }

    public String getHostName() {
        return hostName;
    }

    public void onComboFinish() {
        //ChatPointsTTV.log.info(userName + " Combo finished: " + count);
        ChatPointsTTV.getTikTok().getEventHandler().activeCombos.remove(userId);
        ChatPointsTTV.getTikTok().getEventHandler().onLikeComboFinish(this);
    }

    private void countdown() {
        Bukkit.getScheduler().runTaskAsynchronously(ChatPointsTTV.getPlugin(), () -> {
            try {
                for (timer = TIMEOUT; timer >= 0; timer--) {
                    TimeUnit.SECONDS.sleep(1);
                }

                onComboFinish();
            } catch (InterruptedException ignored) {}
        });
    }
}
