package ma.project.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientModel {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dout;
    private MessageListener listener;
    private volatile boolean connected = false;

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public boolean connect(String host, int port, String username) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            dis = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            dout.writeUTF(username);
            dout.flush();

            connected = true;
            startReceiving();
            return true;
        } catch (IOException e) {
            if (listener != null) listener.onError(e.getMessage());
            return false;
        }
    }

    private void startReceiving() {
        Thread t = new Thread(() -> {
            try {
                while (connected) {
                    String msg = dis.readUTF();
                    if (listener != null) listener.onMessageReceived(msg);
                }
            } catch (IOException e) {
                if (connected) {
                    connected = false;
                    if (listener != null) listener.onDisconnected();
                }
            }
        }, "receive-thread");
        t.setDaemon(true);
        t.start();
    }

    public void sendMessage(String message) {
        if (!connected) return;
        try {
            dout.writeUTF(message);
            dout.flush();
        } catch (IOException e) {
            connected = false;
            if (listener != null) listener.onError(e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (dout != null) {
                dout.writeUTF("bye");
                dout.flush();
            }
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return connected;
    }
}
