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

2. Open your channel's [Twitch Dashboard](https://dashboard.twitch.tv) and create your custom Channel Points Rewards. (optional)
> [!TIP]
>  If needed, you can copy-paste the names of the Channel Points Rewards into a text document for later use.
  
3. Set your [config.yml](core/src/main/resources/config.yml) up. Adjust the settings and setup events for rewards, donations... You will need to link a Twitch account in order to connect use the Twitch API (the linked account **needs to own/moderate all Twitch channels** set in config.yml) There are two ways to link your Twitch account to the plugin:  

    - **Using a key-based authentication** *(recommended)*:  
    You will need a Client ID and Access token. You can get one mannually or through a website as [Twitch Token Generator](https://twitchtokengenerator.com). Make sure to add all the [needed scopes](#twitch-scopes).  
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
* **Auto-Link**: If set to true, it will try to connect automatically to the Twitch API with the provided credentials in `CUSTOM_CLIENT_ID` and `CUSTOM_ACCESS_TOKEN`.  
* **Ignore Offline Streamers**: If set to true, the plugin will only listen to the people who is currently streaming. This also includes chat messages.  
* **Custom Client ID**: Client ID used for key-based authorization. Leave commented if it's not being used.  
* **Custom Access Token**: Access token used for key-based authorization. Leave commented if it's not being used.  
* **Ignore Offline Events**: If enabled, events from offline streamers will be ignored.  
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
* **Raid Rewards***
A list containing all **raid rewards** that you want an action set up. See [Reward Actions](#reward-actions) for more information.  
You need to follow this format: AMOUNT: {ACTION}, replacing AMOUNT with the minimal ammount of raid viewers that will be needed to trigger the event and {ACTION} with the desired action to run.  
* **Mob Glow**: Whether the spawned mobs should have a glowing effect (highlighted and visible through blocks).
* **Display Name on Mob**: Whether the spawned mobs should have the name of the user who triggered the action.
* **Log Events**: Determines whether all events will be logged. `true` means that all channel point rewards, cheers, subscriptions and gifts will be logged into the console. `false` means that they won't be logged.
* **In-game Alerts Mode***: How will the game notify of an event. Valid options: `chat`, `title`, `all`, `none`. Choosing `none`  will disable In-game alerts.
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

* `/twitch test <event type> <...>`  
    Fires a custom test event. Useful for testing your configuration.  
    In case of channel point rewards that contains whitespaces in their name, it can be enclosed between double quotes.  
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
    **Format**: `RUN <TARGET / CONSOLE> <COMMAND>`  
    **Example**: `RUN TARGET DAMAGE @S 2`  
    *This action will run a command as the console or the target player.  
    The example action will substract each player a heart from their health.*  

- Giving Items  
    **Format**: `GIVE <ITEM> [AMOUNT] [TARGET USER]`  
    **Example**: `GIVE DIAMOND 1 GospelBG`
    *This action will give the established amount of the set items to all players with the `chatpointsttv.target` permission. The example action will give all players (with the "target" permission) a diamond.*  

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
> **You should omit the `KEY` section to add follow rewards** and go straight to the actions. See placeholders on the default [config.yml](core/src/main/resources/config.yml#L41).

> [!TIP]
> You can now target multiple channels. If you do so, you can target some events to a specific channel following the second format. You can still follow the first example if you don't aim to target specific channels. 

You can also use the `{USER}` and `{TEXT}` fields to use data from your events inside of your actions. `{USER}` will be replaced with the chatter's username. `{TEXT}` will be replaced with the text input of the Channel Points Reward.


<details>
<summary>Example config.yml file</summary>

```yaml
# This is the configuration file for ChatPointsTTV. For a fresh copy of this file, go to https://github.com/GospelBG/ChatPointsTTV/blob/master/src/main/resources/config.yml
# For more information about how to use this file, refer to the config.yml instructions in the README file at https://github.com/GospelBG/ChatPointsTTV/blob/master/README.md

# Username(s) of the streamer's channels. In case of multiple streamers, must be set as a list: ["channel_1", "channel_2", "..."]
CHANNEL_USERNAME: "gospelbg"

# Uncomment the following lines (remove the # symbol at the start of the line) to use a key-based authentication instead of the browser login.
# It should be used in remote environments which do not have a web browser (i.e. a standalone hosted server)
# CUSTOM_CLIENT_ID: your_client_id_here
# CUSTOM_ACCESS_TOKEN: your_access_token_here

# Whether the plugin shoud automatically connect to Twitch when a key-based authentication is used.
AUTO_LINK_CUSTOM: true

# Whether events should be ignored if the streamer is offline.
IGNORE_OFFLINE_STREAMERS: false

# Whether your stream chat should show up in the in-game chat
SHOW_CHAT: true

# Uncomment the following line and add chat bot usernames (in lowercase) to this list to prevent their messages to display on the in-game chat.
# CHAT_BLACKLIST:

# Set up here your channel point rewards.
# Add as many lines as needed. Replace [REWARD NAME] with your reward's name, EXACTLY as it is on Twitch.

# REWARD_NAME:
#    - ACTION

# For the action, follow this format:
#     SPAWN {ENTITY_NAME} [AMOUNT] [TARGET USER]
#     RUN {TARGET / CONSOLE} {COMMAND}
#     GIVE {ITEM} [AMOUNT] [TARGET USER]
#     TNT {AMOUNT} [EXPLOSION TIME IN TICKS]

# Extra fields:
#     {TEXT} Channel Points User Input
#     {USER} Viewer's username
#     {AMOUNT} Bits/Gifted Subs/Viewers amount.

# To add a streamer-specific reward add the actions inside a list named as the desired channel.
# To target all channels use "default" as the list key.

# Leave any reward class empty to disable it.
CHANNEL_POINTS_REWARDS:
  Give me my favourite item:
    gospelbg:
        - GIVE diamond 1 GospelBG

    my_best_friend:
        - GIVE stick 1 MyBestFriend

  Kill a random player:
    - RUN CONSOLE /kill @r

# Put the desired action after the colon as a list. You musn't add a "Reward Name".
FOLLOW_REWARDS:
  - SPAWN zombie

# Follow the same format as Channel Point Rewards, replacing the name of the reward for the minimum ammount of bits that needs to be cheered.
CHEER_REWARDS:
  10:
    - GIVE iron_ingot 10

  100:
    - GIVE Emerald 1

# Follow the same format as Channel Point Rewards, replacing the name of the reward for the desired sub tier: TWITCH_PRIME/TIER1/TIER2/TIER3.
# Also you can enter an AMOUNT on GIFT_REWARDS to toggle actions with sub gifting
SUB_REWARDS:
  TWITCH_PRIME:
    - SPAWN Creeper 3
  TIER1:
    - TNT 1 10

GIFT_REWARDS:
  1:
    - TNT 1 1

RAID_REWARDS:
  100:
    - TNT 100 1

# Add glow effect to spawned mobs
MOB_GLOW: true
# Set chatter's name to spawned mobs
DISPLAY_NAME_ON_MOB: true

# Whether the plugin should log to the console when a user (GospelBG cheered 300 bits!; GospelBG has subscribed with a Tier 1 sub!; ...)
LOG_EVENTS: true

# In-game event alerts mode.
#     none: Disables in-game alerts
#     chat: Displays a message in the chat
#     title: Shows a title splash message
#     all: Uses both "chat" and "title" alerts
INGAME_ALERTS: all

# You can use the following options:

#  Colors:
#   AQUA, BLACK, BLUE, DARK_AQUA, DARK_BLUE, DARK_GRAY, DARK_GREEN, DARK_PURPLE,
#   DARK_RED, GOLD, GRAY, GREEN, ITALIC, LIGHT_PURPLE, RED, WHITE, YELLOW

#  Formatting:
#   BOLD
#   STRIKETHROUGH
#   UNDERLINE

# Misc:
#   MAGIC

COLORS:
  USER_COLOR: GOLD
  EVENT_COLOR: YELLOW

# Customize the on-screen text that is broadcasted to players.
# Viewer's username and their reward will be autommatically added to the message.

STRINGS:
  REDEEMED_STRING: "has redeemed" # Whitespaces on the start and end of the string are added automatically.
  FOLLOWED_STRING: "has started following"
  CHEERED_STRING: "cheered" #..."100 bits"
  SUB_STRING: "has subscribed with a" #..."Tier 1/2/3/Prime"
  GIFT_STRING: "gifted" #..."5 subs"
  RAIDED_STRING: "has raided {CHANNEL} with a viewer count of" # {CHANNEL} will be replaced with the raided channel name.
```
</details>

## Twitch Scopes
The latest version of the plugin needs the following scopes to function propertly:  
* `channel:read:redemptions`: Needed to read channel point redemptions.
* `channel:read:subscriptions`: Needed to listen for subscriptions and gifts.
* `user:read:moderated_channels`: Needed to check if user has permission to listen for follow events. (API only allows to listen to own/moderated channels)
* `moderator:read:followers`: Needed to be able to listen for follows.
* `bits:read`: Needed to listen for cheers.
* `chat:read` and `user:read:chat`: Needed to show your stream chat in-game and use EventSub API.
* `user:bot` and `channel:bot`: Needed to read stream chat messages

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

Thanks to [AlessioDP](https://github.com/AlessioDP) for their work on their [fork of Libby](https://github.com/AlessioDP/libby)

Thanks to [urgrue](https://github.com/urgrue/Java-Twitch-Api-Wrapper) for the code for Twitch authentification.

Thanks to [Mystiflow](https://github.com/Mystiflow) for their [color convertion code](https://gist.github.com/Mystiflow/c42f45bac9916c84e381155f72a96d84).

