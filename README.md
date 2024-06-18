<img src="icon.png" style="width: 7vw; float: left; padding-right: 10px; vertical-align: center;"/> <h1>ChatPointsTTV</h1>

ChatPointsTTV is a Bukkit plugin that helps you to create interactions between your Twitch stream and your Minecraft world. Set up your own rewards in exchange of channel points, Bits or subs! Spawn mobs or run any command of your choice when an event is triggered, giving you a whole range of use cases and possibilities to connect with your audience!

~~You will need to setup a Twitch Application in the Dev Console.~~ **As of v2.0, you no longer need a Twitch app, as that is replaced with an OAuth login.**

## Download
This GitHub repository contains all the source code of the plugin. You can download a compiled .jar file through any of this links.
- GitHub Releases: https://github.com/GospelBG/ChatPointsTTV/releases  
- CurseForge: https://www.curseforge.com/minecraft/bukkit-plugins/chatpointsttv  
- Modrinth: https://modrinth.com/plugin/chatpointsttv
- SpigotMC: https://www.spigotmc.org/resources/chatpointsttv.112532/

These are the only official download mirrors. Any downloads besides of these links may not be genuine.

## **Setup**
1. Install the plugin into your Minecraft server.  
~~2. Create an app using [Twitch Developer Console](https://dev.twitch.tv/console). (Refer to ["Setting up a Twitch app"](#setting-up-a-twitch-app)).~~  
~~3. Copy your Client ID and your secret token to your [config.yml](/src/main/resources/config.yml).*~~
2. Open your [Twitch Dashboard](https://dashboard.twitch.tv) and create your custom Channel Points Rewards.
> [!TIP]
>  If needed, you can copy-paste the names of the Channel Points Rewards into a text document for later use.
  
3. Add to your [config.yml](/src/main/resources/config.yml) your newly created Twitch rewards, and setup the actions.
> [!NOTE]
> Copy the name **EXACTLY** as it is on Twitch.
  
4. Customize the text and it's formatting (optional).
5. Set up permissions for:
    - linking/reloading (`chatpointsttv.manage`).
    - the target player(s) (`chatpointsttv.target`).
    - people you want the reward messages broadcasted to (`chatpointsttv.broadcast`).  

    · See [Permissions](#permissions) for more information.

> [!TIP]
> The config file for the plugin should be in 'plugins/ChatPointsTTV/config.yml'.

## **config.yml docs**
To reset the original configuration, delete `config.yml` and reload the plugin. The file will regenerate automatically.  
Sections with a (*) are required to be changed in order to the plugin to be used.
* **Channel Username***: The channel that will be listened for rewards, bits and subs
* **Rewards***: A list containing all **channel point rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `{REWARD_NAME}: {ACTION}`, replacing `{REWARD_NAME}` with the **exact** reward name that is on Twitch and `{ACTION}` with the desired action to run.  

* **Cheer Rewards***: A list containing all **cheer rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `AMOUNT: {ACTION}`, replacing `AMOUNT` with the minimal ammount of bits that will be needed to trigger the event and `{ACTION}` with the desired action to run.
* **Sub Rewards***: A list containing all **subscription rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `TWITCH_PRIME/TIER1/TIER2/TIER3: {ACTION}`.  Replace `{ACTION}` with the desired action to run.
* **Gift Rewards***
A list containing all **subscription gifts rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `AMOUNT: {ACTION}`, replacing `AMOUNT` with the minimal ammount of subscriptions that will be needed to be gifted in order to trigger the event and `{ACTION}` with the desired action to run.
* **Log Event**: Determines whether all events will be logged. `true` means that all channel point rewards, cheers, subscriptions and gifts will be logged into the console. `false` means that they won't be logged.
* **Reward Name Bold**: Determines whether the reward name is displayed in bold letters in the title banner to people with the `chatpointsttv.broadcast` permission.
* **Colors**: Allows you to customize every color of the title messages. Set the wanted strings to any Minecraft Color Name (`RED`, `GOLD`, `DARK_PURPLE`...).  
You can leave this section unmodified, as there are default colors set up in the original file
* **Strings**: Allows you to customize all title texts displayed to people with the `chatpointsttv.broadcast` permission. You may want to translate these strings to your language for a better experience. Don't add spaces before or after the double quotes as the plugin already does this for you. English strings are set up in the file by default.

### Reward Actions
Currently, there are 2 types of actions:
- Spawning entities  
    **Format**: `SPAWN <ENTITY NAME> <AMOUNT>`  
    **Example**: `SPAWN CREEPER 2`  
    *This action will spawn the menctioned entities for each player that is set up as a target (with the `chatpointsttv.target` permission). The example action will spawn 2 Creepers on each player's location.*
    
- Running commands  
    **Format**: `RUN <TARGET / CONSOLE> <COMMAND>`  
    **Example**: `RUN TARGET DAMAGE @S 2`  
    *This action will run the command as the console a single time, or as each player once. Command arguments are allowed. The example action will substract each player a heart from their health.*

### Permissions
As of version 2.0 there are 3 permissions for the plugin:
- TARGET  
    **ID**: `chatpointsttv.target`  
    **Behaviour**: Configured actions will trigger on all players with this permission.  

- BROADCAST  
    **ID**: `chatpointsttv.broadast`  
    **Behaviour**: All players with this permission will get a banner message when an action is triggered.  

- MANAGE  
    **ID**: `chatpointsttv.manage`  
    **Behaviour**: Players with this permission will be allowed to run the `/twitch` command and link a Twitch account.  

### **Credits**
Thanks to [Twitch4J](https://twitch4j.github.io/) for the Java libraries used in this mod to communicate with the Twitch API.

Thanks to [Async Twitch API Wrapper](https://github.com/urgrue/Java-Twitch-Api-Wrapper) for the code for the Twitch authentification.
