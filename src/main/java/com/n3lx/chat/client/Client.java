package com.n3lx.chat.client;

import com.n3lx.chat.ChatMemberWithUIElements;
import com.n3lx.chat.Message;
import com.n3lx.chat.server.Server;
import com.n3lx.chat.util.Settings;
import com.n3lx.chat.util.SocketStream;
import com.n3lx.ui.ChatController;
import javafx.application.Platform;
import javafx.scene.control.ListView;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends ChatMemberWithUIElements {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String userName;
    private final String ipAddress;

    private Socket socket;
    private SocketStream serverStream;
    private ExecutorService clientThreads;

    public Client(String ipAddress, String userName, ListView<String> chatBox, ListView<String> userListBox) {
        super(chatBox, userListBox);
        this.ipAddress = ipAddress;
        this.userName = userName;
    }

    public void start() {
        connect();
        performHandshakeWithServer();

        clientThreads = Executors.newCachedThreadPool();
        startMessageHandler();
    }

    public void stop() {
        try {
            //Inform the server about leaving
            sendMessage(new Message("disconnect:", userName, Message.MESSAGE_TYPE.ACTION));

            clientThreads.shutdown();
            serverStream.close();
        } catch (SocketException ignored) {
            //This only happens when server has been closed and can be safely ignored
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error has occurred when attempting to disconnect from the server", e);
        } finally {
            //Empty connected users list box
            Platform.runLater(() -> userListBox.getItems().clear());
        }
    }

    public void sendMessage(String message) {
        try {
            Message msg = new Message(message, userName, Message.MESSAGE_TYPE.STANDARD);
            serverStream.getObjectOutputStream().writeObject(msg);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error has occurred during sending a message to the server", e);
        }
    }

    private void sendMessage(Message message) {
        try {
            serverStream.getObjectOutputStream().writeObject(message);
        } catch (SocketException ignored) {
            //This only happens when server has been closed and can be safely ignored
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error has occurred during sending a message to the server", e);
        }
    }

    private void connect() {
        try {
            socket = new Socket(ipAddress, Settings.PORT);
            serverStream = new SocketStream(socket);
        } catch (SocketException ignored) {
            //This only happens when server has been closed and can be safely ignored
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error has occurred when attempting to connect to the server", e);
        }
    }

    private void performHandshakeWithServer() {
        try {
            //Expect server to provide its name and welcome message
            Message serverName = (Message) serverStream.getObjectInputStream().readObject();
            Message serverWelcomeMessage = (Message) serverStream.getObjectInputStream().readObject();
            appendMessageToChatBox(serverName);
            appendMessageToChatBox(serverWelcomeMessage);

            //Send an empty message so that the server can announce a new chat member
            sendMessage(new Message("Handshake success", userName, Message.MESSAGE_TYPE.ACTION));
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "An error has occurred during the handshake operation", e);
        }
    }

    private void startMessageHandler() {
        Runnable messageHandler = () -> {
            while (!socket.isClosed()) {
                try {
                    Message message = (Message) serverStream.getObjectInputStream().readObject();

                    switch (message.getMessageType()) {
                        case STANDARD -> appendMessageToChatBox(message);
                        case ACTION -> processActionMessage(message);
                    }
                } catch (SocketTimeoutException ignored) {
                    //No activity on the socket, can proceed further
                } catch (SocketException | EOFException ignored) {
                    ChatController.getInstance().stop();
                    var serverInterruptMessage = new Message(
                            "Connection to server has been lost, application will now disconnect.",
                            "System",
                            Message.MESSAGE_TYPE.STANDARD);
                    appendMessageToChatBox(serverInterruptMessage);
                    return;
                } catch (ClassNotFoundException | IOException e) {
                    LOGGER.log(Level.WARNING, "Could not parse incoming message", e);
                }
            }
        };
        clientThreads.submit(messageHandler);
    }

    private void processActionMessage(Message message) {
        String action = message.getMessage().split(":")[0];
        switch (action) {
            case "userlistboxupdate":
                String[] users = message.getMessage().split(":")[1].split(",");
                var newUserListBox = new ListView<String>();
                for (String user : users) {
                    newUserListBox.getItems().add(user);
                }
                updateLocalUserListBox(newUserListBox);
                break;
            case "shutdown":
                stop();
                break;
            default:
                throw new UnsupportedOperationException("Unknown request was received from server.");
        }
    }

}
