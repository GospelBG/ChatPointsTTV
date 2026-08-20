package me.gosdev.chatpointsttv.Spigot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import me.gosdev.chatpointsttv.ChatPointsTTVSpigot;
import me.gosdev.chatpointsttv.Generic.AccountsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;

public class SpigotAccountsManager implements AccountsManager {
    private final File accountsFile;
    private final YamlConfiguration accounts;

    public SpigotAccountsManager() {
        accountsFile = new File(ChatPointsTTVSpigot.getPlugin().getDataFolder(), "accounts");
        accounts = YamlConfiguration.loadConfiguration(accountsFile);
    }

    public HashMap<String, String> getCredentials(Platforms plat, String user) {
        ConfigurationSection credentials = accounts.getConfigurationSection(plat.getName().toLowerCase());

        if (credentials.contains(user)) {
            HashMap<String, String> map = new HashMap<>();

            for (String key : credentials.getConfigurationSection(user).getKeys(false)) {
                map.put(key, credentials.getString(user + "." + key));
            }

            return map;
        } else {
            return null;
        }
    }

    public List<String> getAccounts(Platforms plat) {
        if (plat.equals(Platforms.TIKTOK)) {
            return accounts.getStringList(plat.getName().toLowerCase());
        } else {
            return accounts.getConfigurationSection(plat.getName().toLowerCase()).getKeys(false).stream().toList();
        }
    }

    public void saveAccount(Platforms plat, String user, Optional<HashMap<String, String>> credential) {
        if (credential.isPresent()) {
            if (!accounts.isConfigurationSection(plat.getName().toLowerCase()))  accounts.createSection(plat.getName().toLowerCase());
            ConfigurationSection platformAccounts = accounts.getConfigurationSection(plat.getName().toLowerCase()).createSection(user);

            for (String key : credential.get().keySet()) {
                platformAccounts.set(key, credential.get().get(key));
            }
        } else { // Just store user id
            List<String> platformAccounts = accounts.getStringList(plat.getName().toLowerCase());
            if (platformAccounts.contains(user)) return;
            platformAccounts.add(user);
            accounts.set(plat.getName().toLowerCase(), platformAccounts);
        }
        save();
    }

    public void removeAccount(Platforms plat, String user) {
        accounts.getConfigurationSection(plat.getName().toLowerCase()).set(user, null);
        save();
    }

    @Override
    public Boolean hasPlatform(Platforms plat) {
        if (getAccounts(plat) != null && !getAccounts(plat).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private void save() {
        try {
            accounts.save(accountsFile);
        } catch (IOException e) {
            ChatPointsTTV.log.error("ChatPointsTTV: There was an issue saving account session credentials.");
        }
    }
}
