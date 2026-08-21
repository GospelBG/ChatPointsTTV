package me.gosdev.chatpointsttv.Balm;

import me.gosdev.chatpointsttv.ChatPointsTTVBalm;
import me.gosdev.chatpointsttv.Generic.GenericLogger;
import org.slf4j.Logger;

public class BalmLog implements GenericLogger {
    private final Logger logger = ChatPointsTTVBalm.logger;

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }
}
