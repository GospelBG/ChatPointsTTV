<img src="icon.png" style="width: 3vw; float: left"/> <h1>ChatPointsTTV</h1>

ChatPointsTTV is a Bukkit plugin that helps you to create interactions between your Twitch stream and your Minecraft world. Set up your own rewards in exchange of channel points, follows, Bits, subs and more! Spawn mobs, give items or run any command when an event is triggered.  
There's a whole range of use cases and possibilities to connect with your audience!


## Download
This GitHub repository contains all the source code of the plugin. You can download a compiled .jar file through any of this links.
- GitHub Releases: https://github.com/GospelBG/ChatPointsTTV/releases  
- CurseForge: https://www.curseforge.com/minecraft/bukkit-plugins/chatpointsttv  
- Modrinth: https://modrinth.com/plugin/chatpointsttv
- SpigotMC: https://www.spigotmc.org/resources/chatpointsttv.112532/
- Hangar: https://hangar.papermc.io/GosDev/ChatPointsTTV

These are the only official download mirrors. Any downloads besides of these links may not be genuine.

## **Setup**
1. Install the plugin into your Minecraft server.  

2. Open your channel's [Twitch Dashboard](https://dashboard.twitch.tv) and create your custom Channel Points Rewards. (optional)
> [!TIP]
>  If needed, you can copy-paste the names of the Channel Points Rewards into a text document for later use.
  
3. Set your [config.yml](core/src/main/resources/config.yml) up. Adjust the settings and setup events for rewards, donations... You will need to link a Twitch account in order to use the Twitch API (the linked account **needs to own/moderate all Twitch channels** set in config.yml) There are two ways to link your Twitch account to the plugin:  

    - **Using a key-based authentication** *(recommended)*:  
    You will need a Client ID and Access token. You can get one mannually or through a website as [Twitch Token Generator](https://twitchtokengenerator.com/quick/7RWW5IlqTa). Make sure to add all the [needed scopes](#twitch-scopes) (the above link is already set to allow the needed scopes).  
    Uncomment `CUSTOM_CLIENT_ID` and `CUSTOM_ACCESS_TOKEN` on the config file and add your Client ID and Token onto the corresponding fields.  
    Once the server has started you just need to run `/twitch link` and your account will be automatically logged in. You may need to refresh the token when it expires.

    - **Log in through a browser**:  
    You won't need any extra modification in your config.yml file. You will just need to run `/twitch link` in-game and open the provided link. You may need to log in your Twitch account and authorise the app. Once you finish the log in process you can close the browser and your account will be linked.
> [!WARNING]  
> Currently due to API limitations **it's only possible to use the browser method if the login link is opened with the same machine the server is being ran on**. You also have to repeat this process each time you start the server or reload the plugin.

4. Set up permissions for:
    - linking/reloading (`chatpointsttv.manage`).
    - the target player(s) (`chatpointsttv.target`).
    - people you want the reward messages broadcasted to (`chatpointsttv.broadcast`).  

    · See [Permissions](#permissions) for more information.

> [!NOTE]
> The config file for the plugin should be in 'plugins/ChatPointsTTV/config.yml'.

## **config.yml docs**
To reset the original configuration, delete `config.yml` and reload the plugin. The file will regenerate automatically.  
*Sections with a (\*) are required to be changed in order to the plugin to be used.*
* **Channel Username***: The channel(s) that will be listened for rewards, bits and subs. (In case of multiple channels, they must be added as a list: `["channel_1", "channel_2", "..."]`)  
* **Custom Client ID**: Client ID used for key-based authorization. Leave commented if it's not being used.  
* **Custom Access Token**: Access token used for key-based authorization. Leave commented if it's not being used.  
* **Show Chat**: If enabled, your stream chat will be shown in-game to all players in the server.  
* **Chat Blacklist**: List of usernames of chat bots and other users that will be ignored for the in-game stream chat.
* **Channel Point Rewards***: A list containing all **channel point rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `{REWARD_NAME}: {ACTION}`, replacing `{REWARD_NAME}` with the **exact** reward name that is on Twitch and `{ACTION}` with the desired action to run.  
* **Follow Rewards***: A list with all the actions that will be executed when the channel gets a new follower.
* **Cheer Rewards***: A list containing all **cheer rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `AMOUNT: {ACTION}`, replacing `AMOUNT` with the minimal ammount of bits that will be needed to trigger the event and `{ACTION}` with the desired action to run.
* **Sub Rewards***: A list containing all **subscription rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `TWITCH_PRIME/TIER1/TIER2/TIER3: {ACTION}`.  Replace `{ACTION}` with the desired action to run.
* **Gift Rewards***
A list containing all **subscription gifts rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `AMOUNT: {ACTION}`, replacing `AMOUNT` with the minimal ammount of subscriptions that will be needed to be gifted in order to trigger the event and `{ACTION}` with the desired action to run.
* **Mob Glow**: Whether the spawned mobs should have a glowing effect (highlighted and visible through blocks).
* **Display Name on Mob**: Whether the spawned mobs should have the name of the user who triggered the action.
* **Log Events**: Determines whether all events will be logged. `true` means that all channel point rewards, cheers, subscriptions and gifts will be logged into the console. `false` means that they won't be logged.
* **In-game Alerts Mode***: How will the game notify of an event. Valid options: `chat`, `title`, `all`, `none`. Choosing `none`  will disable In-game alerts.
* **Reward Name Bold**: Determines whether the reward name is displayed in bold letters in the title banner to people with the `chatpointsttv.broadcast` permission.
* **Colors**: Allows you to customize every color of the title messages. Set the wanted strings to any Minecraft Color Name (`RED`, `GOLD`, `DARK_PURPLE`...).  
You can leave this section unmodified, as there are default colors set up in the original file
* **Strings**: Allows you to customize all title texts displayed to people with the `chatpointsttv.broadcast` permission. You may want to translate these strings to your language for a better experience. Don't add spaces before or after the double quotes as the plugin already does this for you. English strings are set up in the file by default.

## Commands
This plugin is controlled by the `/twitch` command followed by one of the following arguments:
* `/twitch link [method]`  
    Links your Account. If a key-based credential is set it will use it. Otherwise will show the button for logging in through a browser (only works if the user logs in with the same machine as the server).  
    You can specify the method to use through the optional `[method]` parameter.  
    **Valid options**: `key`, `browser`
* `/twitch unlink`  
    Unlinks your account and disables the plugin. You may need to log in again if you used the browser method.

* `/twitch status`  
    Shows some plugin information such as: version, listened channel, linked account and connection status.

* `/twitch reload`  
    Unlinks your account, reloads the configuration file and restarts the plugin.

## Reward Actions
Currently, there are 2 types of actions:
- Spawning entities  
    **Format**: `SPAWN <ENTITY NAME> [AMOUNT]`  
    **Example**: `SPAWN CREEPER 2`  
    *This action will spawn the menctioned entities for each player that is set up as a target (with the `chatpointsttv.target` permission). The example action will spawn 2 Creepers on each player's location.*  
    
- Running commands  
    **Format**: `RUN <TARGET / CONSOLE> <COMMAND>`  
    **Example**: `RUN TARGET DAMAGE @S 2`  
    *This action will run the command as the console a single time, or as each player once. Command arguments are allowed. The example action will substract each player a heart from their health.*  

- Giving Items  
    **Format**: `GIVE <ITEM> [AMOUNT]`  
    **Example**: `GIVE DIAMOND 1`
    *This action will give the stablished amount of the set items to all players with the `chatpointsttv.target` permission. The example action will give all players (with the "target" permission) a diamond.*  

- Explosions
    **FORMAT**: `TNT <AMOUNT> [FUSE TIME IN TICKS]`  
    **Example**: `TNT 1 0`
    *This action will spawn a TNT to all players with the `chatpointsttv.target` permission that will explode after the specified fuse time. The example action will spawn a TNT that will explode instantly to all players.*  

> [!TIP]
> Argument names surrounded by <> means that it is a required argument.
> Arguments surrounded by [] are optional.

You should set up your events in your config file following this format:
```
TYPE_REWARDS:
    - KEY:
        - Action 1
        - Action 2
        - ...
```
or
```
TYPE_REWARDS:
    - KEY:
        - STREAMER:
            - Action 1
            - Action 2
            - ...

        - default:
            - Action
```
Whereas `TYPE_REWARDS` is replaces with the appropiate config key that is already on the file, `KEY` with the channel points reward name, subscription tier or minimal amount of bits/subs.  
> [!IMPORTANT]  
> **For follow events you shouldn't add reward keys.** See placeholders on the default [config.yml](core/src/main/resources/config.yml).

> [!TIP]
> You can now target multiple channels. If you do so, you can target some events to a specific channel following the second format. You can still follow the first example if you don't aim to target specific channels. 

## Twitch Scopes
The latest version of the plugin needs the following scopes to function propertly:  
* `channel:read:redemptions`: Needed to read channel point redemptions.
* `user:read:moderated_channels`: Needed to check if user has permission to listen for follow events. (API only allows to listen to own/moderated channels)
* `moderator:read:followers`: Needed to be able to listen for follows.
* `bits:read`: Needed to listen for cheers.
* `channel:read:subscriptions`: Needed to listen for subscriptions and gifts.
* `chat:read` and `user:read:chat`: Needed to show your stream chat in-game and use EventSub API.
* `user:bot` and `channel:bot`: Joins a stream chat to listen for subs.

## Permissions
- TARGET  
    **ID**: `chatpointsttv.target`  
    **Behaviour**: Configured actions will trigger on all players with this permission.  

- BROADCAST  
    **ID**: `chatpointsttv.broadast`  
    **Behaviour**: All players with this permission will get a banner message when an action is triggered.  

- MANAGE  
    **ID**: `chatpointsttv.manage`  
    **Behaviour**: Players with this permission will be allowed to run the `/twitch` command and link a Twitch account.  

## **Credits**
Thanks to [Twitch4J](https://twitch4j.github.io/) for the Java libraries used in this mod to communicate with the Twitch API.

Thanks to [urgrue](https://github.com/urgrue/Java-Twitch-Api-Wrapper) for the code for Twitch authentification.

Thanks to [Mystiflow](https://github.com/Mystiflow) for their [color convertion code](https://gist.github.com/Mystiflow/c42f45bac9916c84e381155f72a96d84).

