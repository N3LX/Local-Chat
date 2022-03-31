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
        //TODO Implement
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
                            //TODO Implement special message handling
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

    private void appendMessageToChatBox(Message message) {
        StringBuilder msg = new StringBuilder();
        msg.append(message.getUsername()).append(": ").append(message.getMessage());
        chatBox.getItems().add(msg.toString());
    }

    private void updateUserListBox(String users) {
        //TODO Implement
    }

}
