package me.gosdev.chatpointsttv;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class SpigotLog implements CPTTV_Log {
    private static final Logger log = Bukkit.getLogger();

    @Override
    public void info(String msg) {
        log.info(msg);
    }

    @Override
    public void warn(String msg) {
        log.warning(msg);
    }

    @Override
    public void error(String msg) {
        log.severe(msg);
    }
}
