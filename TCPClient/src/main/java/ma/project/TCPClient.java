package ma.project;

import javafx.application.Application;
import javafx.stage.Stage;
import ma.project.view.ClientView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class TCPClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        List<String> params = getParameters().getRaw();

        String host;
        int port;

        if (params.size() >= 2) {
            host = params.get(0);
            port = Integer.parseInt(params.get(1));
        } else {
            Properties props = loadConfig();
            host = props.getProperty("server.host", "localhost");
            port = Integer.parseInt(props.getProperty("server.port", "3000"));
        }

        new ClientView(stage, host, port).showLoginScene();
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/client.properties")) {
            if (is != null) props.load(is);
        } catch (IOException ignored) {}
        return props;
    }
}
