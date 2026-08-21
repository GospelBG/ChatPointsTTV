package me.gosdev.chatpointsttv;

import com.mojang.brigadier.CommandDispatcher;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Utils.ChatComponent;
import net.blay09.mods.balm.Balm;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class ConsoleSender implements GenericSender {

    @Override
    public void sendMessage(String message) {
        ChatPointsTTVBalm.logger.info(message);
    }

    @Override
    public void sendMessage(ChatComponent comp) {
        ChatPointsTTVBalm.logger.info(comp.getText().replaceAll("§.", ""));
    }

    @Override
    public void runCommand(String cmd) {
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }

        MinecraftServer server = Balm.getRuntime().platform().server();

        if (server != null) {
            String finalCmd = cmd;
            server.execute(() -> {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), finalCmd);
            });
        }
    }

    @Override
    public boolean hasPermission(ChatPointsTTV.permissions perm) {
        return true;
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public String getName() {
        return "Console";
    }
}
