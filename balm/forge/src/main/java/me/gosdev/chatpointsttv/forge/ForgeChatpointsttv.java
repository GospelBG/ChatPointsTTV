package me.gosdev.chatpointsttv.forge;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.forge.platform.runtime.ForgeLoadContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import me.gosdev.chatpointsttv.ChatPointsTTVBalm;
import me.gosdev.chatpointsttv.client.ChatPointsTTVBalmClient;

@Mod(ChatPointsTTVBalm.MOD_ID)
public class ForgeChatpointsttv {

    public ForgeChatpointsttv(FMLJavaModLoadingContext context) {
        final var loadContext = new ForgeLoadContext(context.getModBusGroup());
        Balm.initializeMod(ChatPointsTTVBalm.MOD_ID, loadContext, ChatPointsTTVBalm::initialize);
        if (FMLEnvironment.dist.isClient()) {
            BalmClient.initializeMod(ChatPointsTTVBalm.MOD_ID, loadContext, ChatPointsTTVBalmClient::initialize);
        }
    }

}
