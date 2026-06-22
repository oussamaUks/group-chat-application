package ma.project.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import ma.project.model.ClientModel;
import ma.project.model.MessageListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ClientView implements MessageListener {

    // ── Palette ──────────────────────────────────────────────
    private static final String BG      = "#0F1117";
    private static final String SURFACE = "#161B27";
    private static final String CARD    = "#1C2333";
    private static final String BORDER  = "#21262D";
    private static final String ACCENT  = "#58A6FF";
    private static final String GREEN   = "#3FB950";
    private static final String ORANGE  = "#F0883E";
    private static final String RED     = "#F85149";
    private static final String TEXT    = "#E6EDF3";
    private static final String MUTED   = "#8B949E";

    private static final String[] AVATAR_COLORS = {
        "#6366F1","#EC4899","#14B8A6","#F59E0B",
        "#EF4444","#8B5CF6","#06B6D4","#10B981","#F97316","#3B82F6"
    };

    // ── State ────────────────────────────────────────────────
    private final Stage  stage;
    private final String host;
    private final int    port;
    private final Map<String, String> userColorMap = new HashMap<>();
    private final Random rng = new Random();

    private ClientModel model;
    private String  username;
    private boolean readOnly;

    private VBox      msgContainer;
    private ScrollPane msgScroll;
    private TextField  msgField;
    private Button     sendBtn;
    private Circle     statusDot;
    private Label      statusLbl;

    public ClientView(Stage stage, String host, int port) {
        this.stage = stage;
        this.host  = host;
        this.port  = port;
    }

    // ── Login scene ──────────────────────────────────────────
    public void showLoginScene() {
        // Full-screen background
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        // Decorative gradient circle behind card
        Circle glow = new Circle(200, Color.web(ACCENT, 0.07));
        glow.setEffect(new Glow(0.3));
        StackPane.setAlignment(glow, Pos.CENTER);

        // Card
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);
        card.setPadding(new Insets(44, 48, 44, 48));
        card.setStyle(
            "-fx-background-color: " + SURFACE + ";" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;"
        );
        DropShadow shadow = new DropShadow(40, Color.web("#000000", 0.6));
        card.setEffect(shadow);

        // Logo
        StackPane logoBox = new StackPane();
        Rectangle logoRect = new Rectangle(56, 56);
        logoRect.setArcWidth(16);
        logoRect.setArcHeight(16);
        logoRect.setFill(Color.web(ACCENT));
        Label logoLbl = new Label("#");
        logoLbl.setFont(Font.font("System", FontWeight.BOLD, 28));
        logoLbl.setStyle("-fx-text-fill: white;");
        logoBox.getChildren().addAll(logoRect, logoLbl);

        // Title
        Label titleLbl = new Label("Group Chat");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 26));
        titleLbl.setStyle("-fx-text-fill: " + TEXT + ";");

        Label subtitleLbl = new Label("Join the conversation");
        subtitleLbl.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 14;");

        // Divider
        Region div = new Region();
        div.setPrefHeight(1);
        div.setMaxWidth(Double.MAX_VALUE);
        div.setStyle("-fx-background-color: " + BORDER + ";");

        // Username field
        Label fieldLbl = new Label("USERNAME");
        fieldLbl.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-weight: bold; -fx-font-size: 11;");
        fieldLbl.setMaxWidth(Double.MAX_VALUE);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username…");
        usernameField.setMaxWidth(Double.MAX_VALUE);
        usernameField.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-prompt-text-fill: " + MUTED + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12 14;" +
            "-fx-font-size: 14;"
        );

        // Server info row
        HBox serverRow = new HBox(6);
        serverRow.setAlignment(Pos.CENTER_LEFT);
        Circle serverDot = new Circle(4, Color.web(GREEN));
        Label serverLbl = new Label("Connecting to " + host + ":" + port);
        serverLbl.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 12;");
        serverRow.getChildren().addAll(serverDot, serverLbl);

        // Error label
        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 12;");
        errorLbl.setWrapText(true);
        errorLbl.setMaxWidth(Double.MAX_VALUE);

        // Connect button
        Button connectBtn = new Button("Connect  →");
        connectBtn.setMaxWidth(Double.MAX_VALUE);
        connectBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        connectBtn.setStyle(
            "-fx-background-color: " + ACCENT + ";" +
            "-fx-text-fill: #0F1117;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12;" +
            "-fx-cursor: hand;"
        );
        connectBtn.setOnMouseEntered(e ->
            connectBtn.setStyle("-fx-background-color: #79B8FF; -fx-text-fill: #0F1117;" +
                                "-fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;"));
        connectBtn.setOnMouseExited(e ->
            connectBtn.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: #0F1117;" +
                                "-fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;"));

        // Read-only note
        Label noteLbl = new Label("Leave empty to join as read-only observer");
        noteLbl.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 11; -fx-font-style: italic;");

        card.getChildren().addAll(
            logoBox, titleLbl, subtitleLbl, div,
            fieldLbl, usernameField, serverRow, errorLbl,
            connectBtn, noteLbl
        );

        root.getChildren().addAll(glow, card);

        // Connect action
        Runnable doConnect = () -> {
            errorLbl.setText("Connecting…");
            connectBtn.setDisable(true);
            String name = usernameField.getText().trim();
            new Thread(() -> {
                ClientModel m = new ClientModel();
                m.setListener(this);
                boolean ok = m.connect(host, port, name);
                Platform.runLater(() -> {
                    connectBtn.setDisable(false);
                    if (!ok) {
                        errorLbl.setText("Could not connect to " + host + ":" + port);
                        return;
                    }
                    model    = m;
                    username = name.isEmpty() ? "Guest" : name;
                    readOnly = name.isEmpty();
                    showChatScene();
                });
            }, "connect-thread").start();
        };
        connectBtn.setOnAction(e -> doConnect.run());
        usernameField.setOnAction(e -> doConnect.run());

        stage.setTitle("Group Chat — Sign In");
        stage.setScene(new Scene(root, 540, 560));
        stage.setResizable(false);
        stage.show();
    }

    // ── Chat scene ───────────────────────────────────────────
    private void showChatScene() {
        GridPane root = new GridPane();
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setPadding(new Insets(0));

        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().add(col);

        RowConstraints topRow = new RowConstraints();
        topRow.setPrefHeight(64);
        RowConstraints midRow = new RowConstraints();
        midRow.setVgrow(Priority.ALWAYS);
        RowConstraints botRow = new RowConstraints();
        botRow.setPrefHeight(72);
        root.getRowConstraints().addAll(topRow, midRow, botRow);

        root.add(buildChatHeader(), 0, 0);
        root.add(buildMessageArea(), 0, 1);
        root.add(buildInputBar(), 0, 2);

        if (readOnly) {
            msgField.setDisable(true);
            sendBtn.setDisable(true);
            msgField.setPromptText("You are in READ-ONLY mode — observing the chat");
            addSystemMessage("⚠  You are in READ-ONLY MODE. You can read but not send messages.");
        }

        stage.setTitle("Group Chat — " + username + (readOnly ? " [READ-ONLY]" : ""));
        stage.setScene(new Scene(root, 820, 640));
        stage.setResizable(true);
        stage.setMinWidth(560);
        stage.setMinHeight(460);
        stage.setOnCloseRequest(e -> { if (model != null) model.disconnect(); Platform.exit(); });
    }

    // ── Chat header ──────────────────────────────────────────
    private HBox buildChatHeader() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 22, 0, 22));
        bar.setStyle(
            "-fx-background-color: " + SURFACE + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;"
        );

        // User avatar
        String myColor = getOrAssignColor(username);
        StackPane avatar = buildAvatar(username, myColor, 38);

        // Name + status
        VBox info = new VBox(2);
        Label nameLbl = new Label(username);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLbl.setStyle("-fx-text-fill: " + TEXT + ";");

        HBox statusRow = new HBox(6);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusDot = new Circle(5, Color.web(GREEN));
        statusDot.setEffect(new Glow(0.7));
        statusLbl = new Label("Online");
        statusLbl.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 12;");
        statusRow.getChildren().addAll(statusDot, statusLbl);

        info.getChildren().addAll(nameLbl, statusRow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badges
        if (readOnly) {
            Label badge = new Label("READ-ONLY");
            badge.setStyle(
                "-fx-background-color: " + RED + "22;" +
                "-fx-text-fill: " + RED + ";" +
                "-fx-font-weight: bold; -fx-font-size: 11;" +
                "-fx-padding: 4 12; -fx-background-radius: 20;" +
                "-fx-border-color: " + RED + "55; -fx-border-radius: 20; -fx-border-width: 1;"
            );
            bar.getChildren().addAll(avatar, info, spacer, badge);
        } else {
            Label hintLbl = new Label("allUsers  ·  end / bye");
            hintLbl.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 11;");
            bar.getChildren().addAll(avatar, info, spacer, hintLbl);
        }

        return bar;
    }

    // ── Message area ─────────────────────────────────────────
    private ScrollPane buildMessageArea() {
        msgContainer = new VBox(2);
        msgContainer.setPadding(new Insets(16, 22, 8, 22));
        msgContainer.setFillWidth(true);
        msgContainer.setStyle("-fx-background-color: " + BG + ";");

        msgScroll = new ScrollPane(msgContainer);
        msgScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        msgScroll.setFitToWidth(true);
        msgScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        msgScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        GridPane.setVgrow(msgScroll, Priority.ALWAYS);

        // Auto-scroll to bottom on new messages
        msgContainer.heightProperty().addListener((obs, o, n) -> msgScroll.setVvalue(1.0));

        return msgScroll;
    }

    // ── Input bar ────────────────────────────────────────────
    private HBox buildInputBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(14, 22, 14, 22));
        bar.setStyle(
            "-fx-background-color: " + SURFACE + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1 0 0 0;"
        );

        // Input wrapper (rounded pill)
        HBox inputWrap = new HBox(10);
        inputWrap.setAlignment(Pos.CENTER_LEFT);
        inputWrap.setPadding(new Insets(0, 10, 0, 14));
        inputWrap.setStyle(
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 24;" +
            "-fx-border-width: 1;"
        );
        HBox.setHgrow(inputWrap, Priority.ALWAYS);

        msgField = new TextField();
        msgField.setPromptText("Message the group…");
        msgField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-prompt-text-fill: " + MUTED + ";" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 14; -fx-padding: 10 0;"
        );
        HBox.setHgrow(msgField, Priority.ALWAYS);
        inputWrap.getChildren().add(msgField);

        // Send button
        sendBtn = new Button("Send");
        sendBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        sendBtn.setStyle(
            "-fx-background-color: " + ACCENT + ";" +
            "-fx-text-fill: #0F1117;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 10 22;" +
            "-fx-cursor: hand;"
        );
        sendBtn.setOnMouseEntered(e ->
            sendBtn.setStyle("-fx-background-color: #79B8FF; -fx-text-fill: #0F1117;" +
                             "-fx-background-radius: 20; -fx-padding: 10 22; -fx-cursor: hand;"));
        sendBtn.setOnMouseExited(e ->
            sendBtn.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: #0F1117;" +
                             "-fx-background-radius: 20; -fx-padding: 10 22; -fx-cursor: hand;"));

        Runnable doSend = () -> {
            String msg = msgField.getText().trim();
            if (msg.isEmpty() || model == null) return;
            msgField.clear();
            if ("end".equalsIgnoreCase(msg) || "bye".equalsIgnoreCase(msg)) {
                model.disconnect();
                setOffline();
                return;
            }
            model.sendMessage(msg);
        };
        sendBtn.setOnAction(e -> doSend.run());
        msgField.setOnAction(e -> doSend.run());

        bar.getChildren().addAll(inputWrap, sendBtn);
        return bar;
    }

    // ── Message bubble builders ───────────────────────────────
    private void parseAndAdd(String raw) {
        if (raw.contains("***")) {
            addSystemMessage(raw);
        } else if (raw.startsWith("[Server]")) {
            addServerMessage(raw);
        } else {
            addChatMessage(raw);
        }
    }

    private void addSystemMessage(String text) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(6, 0, 6, 0));

        Label lbl = new Label(text);
        lbl.setStyle(
            "-fx-text-fill: " + MUTED + ";" +
            "-fx-font-size: 12;" +
            "-fx-font-style: italic;" +
            "-fx-padding: 4 16;" +
            "-fx-background-color: " + CARD + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;"
        );
        lbl.setWrapText(true);
        row.getChildren().add(lbl);
        msgContainer.getChildren().add(row);
    }

    private void addServerMessage(String text) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(4, 0, 4, 0));
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⚙");
        icon.setStyle("-fx-text-fill: " + ORANGE + "; -fx-font-size: 14;");

        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + ORANGE + "; -fx-font-size: 13;");
        lbl.setWrapText(true);
        HBox.setHgrow(lbl, Priority.ALWAYS);

        row.getChildren().addAll(icon, lbl);
        msgContainer.getChildren().add(row);
    }

    private void addChatMessage(String raw) {
        // Parse: [HH:MM] username: content
        String time = "", user = "", content = raw;
        if (raw.startsWith("[")) {
            int bracket = raw.indexOf(']');
            if (bracket > 0) {
                time = raw.substring(1, bracket);
                String rest = raw.substring(bracket + 2).trim();
                int colon = rest.indexOf(": ");
                if (colon > 0) {
                    user    = rest.substring(0, colon);
                    content = rest.substring(colon + 2);
                } else {
                    content = rest;
                }
            }
        }

        boolean isMe = user.equals(username);
        String color = getOrAssignColor(user);

        // Check if previous message was from same user (group messages)
        boolean grouped = false;
        if (!msgContainer.getChildren().isEmpty()) {
            Node last = msgContainer.getChildren().get(msgContainer.getChildren().size() - 1);
            Object tag = last.getUserData();
            grouped = user.equals(tag);
        }

        VBox bubble = new VBox(3);
        bubble.setMaxWidth(Double.MAX_VALUE);
        bubble.setPadding(grouped ? new Insets(1, 0, 1, 0) : new Insets(10, 0, 1, 0));
        bubble.setUserData(user);

        if (!grouped && !user.isEmpty()) {
            // Header: avatar + username + time
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            StackPane avatar = buildAvatar(user, color, 34);

            Label userLbl = new Label(user);
            userLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
            userLbl.setStyle("-fx-text-fill: " + (isMe ? ACCENT : color) + ";");

            Label timeLbl = new Label(time);
            timeLbl.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 11;");

            header.getChildren().addAll(avatar, userLbl, timeLbl);
            bubble.getChildren().add(header);

            // Content indented below avatar
            Label contentLbl = new Label(content);
            contentLbl.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 14;");
            contentLbl.setWrapText(true);
            contentLbl.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(contentLbl, new Insets(0, 0, 0, 44));
            bubble.getChildren().add(contentLbl);
        } else {
            // Grouped — just show content indented
            Label contentLbl = new Label(content);
            contentLbl.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 14;");
            contentLbl.setWrapText(true);
            contentLbl.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(contentLbl, new Insets(0, 0, 0, 44));
            bubble.getChildren().add(contentLbl);
        }

        msgContainer.getChildren().add(bubble);
    }

    // ── Helpers ──────────────────────────────────────────────
    private StackPane buildAvatar(String name, String color, double size) {
        Circle bg = new Circle(size / 2.0, Color.web(color));
        String letter = (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();
        Label lbl = new Label(letter);
        lbl.setFont(Font.font("System", FontWeight.BOLD, size * 0.38));
        lbl.setStyle("-fx-text-fill: white;");
        return new StackPane(bg, lbl);
    }

    private String getOrAssignColor(String name) {
        return userColorMap.computeIfAbsent(name,
            k -> AVATAR_COLORS[rng.nextInt(AVATAR_COLORS.length)]);
    }

    private void setOffline() {
        Platform.runLater(() -> {
            if (statusDot != null) {
                statusDot.setFill(Color.web(RED));
                statusDot.setEffect(new Glow(0.0));
            }
            if (statusLbl != null) {
                statusLbl.setText("Offline");
                statusLbl.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 12;");
            }
            if (msgField != null) msgField.setDisable(true);
            if (sendBtn  != null) sendBtn.setDisable(true);
            addSystemMessage("You have disconnected from the server.");
        });
    }

    // ── MessageListener ──────────────────────────────────────
    @Override
    public void onMessageReceived(String message) {
        Platform.runLater(() -> parseAndAdd(message));
    }

    @Override
    public void onDisconnected() {
        setOffline();
    }

    @Override
    public void onError(String message) {
        Platform.runLater(() -> addServerMessage("[Error] " + message));
    }
}
