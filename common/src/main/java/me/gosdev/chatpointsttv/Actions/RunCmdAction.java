package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Generic.GenericSender;

public class RunCmdAction implements BaseAction {
    public static final String ACTION_NAME = "RUN";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    private final String target;
    private final String command;

    public RunCmdAction (String sender, String command) {
        this.target = sender;
        this.command = command.replace("/", "");
    }

    @Override
    public void run(EventInformation ei) {
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
            if (sender == null) {
                throw new RuntimeException("Couldn't find player " + target + ".");
            }
        }
        sender.runCommand(command);
    }
}
