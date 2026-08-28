package me.gosdev.chatpointsttv.Commands;

import com.mojang.brigadier.context.CommandContext;
import me.gosdev.chatpointsttv.Balm.BalmPlayer;
import me.gosdev.chatpointsttv.Twitch.TwitchCommands;
import net.minecraft.commands.CommandSourceStack;

import java.util.Arrays;

public class TwitchCommandController {
    public static int accounts(CommandContext<CommandSourceStack> context) {
        TwitchCommands.accounts(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int help(CommandContext<CommandSourceStack> context) {
        TwitchCommands.help(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int link(CommandContext<CommandSourceStack> context) {
        TwitchCommands.link(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int reload(CommandContext<CommandSourceStack> context) {
        TwitchCommands.reload(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int status(CommandContext<CommandSourceStack> context) {
        TwitchCommands.displayStatus(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int start(CommandContext<CommandSourceStack> context) {
        TwitchCommands.start(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int stop(CommandContext<CommandSourceStack> context) {
        TwitchCommands.stop(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int test(CommandContext<CommandSourceStack> context) {
        TwitchCommands.test(new BalmPlayer(context.getSource().getPlayer()), Arrays.stream(context.getInput().split(" ")).skip(1).toArray(String[]::new));
        return 1;
    };
}
