package me.gosdev.chatpointsttv.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;

public class AccountsManager {
    private final File accountsFile;
    private final YamlConfiguration accounts;

    public AccountsManager() {
        accountsFile = new File(ChatPointsTTV.getPlugin().getDataFolder(), "accounts");
        accounts = YamlConfiguration.loadConfiguration(accountsFile);
    }

    public ConfigurationSection getCredentials(Platforms plat, String user) {
        ConfigurationSection credentials = accounts.getConfigurationSection(plat.getName().toLowerCase());

        if (credentials.contains(user)) {
            return credentials.getConfigurationSection(user);
        } else {
            return null;
        }
    }

    public List<String> getAccounts(Platforms plat) {
        return accounts.getStringList(plat.getName().toLowerCase());
    }

    public void saveAccount(Platforms plat, String user, Optional<HashMap<String, String>> credential) {
        if (credential.isPresent()) {
            ConfigurationSection platformAccounts = accounts.getConfigurationSection(plat.getName().toLowerCase()).createSection(user);

            for (String key : credential.get().keySet()) {
                platformAccounts.set(key, credential.get().get(key));
            }
        } else { // Just store user
            List<String> platformAccounts = accounts.getStringList(plat.getName().toLowerCase());
            platformAccounts.add(user);
            accounts.set(plat.getName().toLowerCase(), platformAccounts);
        }
        save();
    }

    public void removeAccount(Platforms plat, String user) {
        accounts.getConfigurationSection(plat.getName().toLowerCase()).set(user, null);
        save();
    }

    private void save() {
        try {
            accounts.save(accountsFile);
        } catch (IOException e) {
            ChatPointsTTV.log.severe("ChatPointsTTV: There was an issue saving account session credentials.");
        }
    }
}
