package me.gosdev.chatpointsttv.Generic;

import java.io.IOException;
import java.util.List;

public interface ConfigFile {
    Boolean contains(String key);
    List<String> getKeys(String key);
    void set(String key, Object value);
    List<String> getSectionKeys(String key);
    Boolean isSection(String key);
    List<String> getStringList(String key);
    Boolean isList(String key);
    Boolean isString(String key);
    String getString(String key);

    void reload();
}
