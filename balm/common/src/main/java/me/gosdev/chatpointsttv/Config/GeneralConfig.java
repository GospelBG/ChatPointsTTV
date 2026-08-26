package me.gosdev.chatpointsttv.Config;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Config.Adapters.BalmAlertMode;
import net.blay09.mods.balm.platform.config.reflection.Comment;
import net.blay09.mods.balm.platform.config.reflection.Config;
import net.minecraft.util.StringRepresentable;

@Config(value = "chatpointsttv", type = "common")
public class GeneralConfig {
    public boolean enableTwitch = true;

    public boolean enableTikTok = true;

    @Comment("To prevent leaking Device Codes, this option will not show Device Codes used for logging in in the chat. Instead, you will need to hover to see the code.")
    public boolean hideLoginCodes = false;

    @Comment("Whether events should be ignored if the streamer is offline.")
    public boolean ignoreOfflineStreamers = false;

    @Comment("Whether stream chat messages should show up on the in-game chat.")
    public boolean showChat = true;

    @Comment("Whether events should be printed to the console. (exampleViewer cheered 300 bits; exampleViewer has subscribed with a Tier 1 sub; ...)")
    public boolean logEvents = true;

    @Comment("GLOBAL CONFIGURATION:")
    public OverridenConfig overridenConfig = new OverridenConfig();
}
