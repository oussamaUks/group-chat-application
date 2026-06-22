package ma.project.model;

public interface MessageListener {
    void onMessageReceived(String message);
    void onDisconnected();
    void onError(String message);
}
