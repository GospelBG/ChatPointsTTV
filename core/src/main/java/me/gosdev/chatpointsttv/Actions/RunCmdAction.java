package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Generic.GenericSender;

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
        GenericSender sender;
        if (target.equalsIgnoreCase("CONSOLE")) {
            sender = ChatPointsTTV.getConsole();
        } else if (target.equalsIgnoreCase("TARGET")) {
            for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                if (p.hasPermission(ChatPointsTTV.permissions.TARGET)) {
                    p.runCommand(command);
                }
            }
            return;
        } else {
            sender = ChatPointsTTV.getLoader().getPlayer(target);
            if (sender == null || !ChatPointsTTV.getLoader().getOnlinePlayers().contains(sender)) {
                throw new RuntimeException("Couldn't find player " + target + ".");
            }
        }

        sender.runCommand(command);
    }
}
