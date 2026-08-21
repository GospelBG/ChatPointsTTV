package me.gosdev.chatpointsttv.Commands;

import me.gosdev.chatpointsttv.Generic.AccountsManager;
import me.gosdev.chatpointsttv.Platforms;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BalmAccountManager implements AccountsManager {
    @Override
    public HashMap<String, String> getCredentials(Platforms plat, String user) {
        return null;
    }

    @Override
    public List<String> getAccounts(Platforms plat) {
        return List.of();
    }

    @Override
    public void saveAccount(Platforms plat, String user, Optional<HashMap<String, String>> credential) {

    }

    @Override
    public void removeAccount(Platforms plat, String user) {

    }

    @Override
    public Boolean hasPlatform(Platforms plat) {
        return null;
    }
}
