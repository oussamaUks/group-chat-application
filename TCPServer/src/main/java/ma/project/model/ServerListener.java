package ma.project.model;

public interface ServerListener {
    void onServerStarted(int port);
    void onClientConnected(String username);
    void onClientDisconnected(String username);
    void onMessageBroadcast(String formattedMessage);
    void onLog(String message);
}
