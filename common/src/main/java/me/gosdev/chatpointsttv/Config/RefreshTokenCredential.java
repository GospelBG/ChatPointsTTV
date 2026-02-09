package me.gosdev.chatpointsttv.Config;

public class RefreshTokenCredential {
    private final String accessToken;
    private final String refreshToken;

    public RefreshTokenCredential(String lastAccessToken, String refreshToken) {
        this.accessToken = lastAccessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
