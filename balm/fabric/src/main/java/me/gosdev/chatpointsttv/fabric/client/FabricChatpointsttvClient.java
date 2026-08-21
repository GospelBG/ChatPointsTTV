package me.gosdev.chatpointsttv.fabric.client;

import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ClientModInitializer;
import me.gosdev.chatpointsttv.ChatPointsTTVBalm;
import me.gosdev.chatpointsttv.client.ChatPointsTTVBalmClient;

public class FabricChatpointsttvClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BalmClient.initializeMod(ChatPointsTTVBalm.MOD_ID, FabricLoadContext.INSTANCE, ChatPointsTTVBalmClient::initialize);
    }
}
