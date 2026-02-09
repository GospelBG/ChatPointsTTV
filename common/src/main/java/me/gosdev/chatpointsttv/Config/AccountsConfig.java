package me.gosdev.chatpointsttv.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ConfigFile;
import me.gosdev.chatpointsttv.Platforms;

public class AccountsConfig {
    private final ConfigFile accountsFile;

    private final HashMap<String, OAuth2Credential> twitchCredentials = new HashMap<>();
    private List<String> tiktokUsernames = new ArrayList<>();

    public AccountsConfig(ConfigFile file) {
        this.accountsFile = file;

        for (String userId : file.getKeys("twitch")) {
            String accessToken = accountsFile.getString("twitch." + userId + ".access_token");
            String refreshToken = accountsFile.getString("twitch." + userId + ".refresh_token");

            twitchCredentials.put(userId, new OAuth2Credential("twitch", accessToken, refreshToken, userId, null, null, null));
        }
        tiktokUsernames = file.getStringList("tiktok");
    }
    
    public void save() {
        try {
            accountsFile.save();
        } catch (IOException e) {
            ChatPointsTTV.log.error("ChatPointsTTV: There was an error saving account session credentials.");
        }
    }

    public List<String> getStoredUsers(Platforms platform) {
        switch (platform.toString()) {
            case "TWITCH":
                return new ArrayList<>(twitchCredentials.keySet());

            case "TIKTOK":
                return tiktokUsernames;

            default:
                ChatPointsTTV.log.error("Invalid platform specified.");
                return null;
        }
    }

    public void removeAccount(Platforms platform, String user) {
        switch (platform) {
            case TWITCH:
                twitchCredentials.remove(user);
                break;

            case TIKTOK:
                tiktokUsernames.remove(user);
                break;

            default:
                ChatPointsTTV.log.error("Invalid platform specified.");
                break;
        }

        this.save();
    }

    public OAuth2Credential getTwitchOAuth(String userId) {
        if (twitchCredentials.containsKey(userId)) {
            return twitchCredentials.get(userId);
        } else {
            ChatPointsTTV.log.error("Couldn't find User ID " + userId);
            return null;
        }
    }


    public void saveCredential(String userId, Optional<OAuth2Credential> credential) {
        if (credential.isPresent()) {
            twitchCredentials.put(userId, credential.get());

            accountsFile.set("twitch." + userId + ".access_token", credential.get().getAccessToken());
            accountsFile.set("twitch." + userId + ".refresh_token", credential.get().getRefreshToken());
        } else {
            if (!tiktokUsernames.contains(userId)) {
                tiktokUsernames.add(userId);
            }

            accountsFile.set("tiktok", tiktokUsernames);
        }
        this.save();
    }
}
