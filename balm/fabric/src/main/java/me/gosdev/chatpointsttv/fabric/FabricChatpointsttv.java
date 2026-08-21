package me.gosdev.chatpointsttv.fabric;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ModInitializer;
import me.gosdev.chatpointsttv.ChatPointsTTVBalm;

public class FabricChatpointsttv implements ModInitializer {
    @Override
    public void onInitialize() {
        Balm.initializeMod(ChatPointsTTVBalm.MOD_ID, FabricLoadContext.INSTANCE, ChatPointsTTVBalm::initialize);
    }
}
