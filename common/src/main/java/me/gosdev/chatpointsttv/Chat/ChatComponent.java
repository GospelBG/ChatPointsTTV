package me.gosdev.chatpointsttv.Chat;

import java.util.ArrayList;
import java.util.List;

import me.gosdev.chatpointsttv.Utils.ChatColor;

public class ChatComponent {
    private String text;
    private ChatColor color;
    private ChatEvent.ClickEvent clickEvent;
    private ChatEvent.HoverEvent hoverEvent;
    private List<ChatComponent> extra;

    public ChatComponent(String text) {
        this.text = text;
        this.extra = new ArrayList<>();
    }

    public ChatComponent setClickEvent(ChatEvent.ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public ChatComponent setHoverEvent(ChatEvent.HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
        return this;
    }

    public ChatComponent addExtra(ChatComponent extra) {
        this.extra.add(extra);
        return this;
    }

    public ChatComponent addExtra(String extra) {
        this.extra.add(new ChatComponent(extra));
        return this;
    }

    public String getText() {
        return text;
    }

    public ChatEvent.ClickEvent getClickEvent() {
        return clickEvent;
    }

    public ChatEvent.HoverEvent getHoverEvent() {
        return hoverEvent;
    }

    public List<ChatComponent> getExtra() {
        return extra;
    }
}
