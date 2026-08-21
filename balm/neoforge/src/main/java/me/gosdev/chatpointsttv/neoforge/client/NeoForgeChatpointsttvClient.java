package me.gosdev.chatpointsttv.neoforge.client;

import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import me.gosdev.chatpointsttv.ChatPointsTTVBalm;
import me.gosdev.chatpointsttv.client.ChatPointsTTVBalmClient;

@Mod(value = ChatPointsTTVBalm.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeChatpointsttvClient {

    public NeoForgeChatpointsttvClient(IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modEventBus);
        BalmClient.initializeMod(ChatPointsTTVBalm.MOD_ID, context, ChatPointsTTVBalmClient::initialize);
    }
}
