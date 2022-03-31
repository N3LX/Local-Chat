package com.n3lx.chat.client;

import com.n3lx.chat.Message;
import com.n3lx.chat.server.Server;
import com.n3lx.chat.util.Settings;
import com.n3lx.chat.util.SocketStream;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String userName;
    private final String ipAddress;
    private final ListView<String> chatBox;
    private final ListView<String> userListBox;

    private Socket socket;
    private SocketStream serverStream;
    private ExecutorService clientThreads;

    public Client(String ipAddress, String userName, ListView<String> chatBox, ListView<String> userListBox) {
        this.ipAddress = ipAddress;
        this.userName = userName;
        this.chatBox = chatBox;
        this.userListBox = userListBox;
    }

    public void start() {
        connect();
        performHandshakeWithServer();

        clientThreads = Executors.newCachedThreadPool();
        startMessageHandler();
    }

    public void stop() {
        try {
            serverStream.close();
            clientThreads.shutdown();

            while (!clientThreads.isTerminated()) {
                Thread.sleep(10);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error has occurred when attempting to disconnect from the server", e);
        } catch (InterruptedException ignored) {

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

    private void sendMessage(String message, Message.MESSAGE_TYPE messageType) {
        try {
            Message msg = new Message(message, userName, Message.MESSAGE_TYPE.STANDARD);
            serverStream.getObjectOutputStream().writeObject(msg);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error has occurred during sending a message to the server", e);
        }
    }

    private void connect() {
        try {
            socket = new Socket(ipAddress, Settings.PORT);
            serverStream = new SocketStream(socket);
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
            sendMessage("", Message.MESSAGE_TYPE.ACTION);
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
                        case STANDARD:
                            appendMessageToChatBox(message);
                            break;
                        case ACTION:
                            processActionMessage(message);
                            break;
                    }
                } catch (SocketTimeoutException ignored) {
                    //No activity on the socket, can proceed further
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
            default:
                throw new UnsupportedOperationException("Unknown request was received from server.");
        }
    }

    private void appendMessageToChatBox(Message message) {
        StringBuilder msg = new StringBuilder();
        msg.append(message.getUsername()).append(": ").append(message.getMessage());
        chatBox.getItems().add(msg.toString());
    }

    private synchronized void updateLocalUserListBox(ListView<String> newUserListBox) {
        userListBox.getItems().clear();
        userListBox.getItems().addAll(newUserListBox.getItems());
    }

}
