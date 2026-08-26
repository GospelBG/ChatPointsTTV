package me.gosdev.chatpointsttv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.Translatable;
import org.json.JSONArray;

public class VersionCheck {
    private final static String url = "https://api.modrinth.com/v2/project/nN0gRvoO/version";

    public final static String download_url = "https://modrinth.com/plugin/chatpointsttv";
    public static String latestVersion;
    public static Boolean runningLatest = true;
    
    public static void check() {
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

            if (!ChatPointsTTV.getInstance().version.equals(latestVersion)) {
                runningLatest = false;
                ChatPointsTTV.getConsole().sendMessage(Translatable.getString("versioncheck.new_version", ChatColor.YELLOW, latestVersion, download_url));
            }

        } catch (IOException | URISyntaxException e) {
            ChatPointsTTV.log.warn(Translatable.getString("versioncheck.error", e.toString()));
        }
    }
}
