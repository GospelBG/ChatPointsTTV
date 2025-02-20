package me.gosdev.chatpointsttv.Twitch.Auth;

/**
 * The <code>AuthenticationError</code> class represents an error during
 * authentication with <a href="http://twitch.tv">http://www.twitch.tv</a>.
 */
public class AuthenticationError {
    private final String name; // the name of the error
    private final String description; // the description of the error

    public AuthenticationError(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "AuthenticationError{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthenticationError that = (AuthenticationError) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(description != null ? !description.equals(that.description) : that.description != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    /**
     * Get the name of the error.
     *
     * @return the name of the error
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of the error.
     *
     * @return the description of the error.
     */
    public String getDescription() {
        return description;
    }
}
