package com.n3lx.chat.server;

import com.n3lx.chat.ChatMemberWithUIElements;
import com.n3lx.chat.Message;
import com.n3lx.chat.util.Settings;
import com.n3lx.chat.util.SocketStream;
import com.n3lx.chat.util.serverscanner.ServerScanner;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends ChatMemberWithUIElements {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String serverName;
    private final String welcomeMessage;

    private final ReentrantLock clientStreamsLock;
    private ServerSocket serverSocket;
    private HashMap<String, SocketStream> clientStreams;
    private ExecutorService serverThreads;

    public Server(String serverName, String welcomeMessage, ListView<String> chatBox, ListView<String> userListBox) {
        super(chatBox, userListBox);
        this.serverName = serverName;
        this.welcomeMessage = welcomeMessage;
        clientStreamsLock = new ReentrantLock(true);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(Settings.PORT);
            serverThreads = Executors.newCachedThreadPool();
            clientStreams = new HashMap<>();

            startIncomingConnectionHandler();
            startMessageHandler();

            //Inform user about successful startup
            appendMessageToChatBox(new Message("Server has started, your IP is: " + ServerScanner.getLocalhostIP()
                    , this.serverName
                    , Message.MESSAGE_TYPE.STANDARD));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error has occurred during server startup", e);
        }
    }

    public void stop() {
        try {
            //Inform the connected clients about shutdown
            sendMessage(new Message("Server will shut down shortly", serverName, Message.MESSAGE_TYPE.STANDARD));
            appendMessageToChatBox(new Message("Server is shutting down...", serverName, Message.MESSAGE_TYPE.STANDARD));
            sendMessage(new Message("shutdown:", serverName, Message.MESSAGE_TYPE.ACTION));

            serverThreads.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error has occurred during server closure", e.toString());
        } finally {
            //Close all connections
            clientStreamsLock.lock();
            for (SocketStream client : clientStreams.values()) {
                try {
                    client.close();
                } catch (IOException ignored) {

                }
            }
            clientStreamsLock.unlock();

            //Empty connected users list box
            userListBox.getItems().clear();
        }
    }

    private void startIncomingConnectionHandler() {
        /*
        A runnable that will process all incoming connections
        */
        Runnable incomingConnectionHandler = () -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    SocketStream clientStream = new SocketStream(clientSocket);

                    //Perform handshake with the client
                    Message serverName = new Message("Welcome to " + this.serverName,
                            this.serverName, Message.MESSAGE_TYPE.STANDARD);
                    clientStream.getObjectOutputStream().writeObject(serverName);

                    Message serverWelcomeMessage = new Message("Message of the day:\n" + welcomeMessage,
                            this.serverName, Message.MESSAGE_TYPE.STANDARD);
                    clientStream.getObjectOutputStream().writeObject(serverWelcomeMessage);

                    Message clientResponse = (Message) clientStream.getObjectInputStream().readObject();

                    //Check if the response matches what is expected of a Client instance
                    if (clientResponse.getMessage().equals("Handshake success")) {
                        //Add client to the list and announce it to all clients
                        clientStreamsLock.lock();

                        clientStreams.put(clientResponse.getUsername(), clientStream);

                        Message newUserAnnouncement = new Message(clientResponse.getUsername() + " joined the chat.",
                                this.serverName, Message.MESSAGE_TYPE.STANDARD);
                        clientStream.getObjectOutputStream().writeObject(newUserAnnouncement);
                        appendMessageToChatBox(newUserAnnouncement);

                        //Update the user list box
                        var newUserListBox = new ListView<String>();
                        newUserListBox.getItems().addAll(userListBox.getItems());
                        newUserListBox.getItems().add(clientResponse.getUsername());
                        updateLocalUserListBox(newUserListBox);
                        updateClientsUserListBox(newUserListBox);

                        clientStreamsLock.unlock();
                    }
                } catch (IOException | ClassNotFoundException ignored) {

                }
            }
        };
        serverThreads.submit(incomingConnectionHandler);
    }

    private void startMessageHandler() {
        Runnable messageHandler = () -> {
            while (!serverSocket.isClosed()) {
                clientStreamsLock.lock();
                for (SocketStream client : clientStreams.values()) {
                    try {
                        Message message = (Message) client.getObjectInputStream().readObject();

                        switch (message.getMessageType()) {
                            case STANDARD:
                                sendMessage(message);
                                appendMessageToChatBox(message);
                                break;
                            case ACTION:
                                processActionMessage(message);
                                break;
                        }
                    } catch (SocketTimeoutException ignored) {
                        //No activity on the socket, can proceed further
                    } catch (ClassNotFoundException | IOException ignored) {

                    }
                }
                clientStreamsLock.unlock();
            }
        };
        serverThreads.submit(messageHandler);
    }

    private void sendMessage(Message message) {
        clientStreamsLock.lock();
        try {
            for (SocketStream recipient : clientStreams.values()) {
                recipient.getObjectOutputStream().writeObject(message);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error has occurred when sending a message", e);
        }
        clientStreamsLock.unlock();
    }

    private synchronized void updateClientsUserListBox(ListView<String> newUserListBox) {
        StringBuilder messageContents = new StringBuilder();
        messageContents.append("userlistboxupdate:");
        for (String user : newUserListBox.getItems()) {
            messageContents.append(user).append(",");
        }
        Message updateMessage = new Message(messageContents.toString(), serverName, Message.MESSAGE_TYPE.ACTION);
        sendMessage(updateMessage);
    }

    private void processActionMessage(Message message) {
        String action = message.getMessage().split(":")[0];
        switch (action) {
            case "disconnect":
                //Remove the client from the clientStreams list
                clientStreams.remove(message.getUsername());

                //Remove the client for userBoxList and inform other clients about the change
                var newUserListBox = new ListView<String>();
                newUserListBox.getItems().addAll(userListBox.getItems());
                newUserListBox.getItems().remove(message.getUsername());
                updateLocalUserListBox(newUserListBox);
                updateClientsUserListBox(newUserListBox);

                //Send a message to other clients about this event as well
                var exitMessage = new Message(message.getUsername() + " has left the chat."
                        , serverName, Message.MESSAGE_TYPE.STANDARD);
                sendMessage(exitMessage);
                appendMessageToChatBox(exitMessage);
                break;
            default:
                throw new UnsupportedOperationException("Unknown request was received from a client.");
        }
    }

}
