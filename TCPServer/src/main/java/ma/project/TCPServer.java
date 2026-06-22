package ma.project;

import javafx.application.Application;
import javafx.stage.Stage;
import ma.project.view.ServerView;

public class TCPServer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        new ServerView().init(stage);
    }
}
