package me.gosdev.chatpointsttv.Twitch;

import me.gosdev.chatpointsttv.Generic.GenericSender;

import com.github.philippheuer.credentialmanager.authcontroller.DeviceFlowController;
import com.github.philippheuer.credentialmanager.domain.DeviceAuthorization;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Utils.Translatable;

public class TwitchAuth {
    public static final String VERIFICATION_URL = "https://twitch.tv/activate";
    public static DeviceFlowController flowController;

    
    public static DeviceAuthorization authorize(GenericSender p) {
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
                        p.sendMessage(Translatable.getString("twitch.message.auth.denied"));
                        break;
                        
                    case EXPIRED_TOKEN:
                        p.sendMessage(Translatable.getString("twitch.message.auth.expired"));
                        break;
                        
                    default:
                        p.sendMessage(Translatable.getString("twitch.message.auth.error"));
                        break;
                }
            }
        });
        return auth;
    }
}
