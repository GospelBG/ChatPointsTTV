package me.gosdev.chatpointsttv.Commands;

import com.mojang.brigadier.context.CommandContext;
import me.gosdev.chatpointsttv.Balm.BalmPlayer;
import me.gosdev.chatpointsttv.TikTok.TikTokCommands;
import net.minecraft.commands.CommandSourceStack;

public class TikTokCommandController {
    public static int accounts(CommandContext<CommandSourceStack> context) {
        TikTokCommands.accounts(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int help(CommandContext<CommandSourceStack> context) {
        TikTokCommands.help(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int link(CommandContext<CommandSourceStack> context) {
        TikTokCommands.link(new BalmPlayer(context.getSource().getPlayer()), context.getArgument("username", String.class));
        return 1;
    };

    public static int reload(CommandContext<CommandSourceStack> context) {
        TikTokCommands.reload(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int status(CommandContext<CommandSourceStack> context) {
        TikTokCommands.displayStatus(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int start(CommandContext<CommandSourceStack> context) {
        TikTokCommands.start(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int stop(CommandContext<CommandSourceStack> context) {
        TikTokCommands.stop(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int test(CommandContext<CommandSourceStack> context) {
        TikTokCommands.test(new BalmPlayer(context.getSource().getPlayer()), null);
        return 1;
    };
}
