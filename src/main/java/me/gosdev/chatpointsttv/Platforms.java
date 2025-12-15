package me.gosdev.chatpointsttv;

public enum Platforms {
    TWITCH("Twitch"),
    TIKTOK("TikTok");

    private final String name;

    private Platforms(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
