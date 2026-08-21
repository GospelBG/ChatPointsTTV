package me.gosdev.chatpointsttv.Config;

import me.gosdev.chatpointsttv.AlertMode;
import net.blay09.mods.balm.platform.config.reflection.Comment;
import net.blay09.mods.balm.platform.config.reflection.Config;
import net.minecraft.util.StringRepresentable;

@Config(value = "chatpointsttv", type = "common")
public class BalmGeneralConfig {
    public boolean enableTwitch = true;

    public boolean enableTikTok = true;

    @Comment("To prevent leaking Device Codes, this option will not show Device Codes used for logging in in the chat. Instead, you will need to hover to see the code.")
    public boolean hideLoginCodes = false;

    @Comment("Whether events should be ignored if the streamer is offline.")
    public boolean ignoreOfflineStreamers = false;

    @Comment("Whether stream chat messages should show up on the in-game chat.")
    public boolean showChat = true;

    @Comment("Whether events should be printed to the console. (exampleViewer cheered 300 bits; exampleViewer has subscribed with a Tier 1 sub; ...)")
    public boolean logEvents = true;

    @Comment("In-game event alerts mode.")
    public BalmAlertMode ingameAlerts = BalmAlertMode.CHAT;

    public enum BalmAlertMode implements StringRepresentable {
        NONE(AlertMode.NONE),
        CHAT(AlertMode.CHAT),
        TITLE(AlertMode.TITLE),
        ALL(AlertMode.ALL);

        private final AlertMode alertMode;

        BalmAlertMode(AlertMode alertMode) {
            this.alertMode = alertMode;
        }

        public AlertMode toAlertMode() {
            return alertMode;
        }

        @Override
        public String getSerializedName() {
            return alertMode.name().toLowerCase();
        }

        public static BalmAlertMode fromAlertMode(AlertMode alertMode) {
            for (BalmAlertMode value : values()) {
                if (value.alertMode == alertMode) {
                    return value;
                }
            }

            return NONE;
        }
    }

    @Comment("GLOBAL CONFIGURATION:\n" +
            "Adds glow effect to spawned mobs")
    public boolean mobGlow = true;

    @Comment("Adds the player's name to spawned mobs")
    public boolean displayNameOnMob = true;
}
