package me.gosdev.chatpointsttv.Config;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Config.Adapters.BalmAlertMode;
import net.blay09.mods.balm.platform.config.reflection.Comment;
import net.blay09.mods.balm.platform.config.reflection.Config;
import net.blay09.mods.balm.platform.config.reflection.NestedType;
import net.minecraft.util.StringRepresentable;

import java.util.List;

@Config(value = "chatpointsttv-twitch", type = "twitch")
public class TwitchConfig {
    @Comment("Automatically show, hide and complete redemptions of Channel Point Rewards created by ChatPointsTTV.")
    public boolean manageChannelPointRewards = true;

    @Comment("If enabled, follow events from people who had already been following the channel will be ignored.")
    public boolean followSpamProtection = true;

    @NestedType(String.class)
    @Comment("Add chat bot and other unwanted chatter usernames (in lowercase) to this list to prevent their messages displaying on the in-game chat.")
    public List<String> chatBlacklist = List.of();

    @Comment("OVERRIDEN CONFIGURATION:")
    public OverridenConfig overridenConfig = new OverridenConfig();
}
