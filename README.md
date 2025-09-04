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

Further documentation can be found on the [documentation site](https://gosdev.me/chatpointsttv/install)

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

