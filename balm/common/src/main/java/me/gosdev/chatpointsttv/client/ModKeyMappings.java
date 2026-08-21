package me.gosdev.chatpointsttv.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.gosdev.chatpointsttv.ChatPointsTTVBalm;
import net.blay09.mods.kuma.api.InputBinding;
import net.blay09.mods.kuma.api.Kuma;
import net.blay09.mods.kuma.api.ManagedKeyMapping;

import static me.gosdev.chatpointsttv.ChatPointsTTVBalm.id;

public class ModKeyMappings {

    public static ManagedKeyMapping yourKey;

    public static void initialize() {
        yourKey = Kuma.createKeyMapping(id("your_key"))
                .withDefault(InputBinding.key(InputConstants.KEY_B))
                .handleScreenInput(event -> {
                    ChatPointsTTVBalm.logger.info("B was pressed - " + ChatPointsTTVBalm.MOD_ID);
                    return true;
                })
                .build();
    }
}
