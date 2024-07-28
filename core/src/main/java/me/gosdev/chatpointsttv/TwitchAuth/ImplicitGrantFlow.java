package me.gosdev.chatpointsttv.TwitchAuth;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class ImplicitGrantFlow {
    private static ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    public static AuthenticationCallbackServer server = new AuthenticationCallbackServer(3000);
    private final static String AuthURL = "https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=" + ChatPointsTTV.getClientID() + "&redirect_uri=http://localhost:3000&scope="+plugin.scopes;


    public static CompletableFuture<String> getAccessToken(CommandSender p) {
        CompletableFuture<String> future = new CompletableFuture<>();
        server = new AuthenticationCallbackServer(3000);
        if (ChatPointsTTV.getTwitchClient() != null) {
            ChatPointsTTV.getTwitchClient().close();
        }

        if (p == Bukkit.getServer().getConsoleSender()) {
            String msg = "Link your Twitch account to set ChatPointsTTV up. Open this link in your browser to login:\n" + AuthURL;
            p.sendMessage(msg);
        } else {
            String msg = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "--------------- ChatPointsTTV ---------------\n" + ChatColor.RESET + ChatColor.WHITE + "Link your Twitch account to set ChatPointsTTV up";
            ComponentBuilder formatted = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "[Click here to login with Twitch]");
            
            BaseComponent btn = formatted.create()[0];

            btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));
            btn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, AuthURL));

            ChatPointsTTV.getUtils().sendMessage(p, new BaseComponent[]{new ComponentBuilder(msg + "\n").create()[0], btn});
        }
        
        int serverCloseId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                server.stop();
            }
        }, 6000L);// 60 L == 3 sec, 20 ticks == 1 sec

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                    if(server.getAccessToken() != null) {;
                        server.stop();
                        Bukkit.getScheduler().cancelTask(serverCloseId);
                        future.complete(server.getAccessToken());
                    }
                } catch(IOException e) {
                    e.getMessage();
                }
            }
        });
        return future;
    }
}
