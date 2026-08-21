package me.gosdev.chatpointsttv.Commands;

import com.mojang.brigadier.context.CommandContext;
import me.gosdev.chatpointsttv.Balm.BalmPlayer;
import me.gosdev.chatpointsttv.CommandController;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class BalmCommandController {
    public static int help(CommandContext<CommandSourceStack> context) {
        CommandController.help(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int reload(CommandContext<CommandSourceStack> context) {
        CommandController.reload(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };

    public static int set(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("test"));
        return 1;
    };

    public static int status(CommandContext<CommandSourceStack> context) {
        CommandController.status(new BalmPlayer(context.getSource().getPlayer()));
        return 1;
    };
}
