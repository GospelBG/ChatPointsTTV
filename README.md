# ChatPointsTTV

ChatPointsTTV is an Spigot plugin that helps you to create interactions between your Twitch chat and your Minecraft world. You can setup custom channel points rewards on your channel that can spawn mobs and execute commands.

You will need to setup a Twitch Application in the Dev Console.

### **Setup**:
1. Install the plugin into your Minecraft server.
2. Create an app using [Twitch's Developer Console](https://dev.twitch.tv/console). (Refer to ["How to set up my Twitch app"](#how-to-set-up-my-twitch-app)).
3. Copy your Client ID and your secret token to your [config.yml](/src/main/resources/config.yml).*
4. Open your [Twitch Dashboard](https://dashboard.twitch.tv) and create your custom Channel Points Rewards. (Keep in mind the names of the rewards).
5. Add to your [config.yml](/src/main/resources/config.yml) your newly created Twitch rewards, and setup the actions. Note: copy the name exactly as it is on Twitch.*
6. Customize the text and it's formatting (optional).

*: The config file for the plugin should be in 'plugins/ChatPointsTTV/config.yml'.

### **How to set up my Twitch app:**
1. Go to [Twitch's Developer Console](https://dev.twitch.tv/console) and login.
2. Click "Register your application".
3. Fill in a name, and for the OAuth redirect, type 'https://localhost'. Choose a category and generate a secret. (copy your app ID and your secret).

### **CREDITS:**
Thanks to [Twitch4J](https://twitch4j.github.io/) for the Java libraries used in this mod to communicate with the Twitch API.