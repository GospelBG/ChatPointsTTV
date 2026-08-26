package me.gosdev.chatpointsttv;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.gosdev.chatpointsttv.Balm.BalmAccountManager;
import me.gosdev.chatpointsttv.Balm.BalmLoader;
import me.gosdev.chatpointsttv.Commands.*;
import me.gosdev.chatpointsttv.Config.BalmConfigs;
import me.gosdev.chatpointsttv.Config.GeneralConfig;
import me.gosdev.chatpointsttv.Config.TikTokConfig;
import me.gosdev.chatpointsttv.Config.TwitchConfig;
import me.gosdev.chatpointsttv.Utils.Translatable;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.blay09.mods.balm.core.BalmRegistrars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatPointsTTVBalm {

    public static final Logger logger = LoggerFactory.getLogger(ChatPointsTTVBalm.class);

    public static final String MOD_ID = "chatpointsttv";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static GeneralConfig config() {
        return Balm.config().getActiveConfig(GeneralConfig.class);
    }

    private static ChatPointsTTV chatPointsTTV;

    public static void initialize(BalmRegistrars registrars) {
        Balm.config().registerConfig(GeneralConfig.class);
        Balm.config().registerConfig(TwitchConfig.class);
        //Balm.config().registerConfig(TikTokConfig.class);

        Balm.commands().register(dispatcher -> {
            dispatcher.register(Commands.literal("cpttv")
                .executes(BalmCommandController::help)

                .then(Commands.literal("help")
                    .executes(BalmCommandController::help))

                .then(Commands.literal("reload")
                        .executes(BalmCommandController::reload))

                .then(Commands.literal("set")
                        .executes(BalmCommandController::set))

                .then(Commands.literal("status")
                    .executes(BalmCommandController::status))
            );

            dispatcher.register(Commands.literal("twitch")
                .executes(TwitchCommandController::help)

                    .then(Commands.literal("accounts")
                        .executes(TwitchCommandController::accounts))

                    .then(Commands.literal("help")
                            .executes(TwitchCommandController::help))

                    .then(Commands.literal("link")
                            .executes(TwitchCommandController::link))

                    .then(Commands.literal("reload")
                            .executes(TwitchCommandController::reload))

                    .then(Commands.literal("status")
                            .executes(TwitchCommandController::status))

                    .then(Commands.literal("start")
                            .executes(TwitchCommandController::start))

                    .then(Commands.literal("stop")
                            .executes(TwitchCommandController::stop))

                    .then(Commands.literal("test")
                            .executes(TwitchCommandController::test))
            );

            dispatcher.register(Commands.literal("tiktok")
                    .executes(TikTokCommandController::help)

                    .then(Commands.literal("accounts")
                            .executes(TikTokCommandController::accounts))

                    .then(Commands.literal("help")
                            .executes(TikTokCommandController::help))

                    .then(Commands.literal("link")
                            .then(Commands.argument("username", StringArgumentType.word())
                                .executes(TikTokCommandController::link)))

                    .then(Commands.literal("reload")
                            .executes(TikTokCommandController::reload))

                    .then(Commands.literal("status")
                            .executes(TikTokCommandController::status))

                    .then(Commands.literal("start")
                            .executes(TikTokCommandController::start))

                    .then(Commands.literal("stop")
                            .executes(TikTokCommandController::stop))

                    .then(Commands.literal("test")
                            .executes(TikTokCommandController::test))
            );
        });

        ServerPlayerCallback.Join.EVENT.register(player -> {
            Translatable.loadTranslationsFile(Minecraft.getInstance().options.languageCode);
            chatPointsTTV = new ChatPointsTTV(new BalmLoader(), new BalmConfigs(), new BalmAccountManager());
            chatPointsTTV.onEnable();
        });

        ServerPlayerCallback.Leave.EVENT.register(player -> {
            chatPointsTTV.onDisable();
            chatPointsTTV = null;
        });
    }

}
