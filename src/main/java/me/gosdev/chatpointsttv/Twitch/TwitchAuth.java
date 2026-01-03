package me.gosdev.chatpointsttv.Twitch;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.github.philippheuer.credentialmanager.authcontroller.DeviceFlowController;
import com.github.philippheuer.credentialmanager.domain.DeviceAuthorization;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class TwitchAuth {
    public static DeviceFlowController flowController;
        public static void getDeviceCode(ChatPointsTTV plugin, CommandSender p) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            p.sendMessage(ChatColor.RED + "You must start the Twitch Client first!");
            return;
        }
        p.sendMessage(ChatColor.GRAY + "Please wait...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Boolean shouldHideCode = ChatPointsTTV.getPlugin().config.getBoolean("HIDE_LOGIN_CODES", false);
            DeviceAuthorization auth = TwitchAuth.link(p);
            TextComponent comp = new TextComponent("\n  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "Account Linking" + ChatColor.RESET + " -------------\n\n");
            if (p.equals(Bukkit.getConsoleSender())) {
                comp.addExtra(new TextComponent(ChatColor.LIGHT_PURPLE + "Go to " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "https://twitch.tv/activate" + ChatColor.LIGHT_PURPLE + " and enter the code: " + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode()));
            } else {
                TextComponent button = new TextComponent("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + ChatColor.UNDERLINE + "[Click here]");
                button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, auth.getVerificationUri()));
                button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new TextComponent("Click to open in browser")).create()));
    
                TextComponent code;
                comp.addExtra(button);
                if (shouldHideCode) {
                    comp.addExtra(ChatColor.LIGHT_PURPLE + " to login with Twitch.\n" + ChatColor.GRAY + ChatColor.ITALIC + "Careful! Clicking the button above will show your device code as part of the link");
                    comp.addExtra(ChatColor.LIGHT_PURPLE + "\n\nYou may also go to " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "https://twitch.tv/activate" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " and enter this code: ");
                    code = new TextComponent("" + ChatColor.DARK_PURPLE + ChatColor.MAGIC + ChatColor.BOLD + "ABCDEFGH");
                    code.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode()).create()));

                    code.addExtra("" + ChatColor.GRAY + ChatColor.ITALIC + " (Hover to view)");
                } else {
                    comp.addExtra(ChatColor.LIGHT_PURPLE + " or go to " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "https://twitch.tv/activate" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " and enter this code:\n\n" + ChatColor.GRAY + "   ➡ ");
                    code = new TextComponent("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode());
                }
                comp.addExtra(code);
                comp.addExtra("\n");
            }
            p.spigot().sendMessage(comp);
        });
    }
    
    public static DeviceAuthorization link(CommandSender p) {
        OAuth2IdentityProvider identityProvider = new TwitchIdentityProvider(TwitchClient.CLIENT_ID, null, null);
        flowController = new DeviceFlowController();
        DeviceAuthorization auth = flowController.startOAuth2DeviceAuthorizationGrantType(identityProvider, TwitchClient.scopes,  response -> {
            if (response.getCredential() != null) {
                TwitchClient client = ChatPointsTTV.getTwitch();

                if (client != null && client.isStarted()) {
                    client.getExecutor().submit(() -> {
                        client.link(p, identityProvider.getAdditionalCredentialInformation(response.getCredential()).get());
                    });
                }
            } else {
                switch(response.getError()) {
                    case ACCESS_DENIED:
                        p.sendMessage("Authorization was denied by user.");
                        break;
                        
                    case EXPIRED_TOKEN:
                        p.sendMessage("Device code has expired. Please try again.");
                        break;
                        
                    default:
                        p.sendMessage("An error occurred while linking your account.");
                        break;
                }
            }
        });
        return auth;
    }
}
