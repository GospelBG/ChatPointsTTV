package me.gosdev.chatpointsttv.Spigot;

import me.gosdev.chatpointsttv.Chat.SpigotChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.ChatPointsTTVSpigot;
import me.gosdev.chatpointsttv.Utils.ChatComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SpigotSender implements GenericSender {
    private final CommandSender sender;

    public SpigotSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpigotSender that = (SpigotSender) obj;
        return sender.equals(that.sender);
    }

    @Override
    public int hashCode() {
        return sender.hashCode();
    }


    @Override
    public void sendMessage(ChatComponent comp) {
        sender.spigot().sendMessage(createSpigotComponents(comp));
    }

    @Override
    public boolean hasPermission(permissions perm) {
        return sender.hasPermission(perm.permission_id);
    }

    @Override
    public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }
    
    private BaseComponent createSpigotComponents(ChatComponent comp) {
        TextComponent newComp = new TextComponent(comp.getText());

        if (comp.getClickEvent() != null) {
            newComp.setClickEvent(new ClickEvent(SpigotChatEvent.fromGenericClickEvent(comp.getClickEvent().getAction()), comp.getClickEvent().getValue()));
        }

        if (comp.getHoverEvent() != null) {
            newComp.setHoverEvent(new HoverEvent(SpigotChatEvent.fromGenericHoverEvent(comp.getHoverEvent().getAction()), new ComponentBuilder(comp.getHoverEvent().getValue()).create()));
        }

        if (!comp.getExtra().isEmpty()) {
            for (ChatComponent extraComp : comp.getExtra()) {
                newComp.addExtra(createSpigotComponents(extraComp));
            }
        }

        return newComp;
    }

    @Override
    public void runCommand(String cmd) {
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
            Bukkit.dispatchCommand(sender, cmd);
        });
    }
}
