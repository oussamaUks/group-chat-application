# Group Chat Application

A real-time group chat application built with **Java Sockets (TCP)** and **JavaFX**, following a Server-Client architecture. Multiple clients connect to a central server and exchange messages in a shared chat environment.

---

## Features

### Server
- Accepts multiple simultaneous client connections (thread-per-connection)
- Broadcasts messages to all connected clients with sender username and timestamp
- Live list of connected users with colored avatar indicators
- Activity log showing all server events in real time
- Reads configuration (port) from `server.properties`

### Client
- Username authentication before entering the chat
- **Read-Only Mode** — connecting without a username restricts the user from sending messages
- Real-time messaging via **Send** button or **Enter** key
- `allUsers` command — returns the list of all currently active users
- `end` / `bye` — disconnects gracefully from the server
- Online / Offline status indicator
- Discord-style message display with user avatars and timestamps

---

## Project Structure

```
group-chat-application/
├── TCPServer/
│   ├── pom.xml
│   └── src/main/
│       ├── java/ma/project/
│       │   ├── TCPServer.java              ← Entry point
│       │   ├── model/
│       │   │   ├── ServerModel.java        ← Socket logic, thread management
│       │   │   ├── ClientHandler.java      ← Per-client thread
│       │   │   └── ServerListener.java     ← Model-View interface
│       │   └── view/
│       │       └── ServerView.java         ← JavaFX UI
│       └── resources/
│           └── server.properties
│
└── TCPClient/
    ├── pom.xml
    └── src/main/
        ├── java/ma/project/
        │   ├── TCPClient.java              ← Entry point
        │   ├── model/
        │   │   ├── ClientModel.java        ← Socket connection logic
        │   │   └── MessageListener.java    ← Model-View interface
        │   └── view/
        │       └── ClientView.java         ← JavaFX UI
        └── resources/
            └── client.properties
```

---

## Architecture

The application strictly follows **Separation of Concerns** (Model-View decoupling):

- **Model** (`ServerModel`, `ClientModel`) handles all socket communication and knows nothing about JavaFX
- **View** (`ServerView`, `ClientView`) handles all UI and knows nothing about sockets
- **Listener interfaces** (`ServerListener`, `MessageListener`) are the only bridge between the two layers

The server uses a **thread-per-connection** approach — each client gets its own `ClientHandler` thread, managed safely via a `CopyOnWriteArrayList`.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 22 |
| Network | Java Sockets (TCP) |
| GUI | JavaFX 21 |
| Layout | GridPane + JavaFX CSS |
| Build | Maven |
| IDE | IntelliJ IDEA |

---

## How to Run

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### 1. Start the Server

```bash
cd TCPServer
mvn javafx:run
```

### 2. Start the Client (open a new terminal for each client)

```bash
cd TCPClient
mvn javafx:run
```

Or with command-line arguments:

```bash
mvn javafx:run -Djavafx.args="localhost 3000"
```

> **Important:** Always use `mvn javafx:run` — do NOT use the IDE's green Run button, as it bypasses the JavaFX module path configuration.

---

## Configuration

**Server** — edit `TCPServer/src/main/resources/server.properties`:
```properties
server.port=3000
```

**Client** — edit `TCPClient/src/main/resources/client.properties`:
```properties
server.host=localhost
server.port=3000
```

---

## Usage

| Action | How |
|---|---|
| Join chat | Enter a username and click **Connect** |
| Join as observer | Leave username empty → Read-Only Mode |
| Send a message | Type and press **Enter** or click **Send** |
| List active users | Type `allUsers` and send |
| Disconnect | Type `end` or `bye` and send |






