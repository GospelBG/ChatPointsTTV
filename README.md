<img src="icon.png" style="width: 3vw; float: left"/> <h1>ChatPointsTTV</h1>
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/chatpointsttv?style=for-the-badge&color=97ca00&logo=modrinth)](https://modrinth.com/plugin/chatpointsttv)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/910370?style=for-the-badge&color=97ca00&logo=curseforge)](https://www.curseforge.com/minecraft/bukkit-plugins/chatpointsttv)
[![Spigot Downloads](https://img.shields.io/spiget/downloads/112532?style=for-the-badge&logo=spigotmc&color=97ca00)](https://www.spigotmc.org/resources/chatpointsttv-twitch-integration-for-streamers.112532/)
[![GitHub Downloads](https://img.shields.io/github/downloads/gospelbg/chatpointsttv/total?style=for-the-badge&color=97ca00&logo=github)](https://github.com/GospelBG/ChatPointsTTV/releases)


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

2. Set your [config.yml](core/src/main/resources/config.yml) and [twitch.yml](core/src/main/resources/twitch.yml) up. Adjust the settings and setup events for rewards, donations...  
> [!NOTE]
> You will need to link a Twitch account in order to connect use the Twitch API (the linked accounts **must be the channel owner**)  

3. Set up permissions for:
    - linking/reloading (`chatpointsttv.manage`).
    - the target player(s) (`chatpointsttv.target`).
    - people you want the reward messages broadcasted to (`chatpointsttv.broadcast`).  

    · See [Permissions](#permissions) for more information.  
    · A permissions plugin, such as [LuckPerms](https://luckperms.net/) is recommended.

4. Start your server and link your accounts  
    - Start the Twitch client, if it's stopped `/twitch start`  
    - Run `/twitch link` to get your device code  
    - Click the button on the chat or go to [https://twitch.tv/activate](https://twitch.tv/activate) and enter the code provided by the plugin if it did not autocomplete.  
    - Log in with your account and authorise the application.  
    - Return to Minecraft and wait for the linking process to complete.  

## **config.yml docs**
To reset the original configuration, delete `config.yml` and reload the plugin. The file will regenerate automatically.  
* **Enable Twitch**: If set to true, the Twitch client will automatically start.  
* **Ignore Offline Streamers**: If set to true, the plugin will only listen to the people who is currently streaming. This also includes chat messages.  
* **Ignore Offline Events**: If enabled, events from offline streamers will be ignored.  
* **Show Chat**: If enabled, your stream chat will be shown in-game to all players in the server.  
* **Log Events**: Determines whether all events will be logged. `true` means that all channel point rewards, cheers, subscriptions and gifts will be logged into the console. `false` means that they won't be logged.
* **In-game Alerts Mode**: How will the game notify of an event. Valid options: `chat`, `title`, `all`, `none`. Choosing `none`  will disable In-game alerts.
* **Colors**: Allows you to customize every color of the title messages. See a list of valid options in the [Bukkit API Docs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html).  

## **twitch.yml docs**
* **Channel Point Rewards**: A list containing all **channel point rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `{REWARD_NAME}: {ACTION}`, replacing `{REWARD_NAME}` with the **exact** reward name that is on Twitch and `{ACTION}` with the desired action to run.  
* **Follow Rewards**: A list with all the actions that will be executed when a channel gets a new follower.  
* **Cheer Rewards**: A list containing all **cheer rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `AMOUNT: {ACTION}`, replacing `AMOUNT` with the minimal ammount of bits that will be needed to trigger the event and `{ACTION}` with the desired action to run.  
* **Sub Rewards**: A list containing all **subscription rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `TWITCH_PRIME/TIER1/TIER2/TIER3: {ACTION}`.  Replace `{ACTION}` with the desired action to run.  
* **Gift Rewards**: A list containing all **subscription gifts rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: `AMOUNT: {ACTION}`, replacing `AMOUNT` with the minimal ammount of subscriptions that will be needed to be gifted in order to trigger the event and `{ACTION}` with the desired action to run.  
* **Raid Rewards**: A list containing all **raid rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: AMOUNT: {ACTION}, replacing AMOUNT with the minimal ammount of raid viewers that will be needed to trigger the event and {ACTION} with the desired action to run.  
* **Chat Blacklist**: List of usernames of chat bots and other users that will be ignored for the in-game stream chat.  

* **Mob Glow Override**: Overrides the global value set in the config.yml file for Twitch events.  
* **Display Name on Mob Override**: Overrides the global value set in the config.yml file for Twitch events.  
* **Colors Override**: Overrides the global colors set in the config.yml file for Twitch events. See a list of valid options in the [Bukkit API Docs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html).  

For a better streaming experience, it is possible to translate the plugin's messages into your language by editing the [localization.yml](src/main/resources/localization.yml) file.  


## Commands
This plugin is controlled by the `/twitch` command followed by one of the following arguments:
* `/twitch accounts`  
    Displays all connected accounts and allows the user to easily add or remove accounts.

* `/twitch link`  
    Generates a Twitch Device Code and starts the linking process.  

* `/twitch unlink [account]`  
    Unlinks a Twitch account. If no extra parameter is entered, it will unlink all accounts.  

* `/twitch status`  
    Shows some plugin information such as: version, listened channels and connection status.  

* `/twitch reload`  
    Reloads the configuration file and restarts the plugin.

* `/twitch start`  
    Starts the Twitch client. This is required to use the Twitch functionality.

* `/twitch stop`  
    Stops the Twitch client. This is useful to stop incoming events without the need of unlinking any accounts.  

* `/twitch test <event type> <...>`  
    Fires a custom test event. Useful for testing your configuration.  
    For channel point rewards that contains whitespaces in their name, it can be enclosed between double quotes.  
    **Valid event types**:
    - channelpoints
    - cheer
    - follow 
    - raid
    - sub
    - subgift

## Reward Actions
- Spawning entities  
    **Format**: `SPAWN <ENTITY NAME> [AMOUNT] [TARGET USER]`  
    **Example**: `SPAWN CREEPER 2 GospelBG`  
    *This action will spawn an entity for each player that has the `chatpointsttv.target` permission or a specific player (when the `[TARGET USER] field is used`). The example action will spawn 2 Creepers on each player's location.*  
    
- Running commands  
    **Format**: `RUN <TARGET USER / "TARGET" / "CONSOLE"> <COMMAND>`  
    **Example**: `RUN TARGET DAMAGE @S 2`  
    *This action will run a command as the console, all target players or a specific player.  
    The example action will substract each player a heart from their health.*  

- Giving Items  
    **Format**: `GIVE <ITEM> [AMOUNT] [TARGET USER]`  
    **Example**: `GIVE DIAMOND 1 GospelBG`
    *This action will give the established amount of the set items to all players with the `chatpointsttv.target` permission. The example action will give all players (with the "target" permission) a diamond.*  

- Giving Effects  
    **Format:**: `EFFECT {EFFECT NAME} {STRENGTH} {DURATION IN SECONDS} [TARGET PLAYER]`
    **Example**: `EFFECT JUMP_BOOST 2 60 GospelBG`
    *This action gives the set potion effect to all players with the `chatpointsttv.target` permission or a specific player (when the `[TARGET USER] field is used`)*

- Explosions  
    **FORMAT**: `TNT <AMOUNT> [FUSE TIME IN TICKS]`  
    **Example**: `TNT 1 0`
    *This action will spawn a TNT to all players with the `chatpointsttv.target` permission that will explode after the specified fuse time. The example action will spawn a TNT that will explode instantly to all players.*  

- Deleting Items  
    **FORMAT**: `DELETE <"ALL" / "HAND" / "RANDOM"> [TARGET USER]`  
    **Example**: `DELETE RANDOM`  
    *This action will remove either all items, the one in the player's main hand (or offhand, if not holding anything) or their whole inventory. The example action delete a random item from the inventory of every targetted player.*  

- Shuffling a player's inventory  
    **FORMAT**: `SHUFFLE <"ALL" / TARGET USER>`  
    **Example**: `SHUFFLE ALL`  
    *This action shuffles item positions inside a player's inventory. The example shuffles all targetted players inventories.*  

- Delay between actions  
    **Format**: `WAIT <SECONDS>`
    **Example**: `WAIT 2.5`
    *This action will make the plugin wait the specified amount of seconds before runnning the next action.*  

> [!TIP]
> Argument names surrounded by <> are required arguments.
> Arguments surrounded by [] are optional.

You should set up your events in your config file following this format:
```
TYPE_REWARDS:
    - EVENT:
        - Action 1
        - Action 2
        - ...
```
or
```
TYPE_REWARDS:
    - EVENT:
        - STREAMER:
            - Action 1
            - Action 2
            - ...

        - default:
            - Action
```
`TYPE_REWARDS` must be replaced with the appropiate config key that is already on the file and `EVENT` with the channel points reward name, subscription tier or minimal amount of bits/subs/viewers. In case of follow events, the `EVENT` line should be ommited (see placeholders in [twitch.yml](src/main/resources/twitch.yml)).  

For your actions, you can use the `{USER}`, `{TEXT}` and `{AMOUNT}` fields to use data from your events inside of your actions. `{USER}` will be replaced with the chatter's username. `{TEXT}` will be replaced with the text input of the Channel Points Reward. `{AMOUNT}` will be replaced with the bits/subs/raided viewers amount.  

<details>
<summary>Example Event</summary>

```yaml
CHANNEL_POINTS_REWARDS:
    My reward:
        your_favourite_streamer:
            - SPAWN CREEPER 3 Steve
        default:
            - GIVE DIAMOND 1
```
</details>

## Twitch Scopes
The latest version of the plugin needs the following scopes to function propertly:  
* `channel:read:redemptions`: Needed to read channel point redemptions.
* `channel:read:subscriptions`: Needed to listen for subscriptions and gifts.
* `moderator:read:followers`: Needed to be able to listen for follows.
* `bits:read`: Needed to listen for cheers.
* `chat:read` and `user:read:chat`: Needed to show your stream chat in-game and use EventSub API.
* `user:bot` and `channel:bot`: Needed to read stream chat messages

## Permissions
- TARGET  
    **ID**: `chatpointsttv.target`  
    **Behaviour**: Configured actions will trigger on all players with this permission.  

- BROADCAST  
    **ID**: `chatpointsttv.broadcast`  
    **Behaviour**: All players with this permission will get a banner message when an action is triggered.  

- MANAGE  
    **ID**: `chatpointsttv.manage`  
    **Behaviour**: Players with this permission will be allowed to run the `/twitch` command and link a Twitch account.  

## **Credits**
Thanks to [Twitch4J](https://twitch4j.github.io/) for the Java libraries used in this mod to communicate with the Twitch API.

Thanks to [AlessioDP](https://github.com/AlessioDP) for their work on their [fork of Libby](https://github.com/AlessioDP/libby)

Thanks to [urgrue](https://github.com/urgrue/Java-Twitch-Api-Wrapper) for the code for Twitch authentification.

Thanks to [Mystiflow](https://github.com/Mystiflow) for their [color convertion code](https://gist.github.com/Mystiflow/c42f45bac9916c84e381155f72a96d84).

