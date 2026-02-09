package me.gosdev.chatpointsttv.Chat;

public class ChatEvent {
    public enum ClickAction {
        OPEN_URL,
        RUN_COMMAND,
        COMPLETE_COMMAND,
    }

    public enum HoverAction {
        SHOW_TEXT
    }

    public static class ClickEvent {
        private final ClickAction action;
        private final String value;

        public ClickEvent(ClickAction action, String value) {
            this.action = action;
            this.value = value;
        }

        public ClickAction getAction() {
            return action;
        }

        public String getValue() {
            return value;
        }
    }

    public static class HoverEvent {
        private final HoverAction action;
        private final String value;

        public HoverEvent(HoverAction action, String value) {
            this.action = action;
            this.value = value;
        }

        public HoverAction getAction() {
            return action;
        }

        public String getValue() {
            return value;
        }
    }
}
