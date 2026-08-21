package me.gosdev.chatpointsttv.neoforge;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import me.gosdev.chatpointsttv.ChatPointsTTVBalm;

@Mod(ChatPointsTTVBalm.MOD_ID)
public class NeoForgeChatpointsttv {

    public NeoForgeChatpointsttv(IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modEventBus);
        Balm.initializeMod(ChatPointsTTVBalm.MOD_ID, context, ChatPointsTTVBalm::initialize);
    }
}
