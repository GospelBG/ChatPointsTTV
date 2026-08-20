package me.gosdev.chatpointsttv.Spigot;

import java.util.logging.Logger;

import me.gosdev.chatpointsttv.Generic.GenericLogger;
import org.bukkit.Bukkit;

public class SpigotLog implements GenericLogger {
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
