package me.gosdev.chatpointsttv.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.gosdev.chatpointsttv.Generic.ConfigFile;
import net.blay09.mods.balm.Balm;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BalmJsonConfigFile implements ConfigFile {
    private final File file;
    private final Gson gson;
    private JsonObject root;

    public BalmJsonConfigFile(String fileName) {
        this.file = Balm.config().getConfigDir().toPath().resolve(fileName).toFile();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        reload();
    }

    @Override
    public void reload() {
        if (!file.exists()) {
            root = new JsonObject();
            save();
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            root = gson.fromJson(reader, JsonObject.class);
            if (root == null) {
                root = new JsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            root = new JsonObject();
        }
    }

    private void save() {
        try {
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(root, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonElement traverse(String key) {
        if (key == null || key.isEmpty()) return root;
        String[] parts = key.split("\\.");
        JsonElement current = root;
        for (String part : parts) {
            if (current != null && current.isJsonObject()) {
                current = current.getAsJsonObject().get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    @Override
    public Boolean contains(String key) {
        return traverse(key) != null;
    }

    @Override
    public List<String> getKeys(String key) {
        return getSectionKeys(key);
    }

    @Override
    public void set(String key, Object value) {
        String[] parts = key.split("\\.");
        JsonObject current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.has(parts[i]) || !current.get(parts[i]).isJsonObject()) {
                current.add(parts[i], new JsonObject());
            }
            current = current.getAsJsonObject(parts[i]);
        }
        
        String lastKey = parts[parts.length - 1];
        if (value == null) {
            current.remove(lastKey);
        } else if (value instanceof String) {
            current.addProperty(lastKey, (String) value);
        } else if (value instanceof Boolean) {
            current.addProperty(lastKey, (Boolean) value);
        } else if (value instanceof Number) {
            current.addProperty(lastKey, (Number) value);
        } else if (value instanceof List) {
            JsonArray arr = new JsonArray();
            for (Object obj : (List<?>) value) {
                arr.add(obj.toString());
            }
            current.add(lastKey, arr);
        } else {
            current.addProperty(lastKey, value.toString());
        }
        save();
    }

    @Override
    public List<String> getSectionKeys(String key) {
        JsonElement el = traverse(key);
        List<String> keys = new ArrayList<>();
        if (el != null && el.isJsonObject()) {
            for (String k : el.getAsJsonObject().keySet()) {
                keys.add(k);
            }
        }
        return keys;
    }

    @Override
    public Boolean isSection(String key) {
        JsonElement el = traverse(key);
        return el != null && el.isJsonObject();
    }

    @Override
    public List<String> getStringList(String key) {
        JsonElement el = traverse(key);
        List<String> list = new ArrayList<>();
        if (el != null && el.isJsonArray()) {
            for (JsonElement item : el.getAsJsonArray()) {
                list.add(item.getAsString());
            }
        }
        return list;
    }

    @Override
    public Boolean isList(String key) {
        JsonElement el = traverse(key);
        return el != null && el.isJsonArray();
    }

    @Override
    public Boolean isString(String key) {
        JsonElement el = traverse(key);
        return el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString();
    }

    @Override
    public String getString(String key) {
        JsonElement el = traverse(key);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            return el.getAsString();
        }
        return null;
    }
}
