package me.gosdev.chatpointsttv.Utils;

/**
 * When requesting authorization from users, the scope parameter allows you to specify
 * which permissions your app requires. These scopes are ties to the access token you
 * receive upon a successful authorization. Without specifying scopes, your app only has
 * access to basic information about the authenticated user. You may specify any or all
 * of the following scopes.
 */
public enum Scopes {

    /**
    * View analytics data for the Twitch Extensions owned by the authenticated account.

    */
    ANALYTICS_READ_EXTENSIONS("analytics:read:extensions"),

    /**
    * View analytics data for the games owned by the authenticated account.

    */
    ANALYTICS_READ_GAMES("analytics:read:games"),

    /**
    * View Bits information for a channel.

    */
    BITS_READ("bits:read"),

    /**
    * Manage ads schedule on a channel.

    */
    CHANNEL_MANAGE_ADS("channel:manage:ads"),

    /**
    * Read the ads schedule and details on your channel.

    */
    CHANNEL_READ_ADS("channel:read:ads"),

    /**
    * Manage a channel’s broadcast configuration, including updating channel configuration and managing stream markers and stream tags.

    */
    CHANNEL_MANAGE_BROADCAST("channel:manage:broadcast"),

    /**
    * Read charity campaign details and user donations on your channel.

    */
    CHANNEL_READ_CHARITY("channel:read:charity"),

    /**
    * Run commercials on a channel.

    */
    CHANNEL_EDIT_COMMERCIAL("channel:edit:commercial"),

    /**
    * View a list of users with the editor role for a channel.

    */
    CHANNEL_READ_EDITORS("channel:read:editors"),

    /**
    * Manage a channel’s Extension configuration, including activating Extensions.Update User Extensions

    */
    CHANNEL_MANAGE_EXTENSIONS("channel:manage:extensions"),

    /**
    * View Creator Goals for a channel.

    */
    CHANNEL_READ_GOALS("channel:read:goals"),

    /**
    * Read Guest Star details for your channel.

    */
    CHANNEL_READ_GUEST_STAR("channel:read:guest_star"),

    /**
    * Manage Guest Star for your channel.

    */
    CHANNEL_MANAGE_GUEST_STAR("channel:manage:guest_star"),

    /**
    * View Hype Train information for a channel.

    */
    CHANNEL_READ_HYPE_TRAIN("channel:read:hype_train"),

    /**
    * Add or remove the moderator role from users in your channel.

    */
    CHANNEL_MANAGE_MODERATORS("channel:manage:moderators"),

    /**
    * View a channel’s polls.

    */
    CHANNEL_READ_POLLS("channel:read:polls"),

    /**
    * Manage a channel’s polls.

    */
    CHANNEL_MANAGE_POLLS("channel:manage:polls"),

    /**
    * View a channel’s Channel Points Predictions.

    */
    CHANNEL_READ_PREDICTIONS("channel:read:predictions"),

    /**
    * Manage of channel’s Channel Points Predictions

    */
    CHANNEL_MANAGE_PREDICTIONS("channel:manage:predictions"),

    /**
    * Manage a channel raiding another channel.

    */
    CHANNEL_MANAGE_RAIDS("channel:manage:raids"),

    /**
    * View Channel Points custom rewards and their redemptions on a channel.

    */
    CHANNEL_READ_REDEMPTIONS("channel:read:redemptions"),

    /**
    * Manage Channel Points custom rewards and their redemptions on a channel.

    */
    CHANNEL_MANAGE_REDEMPTIONS("channel:manage:redemptions"),

    /**
    * Manage a channel’s stream schedule.

    */
    CHANNEL_MANAGE_SCHEDULE("channel:manage:schedule"),

    /**
    * View an authorized user’s stream key.

    */
    CHANNEL_READ_STREAM_KEY("channel:read:stream_key"),

    /**
    * View a list of all subscribers to a channel and check if a user is subscribed to a channel.

    */
    CHANNEL_READ_SUBSCRIPTIONS("channel:read:subscriptions"),

    /**
    * Manage a channel’s videos, including deleting videos.

    */
    CHANNEL_MANAGE_VIDEOS("channel:manage:videos"),

    /**
    * Read the list of VIPs in your channel.

    */
    CHANNEL_READ_VIPS("channel:read:vips"),

    /**
    * Add or remove the VIP role from users in your channel.

    */
    CHANNEL_MANAGE_VIPS("channel:manage:vips"),

    /**
    * Manage Clips for a channel.

    */
    CLIPS_EDIT("clips:edit"),

    /**
    * View a channel’s moderation data including Moderators, Bans, Timeouts, and Automod settings.

    */
    MODERATION_READ("moderation:read"),

    /**
    * Send announcements in channels where you have the moderator role.

    */
    MODERATOR_MANAGE_ANNOUNCEMENTS("moderator:manage:announcements"),

    /**
    * Manage messages held for review by AutoMod in channels where you are a moderator.

    */
    MODERATOR_MANAGE_AUTOMOD("moderator:manage:automod"),

    /**
    * View a broadcaster’s AutoMod settings.

    */
    MODERATOR_READ_AUTOMOD_SETTINGS("moderator:read:automod_settings"),

    /**
    * Manage a broadcaster’s AutoMod settings.

    */
    MODERATOR_MANAGE_AUTOMOD_SETTINGS("moderator:manage:automod_settings"),

    /**
    * Ban and unban users.

    */
    MODERATOR_MANAGE_BANNED_USERS("moderator:manage:banned_users"),

    /**
    * View a broadcaster’s list of blocked terms.

    */
    MODERATOR_READ_BLOCKED_TERMS("moderator:read:blocked_terms"),

    /**
    * Manage a broadcaster’s list of blocked terms.

    */
    MODERATOR_MANAGE_BLOCKED_TERMS("moderator:manage:blocked_terms"),

    /**
    * Delete chat messages in channels where you have the moderator role

    */
    MODERATOR_MANAGE_CHAT_MESSAGES("moderator:manage:chat_messages"),

    /**
    * View a broadcaster’s chat room settings.

    */
    MODERATOR_READ_CHAT_SETTINGS("moderator:read:chat_settings"),

    /**
    * Manage a broadcaster’s chat room settings.

    */
    MODERATOR_MANAGE_CHAT_SETTINGS("moderator:manage:chat_settings"),

    /**
    * View the chatters in a broadcaster’s chat room.

    */
    MODERATOR_READ_CHATTERS("moderator:read:chatters"),

    /**
    * Read the followers of a broadcaster.

    */
    MODERATOR_READ_FOLLOWERS("moderator:read:followers"),

    /**
    * Read Guest Star details for channels where you are a Guest Star moderator.

    */
    MODERATOR_READ_GUEST_STAR("moderator:read:guest_star"),

    /**
    * Manage Guest Star for channels where you are a Guest Star moderator.

    */
    MODERATOR_MANAGE_GUEST_STAR("moderator:manage:guest_star"),

    /**
    * View a broadcaster’s Shield Mode status.

    */
    MODERATOR_READ_SHIELD_MODE("moderator:read:shield_mode"),

    /**
    * Manage a broadcaster’s Shield Mode status.

    */
    MODERATOR_MANAGE_SHIELD_MODE("moderator:manage:shield_mode"),

    /**
    * View a broadcaster’s shoutouts.

    */
    MODERATOR_READ_SHOUTOUTS("moderator:read:shoutouts"),

    /**
    * Manage a broadcaster’s shoutouts.

    */
    MODERATOR_MANAGE_SHOUTOUTS("moderator:manage:shoutouts"),

    /**
    * View a broadcaster’s unban requests.

    */
    MODERATOR_READ_UNBAN_REQUESTS("moderator:read:unban_requests"),

    /**
    * Manage a broadcaster’s unban requests.

    */
    MODERATOR_MANAGE_UNBAN_REQUESTS("moderator:manage:unban_requests"),

    /**
    * Manage a user object.

    */
    USER_EDIT("user:edit"),

    /**
    * View the block list of a user.

    */
    USER_READ_BLOCKED_USERS("user:read:blocked_users"),

    /**
    * Manage the block list of a user.

    */
    USER_MANAGE_BLOCKED_USERS("user:manage:blocked_users"),

    /**
    * View a user’s broadcasting configuration, including Extension configurations.

    */
    USER_READ_BROADCAST("user:read:broadcast"),

    /**
    * Update the color used for the user’s name in chat.Update User Chat Color

    */
    USER_MANAGE_CHAT_COLOR("user:manage:chat_color"),

    /**
    * View a user’s email address.

    */
    USER_READ_EMAIL("user:read:email"),

    /**
    * View emotes available to a user

    */
    USER_READ_EMOTES("user:read:emotes"),

    /**
    * View the list of channels a user follows.

    */
    USER_READ_FOLLOWS("user:read:follows"),

    /**
    * Read the list of channels you have moderator privileges in.

    */
    USER_READ_MODERATED_CHANNELS("user:read:moderated_channels"),

    /**
    * View if an authorized user is subscribed to specific channels.

    */
    USER_READ_SUBSCRIPTIONS("user:read:subscriptions"),

    /**
    * Read whispers that you send and receive, and send whispers on your behalf.

    */
    USER_MANAGE_WHISPERS("user:manage:whispers"),

    /**
    * Allows the client’s bot users access to a channel.

    */
    CHANNEL_BOT("channel:bot"),

    /**
    * Perform moderation actions in a channel. The user requesting the scope must be a moderator in the channel.

    */
    CHANNEL_MODERATE("channel:moderate"),

    /**
    * Send live stream chat messages using an IRC connection.

    */
    CHAT_EDIT("chat:edit"),

    /**
    * View live stream chat messages using an IRC connection.

    */
    CHAT_READ("chat:read"),

    /**
    * Allows client’s bot to act as this user.

    */
    USER_BOT("user:bot"),

    /**
    * View live stream chat and room messages using EventSub.

    */
    USER_READ_CHAT("user:read:chat"),

    /**
    * Send live stream chat messages using Send Chat Message API.

    */
    USER_WRITE_CHAT("user:write:chat"),

    /**
    * View your whisper messages.

    */
    WHISPERS_READ("whispers:read"),

    /**
    * Send your whisper messages.
    */
    WHISPERS_EDIT("whispers:edit");



    private final String key;

    Scopes(String key) {
        this.key = key;
    }

    /**
     * Combine <code>Scopes</code> into a '+' separated <code>String</code>.
     * This is the required input format for twitch.tv
     *
     * @param scopes <code>Scopes</code> to combine.
     * @return <code>String</code> representing '+' separated list of <code>Scopes</code>
     */
    public static String join(Scopes... scopes) {
        if (scopes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Scopes scope : scopes) {
            sb.append(scope.getKey()).append("+");
        }
        sb.deleteCharAt(sb.length()-1); // Remove last +
        return sb.toString();
    }

    /**
     * Convert the string representation of the Scope to the Enum.
     *
     * @param text Text representation of Enum value
     * @return Enum value that the text represents
     */
    public static Scopes fromString(String text) {
        if (text == null) return null;
        for (Scopes b : Scopes.values()) {
            if (text.equalsIgnoreCase(b.key)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Get the identifier that twitch will recognize.
     *
     * @return A <code>String</code> identifier
     */
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}