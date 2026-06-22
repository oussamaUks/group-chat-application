package ma.project.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import ma.project.model.ServerListener;
import ma.project.model.ServerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class ServerView implements ServerListener {

    // ── Palette ──────────────────────────────────────────────
    private static final String BG        = "#0F1117";
    private static final String SURFACE   = "#161B27";
    private static final String CARD      = "#1C2333";
    private static final String BORDER    = "#21262D";
    private static final String ACCENT    = "#58A6FF";
    private static final String GREEN     = "#3FB950";
    private static final String ORANGE    = "#F0883E";
    private static final String PURPLE    = "#BC8CFF";
    private static final String RED       = "#F85149";
    private static final String TEXT      = "#E6EDF3";
    private static final String MUTED     = "#8B949E";

    private static final String[] AVATAR_COLORS = {
        "#6366F1","#EC4899","#14B8A6","#F59E0B",
        "#EF4444","#8B5CF6","#06B6D4","#10B981","#F97316","#3B82F6"
    };

    // ── State ────────────────────────────────────────────────
    private final ObservableList<String> userList = FXCollections.observableArrayList();
    private final Map<String, String>    userColors = new HashMap<>();
    private final Random rng = new Random();

    private VBox   logContainer;
    private ScrollPane logScroll;
    private Label  countLabel;
    private ServerModel model;

    // ── Entry point ──────────────────────────────────────────
    public void init(Stage stage) {
        Properties props = loadConfig();
        int port = Integer.parseInt(props.getProperty("server.port", "3000"));

        model = new ServerModel(port);
        model.setListener(this);

        stage.setTitle("Group Chat — Server");
        stage.setScene(buildScene(port));
        stage.setMinWidth(900);
        stage.setMinHeight(580);
        stage.setOnCloseRequest(e -> { model.stop(); Platform.exit(); });
        stage.show();

        model.start();
    }

    // ── Scene ────────────────────────────────────────────────
    private Scene buildScene(int port) {
        GridPane root = new GridPane();
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setPadding(new Insets(20));
        root.setHgap(16);
        root.setVgap(14);

        ColumnConstraints lc = new ColumnConstraints();
        lc.setPercentWidth(30);
        lc.setHgrow(Priority.ALWAYS);
        ColumnConstraints rc = new ColumnConstraints();
        rc.setPercentWidth(70);
        rc.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(lc, rc);

        RowConstraints hr = new RowConstraints();
        hr.setPrefHeight(68);
        RowConstraints cr = new RowConstraints();
        cr.setVgrow(Priority.ALWAYS);
        root.getRowConstraints().addAll(hr, cr);

        HBox header = buildHeader(port);
        GridPane.setColumnSpan(header, 2);
        root.add(header, 0, 0);
        root.add(buildUsersPanel(), 0, 1);
        root.add(buildLogPanel(), 1, 1);

        return new Scene(root, 1000, 680);
    }

    // ── Header bar ───────────────────────────────────────────
    private HBox buildHeader(int port) {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 22, 14, 22));
        bar.setStyle(
            "-fx-background-color: " + SURFACE + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        DropShadow ds = new DropShadow(12, Color.web("#000000", 0.5));
        bar.setEffect(ds);

        // Logo icon
        StackPane logo = new StackPane();
        Rectangle rect = new Rectangle(40, 40);
        rect.setArcWidth(12);
        rect.setArcHeight(12);
        rect.setFill(Color.web(ACCENT));
        Label logoTxt = new Label("#");
        logoTxt.setFont(Font.font("System", FontWeight.BOLD, 20));
        logoTxt.setStyle("-fx-text-fill: white;");
        logo.getChildren().addAll(rect, logoTxt);

        // Title
        VBox titles = new VBox(2);
        Label title = new Label("Group Chat Server");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: " + TEXT + ";");
        countLabel = new Label("No clients connected");
        countLabel.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 12;");
        titles.getChildren().addAll(title, countLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Port badge
        HBox portBadge = new HBox(6);
        portBadge.setAlignment(Pos.CENTER);
        portBadge.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-padding: 6 14;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;"
        );
        Circle pulseDot = new Circle(5, Color.web(GREEN));
        pulseDot.setEffect(new Glow(0.9));
        Label portLbl = new Label("PORT " + port);
        portLbl.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-weight: bold; -fx-font-size: 12;");
        portBadge.getChildren().addAll(pulseDot, portLbl);

        bar.getChildren().addAll(logo, titles, spacer, portBadge);
        return bar;
    }

    // ── Users panel ──────────────────────────────────────────
    private VBox buildUsersPanel() {
        VBox panel = new VBox(10);
        panel.setStyle(
            "-fx-background-color: " + SURFACE + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 16;"
        );
        VBox.setVgrow(panel, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label("ONLINE USERS");
        titleLbl.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-weight: bold; -fx-font-size: 11;");
        titleRow.getChildren().add(titleLbl);

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox listBox = new VBox(6);
        listBox.setStyle("-fx-background-color: " + SURFACE + ";");
        scroll.setContent(listBox);

        userList.addListener((javafx.collections.ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (String u : change.getAddedSubList()) {
                        String color = userColors.getOrDefault(u, AVATAR_COLORS[0]);
                        listBox.getChildren().add(buildUserCard(u, color));
                    }
                }
                if (change.wasRemoved()) {
                    for (String u : change.getRemoved()) {
                        listBox.getChildren().removeIf(node -> {
                            Object tag = node.getUserData();
                            return tag != null && tag.equals(u);
                        });
                    }
                }
            }
        });

        panel.getChildren().addAll(titleRow, buildDivider(), scroll);
        return panel;
    }

    private HBox buildUserCard(String username, String color) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        card.setUserData(username);

        // Avatar
        StackPane avatar = buildAvatar(username, color, 36);

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(username);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        nameLbl.setStyle("-fx-text-fill: " + TEXT + ";");

        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web(GREEN));
        dot.setEffect(new Glow(0.6));
        Label statusLbl = new Label("Online");
        statusLbl.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 11;");
        statusRow.getChildren().addAll(dot, statusLbl);

        info.getChildren().addAll(nameLbl, statusRow);
        card.getChildren().addAll(avatar, info);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #21262D; -fx-background-radius: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: " + CARD + "; -fx-background-radius: 10; -fx-cursor: hand;"));

        return card;
    }

    // ── Log panel ────────────────────────────────────────────
    private VBox buildLogPanel() {
        VBox panel = new VBox(10);
        panel.setStyle(
            "-fx-background-color: " + SURFACE + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 16;"
        );
        VBox.setVgrow(panel, Priority.ALWAYS);

        Label titleLbl = new Label("ACTIVITY LOG");
        titleLbl.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-weight: bold; -fx-font-size: 11;");

        logContainer = new VBox(4);
        logContainer.setStyle("-fx-background-color: " + CARD + ";");
        logContainer.setPadding(new Insets(8));

        logScroll = new ScrollPane(logContainer);
        logScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-background-radius: 8;");
        logScroll.setFitToWidth(true);
        logScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(logScroll, Priority.ALWAYS);

        logContainer.heightProperty().addListener((obs, o, n) -> logScroll.setVvalue(1.0));

        panel.getChildren().addAll(titleLbl, buildDivider(), logScroll);
        return panel;
    }

    // ── Helpers ──────────────────────────────────────────────
    private StackPane buildAvatar(String username, String color, double size) {
        Circle bg = new Circle(size / 2.0, Color.web(color));
        String letter = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
        Label lbl = new Label(letter);
        lbl.setFont(Font.font("System", FontWeight.BOLD, size * 0.4));
        lbl.setStyle("-fx-text-fill: white;");
        return new StackPane(bg, lbl);
    }

    private Region buildDivider() {
        Region d = new Region();
        d.setPrefHeight(1);
        d.setStyle("-fx-background-color: " + BORDER + ";");
        return d;
    }

    private void addLogEntry(String icon, String text, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setStyle("-fx-background-color: transparent;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13;");
        iconLbl.setMinWidth(20);

        Label textLbl = new Label(text);
        textLbl.setStyle(
            "-fx-text-fill: " + TEXT + "; -fx-font-size: 13;" +
            "-fx-font-family: 'Consolas', monospace;"
        );
        textLbl.setWrapText(true);
        HBox.setHgrow(textLbl, Priority.ALWAYS);

        row.getChildren().addAll(iconLbl, textLbl);
        logContainer.getChildren().add(row);
    }

    private Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/server.properties")) {
            if (is != null) p.load(is);
        } catch (IOException ignored) {}
        return p;
    }

    // ── ServerListener callbacks ─────────────────────────────
    @Override
    public void onServerStarted(int port) {
        Platform.runLater(() -> addLogEntry("✦", "Server started on port " + port, ACCENT));
    }

    @Override
    public void onClientConnected(String username) {
        Platform.runLater(() -> {
            String color = AVATAR_COLORS[rng.nextInt(AVATAR_COLORS.length)];
            userColors.put(username, color);
            if (!userList.contains(username)) userList.add(username);
            countLabel.setText(userList.size() + " client(s) connected");
            addLogEntry("→", "Welcome " + username + "!", GREEN);
        });
    }

    @Override
    public void onClientDisconnected(String username) {
        Platform.runLater(() -> {
            userList.remove(username);
            userColors.remove(username);
            countLabel.setText(userList.size() + " client(s) connected");
            addLogEntry("←", username + " disconnected.", RED);
        });
    }

    @Override
    public void onMessageBroadcast(String msg) {
        Platform.runLater(() -> addLogEntry("◈", msg, MUTED));
    }

    @Override
    public void onLog(String message) {
        Platform.runLater(() -> addLogEntry("·", message, MUTED));
    }
}
