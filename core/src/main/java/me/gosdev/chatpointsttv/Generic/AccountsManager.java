package me.gosdev.chatpointsttv.Generic;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import me.gosdev.chatpointsttv.Platforms;

public interface AccountsManager {
    public HashMap<String, String> getCredentials(Platforms plat, String user);

    public List<String> getAccounts(Platforms plat);

    public void saveAccount(Platforms plat, String user, Optional<HashMap<String, String>> credential);

    public void removeAccount(Platforms plat, String user);

    public Boolean hasPlatform(Platforms plat);

}
