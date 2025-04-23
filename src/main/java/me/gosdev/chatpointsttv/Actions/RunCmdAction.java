package me.gosdev.chatpointsttv.Actions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.gosdev.chatpointsttv.ChatPointsTTV;

public class RunCmdAction extends BaseAction {
    private final String target;
    private final String command;

    public RunCmdAction (String sender, String command) {
        this.target = sender;
        this.command = command.replace("/", "");
    }

    @Override
    public void run() {
        CommandSender sender;
        if (target.equalsIgnoreCase("CONSOLE")) {
            sender = Bukkit.getConsoleSender();
        } else if (target.equalsIgnoreCase("TARGET")) {
            for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
                if (p.hasPermission(ChatPointsTTV.permissions.TARGET.permission_id)) {
                    Bukkit.dispatchCommand(p, command);
                }
            }
            return;
        } else {
            sender = Bukkit.getPlayer(target);
            if (sender == null || !Bukkit.getOnlinePlayers().contains((Player) sender)) {
                throw new RuntimeException("Couldn't find player " + target + ".");
            }
        }

        Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> { // Commands should only be dispatched synchronously.
            Bukkit.dispatchCommand(sender, command);
        });
    }
}
