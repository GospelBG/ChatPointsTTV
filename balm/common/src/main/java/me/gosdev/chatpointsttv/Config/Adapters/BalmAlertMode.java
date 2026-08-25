package me.gosdev.chatpointsttv.Config.Adapters;

import me.gosdev.chatpointsttv.AlertMode;
import net.minecraft.util.StringRepresentable;

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