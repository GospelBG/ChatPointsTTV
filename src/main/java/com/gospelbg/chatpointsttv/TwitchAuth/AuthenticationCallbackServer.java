package com.gospelbg.chatpointsttv.TwitchAuth;

import java.io.IOException;
import java.net.*;

public class AuthenticationCallbackServer implements AuthenticationListener {

    /**
     * Default HTML page that twitch.tv will send the access_token to
     */
    public static final String DEFAULT_AUTH_PAGE = "/authorize.html";

    /**
     * Default HTML page that shows auth error's to user
     */
    public static final String DEFAULT_FAILURE_PAGE = "/authorize-failure.html";

    /**
     * Default HTML page that shows auth success to
     */
    public static final String DEFAULT_SUCCESS_PAGE = "/authorize-success.html";
    private final URL authPage;
    private final URL failurePage;
    private final URL successPage;
    private int port;
    private ServerSocket serverSocket;
    private String accessToken; // twitch.tv auth access token
    private Scopes[] accessScopes; // scopes retrieves for access token
    private AuthenticationError authenticationError;
    Thread thread;

    /**
     * Constructor that will use default HTML views for output.
     *
     * @param port Network port to receive requests on
     */
    public AuthenticationCallbackServer(int port) {
        this.port = port;
        // Load default pages
        authPage = getClass().getResource(DEFAULT_AUTH_PAGE);
        failurePage = getClass().getResource(DEFAULT_FAILURE_PAGE);
        successPage = getClass().getResource(DEFAULT_SUCCESS_PAGE);
    }

    /**
     * Constructor with user specified HTML views to output.
     *
     * @param port        Network port to receive requests on
     * @param authPage    HTML page that twitch.tv will send the access_token to
     * @param failurePage HTML page that shows auth error to user
     * @param successPage HTML page that shows auth success to user
     */
    public AuthenticationCallbackServer(int port, URL authPage, URL failurePage, URL successPage) {
        this.port = port;
        this.authPage = authPage == null ? getClass().getResource(DEFAULT_AUTH_PAGE) : authPage;
        this.failurePage = failurePage == null ? getClass().getResource(DEFAULT_FAILURE_PAGE) : failurePage;
        this.successPage = successPage == null ? getClass().getResource(DEFAULT_SUCCESS_PAGE) : successPage;
    }

    /**
     * Start the server and listen for auth callbacks from twitch.
     *
     * @throws IOException if an I/O error occurs while waiting for a connection.
     */
    public void start() throws IOException {
        // Establish the listen socket
        // For security reasons, the third parameter is set to not accept connections from outside the localhost
        serverSocket = new ServerSocket(port, 0, InetAddress.getByName("127.0.0.1"));
        run();
    }

    private void run() throws IOException {
        // Process HTTP service requests
        while (!serverSocket.isClosed()) {
            try {
                // Listen for a TCP connection request
                Socket connectionSocket = serverSocket.accept();
                // Handle request
                AuthenticationCallbackRequest request = new AuthenticationCallbackRequest(connectionSocket, authPage, failurePage, successPage);
                request.setAuthenticationListener(this);
                // Start thread
                thread = new Thread(request);
                thread.start();
            } catch (SocketException e) {
                // Socket was closed by another thread
                break;
            }
        }
    }

    /**
     * Stops the server.
     */
    public void stop() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                thread.interrupt();
            } catch (IOException ignored) {
            } finally {
                serverSocket = null;
            }
        }
    }

    /**
     * Check to see if server is running.
     *
     * @return <code>true</code> if server is running. <code>false</code> otherwise
     */
    public boolean isRunning() {
        return serverSocket != null;
    }

    @Override
    public void onAccessTokenReceived(String token, Scopes... scopes) {
        accessToken = token;
        accessScopes = scopes;
        stop(); // Stop the server, we no longer need to process requests
    }

    @Override
    public void onAuthenticationError(String error, String description) {
        authenticationError = new AuthenticationError(error, description);
        stop(); // Stop the server
    }

    /**
     * Get the access token if one exists.
     *
     * @return Access token if it exists, <code>null</code> otherwise
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Get the access scopes for the authenticated user.
     *
     * @return Array of {@link Scopes} that the authenticated user has
     */
    public Scopes[] getAccessScopes() {
        return accessScopes;
    }

    /**
     * Get the auth error if it failed.
     *
     * @return Error string if auth failed. <code>null</code> otherwise
     */
    public AuthenticationError getAuthenticationError() {
        return authenticationError;
    }

    /**
     * Check if there was an authentication error
     *
     * @return <code>true</code> if an error exists, <code>false</code> otherwise
     */
    public boolean hasAuthenticationError() {
        return authenticationError != null;
    }
}
