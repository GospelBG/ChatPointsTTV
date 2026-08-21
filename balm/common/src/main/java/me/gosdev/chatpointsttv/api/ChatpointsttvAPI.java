package me.gosdev.chatpointsttv.api;

import java.lang.reflect.InvocationTargetException;

public class ChatpointsttvAPI {

    public static final String MOD_ID = "chatpointsttv";

    private static final InternalMethods __internalMethods;

    static {
        try {
            __internalMethods = (InternalMethods) Class.forName("me.gosdev.chatpointsttv.InternalMethodsImpl").getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
