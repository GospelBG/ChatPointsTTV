<img src="icon.png" style="width: 40px; float: left; padding-right: 10px; vertical-align: baseline;"/> <h1>ChatPointsTTV</h1>

ChatPointsTTV is a Spigot plugin that helps you to create interactions between your Twitch chat and your Minecraft world. You can set up custom channel points rewards on your channel that can spawn mobs and execute commands, giving you a huge variety of possibilities for rewards.

~~You will need to setup a Twitch Application in the Dev Console.~~ As of v2.0, you no longer need a Twitch app, as that is replaced with an OAuth login.

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

*: The config file for the plugin should be in 'plugins/ChatPointsTTV/config.yml'.

## **config.yml docs**
If you need an original config.yml file, you can copy it from [here](/src/main/resources/plugin.yml).
* **Channel Username**: The channel that will be listened for rewards, bits and subs
* **Rewards**: A list containing all **channel point rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `{REWARD_NAME}: {ACTION}`, replacing `{REWARD_NAME}` with the **exact** reward name that is on Twitch and `{ACTION}` with the desired action to run.  

* **Cheer Rewards**: A list containing all **channel point rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `{100/500/1000...}: {ACTION}`, replacing `{100/500/1000...}` with the minimal ammount of bits needed and `{ACTION}` with the desired action to run.
* **Sub Rewards**: A list containing all **channel point rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `{TWITCH_PRIME/TIER1/TIER2/TIER3}: {ACTION}`, replacing `{TWITCH_PRIME/TIER1/TIER2/TIER3}` with the desired subscription tier. Also you can replace it by an amount, this will listen for subscription gifts and will run if an ammount of any sub equal or greater is gifted. Replace `{ACTION}` with the desired action to run.
* **Reward Name Bold**: Determines wheter the reward name is displayed in bold letters in the title banner to people with the `chatpointsttv.broadcast` permission.
* **Colors**: Allows you to customize every color of the title messages. Set the wanted strings to any Minecraft Color Name (`RED`, `GOLD`, `DARK_PURPLE`...).  
You can leave this section unmodified, as there are default colors set up in the original file
* **Strings**: Allows you to customize all title texts displayed to people with the `chatpointsttv.broadcast` permission. You may want to translate these strings to your language for a better experience. Don't add spaces before or after the double quotes as the plugin already does this for you. English strings are set up in the file by default.

### Reward Actions
Currently, there are 2 types of actions:
- Spawning entities  
    **Format**: `SPAWN <ENTITY NAME> <AMOUNT>`  
    **Example**: `SPAWN CREEPER 2`  
    This action will spawn the menctioned entities for each player that is set up as a target (with the `chatpointsttv.target` permission). The example action will spawn 2 Creepers on each player's location.
    
- Running commands  
    **Format**: `RUN <TARGET / CONSOLE> <COMMAND>`  
    **Example**: `RUN TARGET DAMAGE @S 2`  
    This action will run the command as the console a single time, or as each player once. Command arguments are allowed. The example action will substract each player a heart from their health.

## **Setting up a Twitch app (CURRENTLY NOT REQUIRED)**
1. Go to [Twitch's Developer Console](https://dev.twitch.tv/console) and login.
2. Click "Register your application".
3. Fill in a name, and for the OAuth redirect, type 'https://localhost'. Choose a category and generate a secret. (copy your app ID and your secret into config.yml).

### **Credits**
Thanks to [Twitch4J](https://twitch4j.github.io/) for the Java libraries used in this mod to communicate with the Twitch API.

Thanks to [Async Twitch API Wrapper](https://github.com/urgrue/Java-Twitch-Api-Wrapper) for the code for the Twitch authentication.
