package me.gosdev.chatpointsttv.Generic;

import me.gosdev.chatpointsttv.Chat.ChatComponent;
import me.gosdev.chatpointsttv.ChatPointsTTV;

public interface GenericSender {
    void sendMessage(String message);
    void sendMessage(ChatComponent comp);

    void runCommand(String cmd);

    boolean hasPermission(ChatPointsTTV.permissions perm);

    boolean isConsole();
    
    String getName();
}
