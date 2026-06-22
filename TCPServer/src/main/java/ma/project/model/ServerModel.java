package ma.project.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerModel {

    private volatile ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private ServerListener listener;
    private final int port;
    private volatile boolean running = false;

    public ServerModel(int port) {
        this.port = port;
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    public void start() {
        Thread t = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                if (listener != null) listener.onServerStarted(port);
                acceptLoop();
            } catch (IOException e) {
                if (listener != null) listener.onLog("ERROR starting server: " + e.getMessage());
            }
        }, "server-accept-thread");
        t.setDaemon(true);
        t.start();
    }

    private void acceptLoop() {
        while (running) {
            if (listener != null) listener.onLog("Waiting for client...");
            try {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                Thread ct = new Thread(handler, "client-handler-" + socket.getRemoteSocketAddress());
                ct.setDaemon(true);
                ct.start();
            } catch (IOException e) {
                if (running && listener != null) listener.onLog("Accept error: " + e.getMessage());
            }
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
        if (listener != null) listener.onMessageBroadcast(message);
    }

    public void notifyClientConnected(ClientHandler handler) {
        if (listener != null) listener.onClientConnected(handler.getUsername());
    }

    public void removeClient(ClientHandler handler) {
        if (clients.remove(handler)) {
            if (listener != null) listener.onClientDisconnected(handler.getUsername());
        }
    }

    public List<String> getActiveUsernames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler ch : clients) {
            String name = ch.getUsername();
            if (name != null) names.add(name);
        }
        return names;
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }
}
