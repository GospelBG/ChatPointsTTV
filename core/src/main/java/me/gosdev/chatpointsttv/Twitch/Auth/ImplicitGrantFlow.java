package me.gosdev.chatpointsttv.Twitch.Auth;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ImplicitGrantFlow {
    public static AuthenticationCallbackServer server = new AuthenticationCallbackServer(3000);

    static Utils utils = ChatPointsTTV.getUtils();

    public static CompletableFuture<String> getAccessToken(ChatPointsTTV plugin, CommandSender p, String clientID) {
        CompletableFuture<String> future = new CompletableFuture<>();
        String AuthURL = "https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=" + clientID + "&redirect_uri=http://localhost:3000&scope=" + TwitchClient.scopes;

        server = new AuthenticationCallbackServer(3000);
        if (ChatPointsTTV.getTwitch().getClient() != null) {
            ChatPointsTTV.getTwitch().getClient().close();
        }

        if (p == Bukkit.getServer().getConsoleSender()) {
            TextComponent msg = new TextComponent("Link your Twitch account to set ChatPointsTTV up. Open this link in your browser to login:\n" + AuthURL);
            utils.sendMessage(p, msg);
        } else {
            String msg = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "--------------- ChatPointsTTV ---------------\n" + ChatColor.RESET + ChatColor.WHITE + "Link your Twitch account to set ChatPointsTTV up";
            BaseComponent btn = new TextComponent(ChatColor.LIGHT_PURPLE + "[Click here to login with Twitch]");
            btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));
            btn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, AuthURL));

            utils.sendMessage(p, new BaseComponent[]{new ComponentBuilder(msg + "\n").create()[0], btn});
        }
        
        int serverCloseId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            server.stop();
        }, 6000L); // 60 L == 3 sec, 20 ticks == 1 sec

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!server.isRunning()) {
                    server.start();
                }
                if(server.getAccessToken() != null) {
                    server.stop();
                    Bukkit.getScheduler().cancelTask(serverCloseId);
                    future.complete(server.getAccessToken());
                }
            } catch(IOException e) {
                plugin.log.warning(e.getMessage());
            }
        });
        return future;
    }
}
