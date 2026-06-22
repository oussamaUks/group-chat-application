package ma.project.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final AtomicInteger GUEST_COUNTER = new AtomicInteger(0);

    private final Socket socket;
    private final ServerModel server;
    private DataInputStream dis;
    private DataOutputStream dout;
    private String username;

    public ClientHandler(Socket socket, ServerModel server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            String rawName = dis.readUTF().trim();
            username = rawName.isEmpty() ? "Guest-" + GUEST_COUNTER.incrementAndGet() : rawName;

            server.notifyClientConnected(this);
            server.broadcast("[" + now() + "] *** " + username + " joined the chat ***");

            while (true) {
                String msg = dis.readUTF();

                if ("end".equalsIgnoreCase(msg) || "bye".equalsIgnoreCase(msg)) {
                    server.removeClient(this);
                    server.broadcast("[" + now() + "] *** " + username + " left the chat ***");
                    break;
                }

                if ("allUsers".equalsIgnoreCase(msg)) {
                    List<String> users = server.getActiveUsernames();
                    sendMessage("[Server] Active users (" + users.size() + "): " + String.join(", ", users));
                    continue;
                }

                server.broadcast("[" + now() + "] " + username + ": " + msg);
            }
        } catch (IOException e) {
            server.removeClient(this);
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public void sendMessage(String message) {
        try {
            dout.writeUTF(message);
            dout.flush();
        } catch (IOException e) {
            server.removeClient(this);
        }
    }

    public String getUsername() {
        return username;
    }

    private String now() {
        return LocalTime.now().format(TIME_FMT);
    }
}
