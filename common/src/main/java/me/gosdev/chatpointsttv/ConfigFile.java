package me.gosdev.chatpointsttv;

import java.io.IOException;
import java.util.List;

public interface ConfigFile {
    Boolean contains(String key);
    List<String> getKeys(String key);

    void set(String key, Object value);

    Boolean getBoolean(String key, Boolean def);

    String getString(String key);
    String getString(String key, String def);
    Boolean isString(String key);

    List<String> getSectionKeys(String key);
    Boolean isSection(String key);

    List<String> getStringList(String key);
    Boolean isList(String key);

    void save() throws IOException;
    void reload();
}
