package me.gosdev.chatpointsttv.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;

public class Translatable {
    private static final String DEFAULT_LOCALE = "en_us";
    private static final Gson GSON = new Gson();
    private static final HashMap<String, String> TRANSLATIONS = new HashMap<>();

    public static void loadTranslationsFile(String lang) {
        TRANSLATIONS.clear();
        String filePath = "/assets/chatpointsttv/lang/" + lang + ".json";

        try (InputStream is = Translatable.class.getResourceAsStream(filePath)) {
            if (is == null) {
                if (lang.equals(DEFAULT_LOCALE)) return; // Fail silently
                loadTranslationsFile(DEFAULT_LOCALE);
            } else {
                JsonObject json = GSON.fromJson(new InputStreamReader(is), JsonObject.class);
                for (String key : json.keySet()) {
                    TRANSLATIONS.put(key, json.get(key).getAsString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getString(String key, Object... args) {
        String translation = TRANSLATIONS.getOrDefault(key, key);

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                translation = translation.replaceFirst("%s", "{" + i + "}");
            }
            return MessageFormat.format(translation, args);
        }

        return translation;
    }
}
