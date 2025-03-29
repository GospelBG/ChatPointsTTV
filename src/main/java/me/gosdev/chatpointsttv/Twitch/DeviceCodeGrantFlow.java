package me.gosdev.chatpointsttv.Twitch;

import org.bukkit.command.CommandSender;

import com.github.philippheuer.credentialmanager.authcontroller.DeviceFlowController;
import com.github.philippheuer.credentialmanager.domain.DeviceAuthorization;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;

public class DeviceCodeGrantFlow {
    public static DeviceFlowController flowController;

    public static DeviceAuthorization link(CommandSender p, TwitchClient client) {
        OAuth2IdentityProvider identityProvider = new TwitchIdentityProvider(TwitchClient.getClientID(), null, null);
        flowController = new DeviceFlowController();
        DeviceAuthorization auth = flowController.startOAuth2DeviceAuthorizationGrantType(identityProvider, TwitchClient.scopes,  response -> {
            if (response.getCredential() != null) {
                client.link(p, identityProvider.getAdditionalCredentialInformation(response.getCredential()).get());
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
