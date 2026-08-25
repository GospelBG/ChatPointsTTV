package me.gosdev.chatpointsttv.Balm;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Generic.AccountsManager;
import me.gosdev.chatpointsttv.Generic.ConfigFile;
import me.gosdev.chatpointsttv.Platforms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BalmAccountManager implements AccountsManager {

    private ConfigFile getAccountsConfig() {
        return ChatPointsTTV.getInstance().config.getAccounts();
    }

    @Override
    public HashMap<String, String> getCredentials(Platforms plat, String user) {
        ConfigFile accounts = getAccountsConfig();
        String platName = plat.getName().toLowerCase();
        
        if (accounts.contains(platName + "." + user)) {
            HashMap<String, String> map = new HashMap<>();
            
            for (String key : accounts.getSectionKeys(platName + "." + user)) {
                map.put(key, accounts.getString(platName + "." + user + "." + key));
            }
            
            return map;
        } else {
            return null;
        }
    }

    @Override
    public List<String> getAccounts(Platforms plat) {
        ConfigFile accounts = getAccountsConfig();
        String platName = plat.getName().toLowerCase();

        if (plat.equals(Platforms.TIKTOK)) {
            List<String> list = accounts.getStringList(platName);
            return list != null ? list : new ArrayList<>();
        } else {
            return accounts.getSectionKeys(platName);
        }
    }

    @Override
    public void saveAccount(Platforms plat, String user, Optional<HashMap<String, String>> credential) {
        ConfigFile accounts = getAccountsConfig();
        String platName = plat.getName().toLowerCase();

        if (credential.isPresent()) {
            for (String key : credential.get().keySet()) {
                accounts.set(platName + "." + user + "." + key, credential.get().get(key));
            }
        } else { // Just store user id
            List<String> platformAccounts = accounts.getStringList(platName);
            if (platformAccounts == null) {
                platformAccounts = new ArrayList<>();
            }
            if (platformAccounts.contains(user)) return;
            platformAccounts.add(user);
            accounts.set(platName, platformAccounts);
        }
    }

    @Override
    public void removeAccount(Platforms plat, String user) {
        ConfigFile accounts = getAccountsConfig();
        String platName = plat.getName().toLowerCase();
        
        if (plat.equals(Platforms.TIKTOK)) {
            List<String> platformAccounts = accounts.getStringList(platName);
            if (platformAccounts != null && platformAccounts.contains(user)) {
                platformAccounts.remove(user);
                accounts.set(platName, platformAccounts);
            }
        } else {
            accounts.set(platName + "." + user, null);
        }
    }

    @Override
    public Boolean hasPlatform(Platforms plat) {
        List<String> accounts = getAccounts(plat);
        return accounts != null && !accounts.isEmpty();
    }
}
