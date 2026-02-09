package me.gosdev.chatpointsttv.Chat;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class SpigotChatEvent {
    public static ClickEvent.Action fromGenericClickEvent(ChatEvent.ClickAction action) {
        switch (action) {
            case OPEN_URL:
                return ClickEvent.Action.OPEN_URL;

            case RUN_COMMAND:
                return ClickEvent.Action.RUN_COMMAND;

            case COMPLETE_COMMAND:
                return ClickEvent.Action.SUGGEST_COMMAND;

            default:
                ChatPointsTTV.log.error("No such Click Action (" + action + ")");
                return null;
        }
    }

    public static HoverEvent.Action fromGenericHoverEvent(ChatEvent.HoverAction action) {
        switch (action) {
            case SHOW_TEXT:
                return HoverEvent.Action.SHOW_TEXT;

            default:
                ChatPointsTTV.log.error("No such Hover Action (" + action + ")");
                return null;
        }
    }
}
