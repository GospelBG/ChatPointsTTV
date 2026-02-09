package me.gosdev.chatpointsttv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONArray;

//import net.md_5.bungee.api.ChatColor;

public class VersionCheck {
    private final static String url = "https://api.modrinth.com/v2/project/nN0gRvoO/version";

    public final static String download_url = "https://modrinth.com/plugin/chatpointsttv";
    public static String latestVersion;
    public static Boolean runningLatest = true;
    
    public static Boolean check() {
        try {
            StringBuilder result = new StringBuilder();
            HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            conn.disconnect();
            JSONArray json = new JSONArray(result.toString());
            latestVersion = json.getJSONObject(0).getString("version_number");

            runningLatest = ChatPointsTTV.getLoader().getVersion().equals(latestVersion);

        } catch (IOException | URISyntaxException | NullPointerException e) {
            ChatPointsTTV.log.warn("Couldn't fetch latest version." + e.toString());
            return true;
        }
        return runningLatest;
    }
}
