package com.n3lx.chat.server;

import com.n3lx.chat.ChatMember;
import com.n3lx.chat.util.Message;
import com.n3lx.chat.util.Settings;
import com.n3lx.chat.util.SocketStream;
import com.n3lx.chat.util.serverscanner.ServerScanner;
import javafx.application.Platform;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends ChatMember {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String serverName;
    private final String welcomeMessage;

    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, SocketStream> clientStreams;
    private ExecutorService serverThreads;

    public Server(String serverName, String welcomeMessage, ListView<String> chatBox, ListView<String> userListBox) {
        super(chatBox, userListBox);
        this.serverName = serverName;
        this.welcomeMessage = welcomeMessage;
    }

    /**
     * Starts the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(Settings.getPort());
            serverThreads = Executors.newCachedThreadPool();
            clientStreams = new ConcurrentHashMap<>();

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

    /**
     * Stops the server
     */
    public void stop() {
        try {
            //Inform the connected clients about shutdown
            sendMessage(new Message("Server is shutting down...", serverName, Message.MESSAGE_TYPE.STANDARD));
            appendMessageToChatBox(new Message("Server is shutting down...", serverName, Message.MESSAGE_TYPE.STANDARD));
            sendMessage(new Message("shutdown:", serverName, Message.MESSAGE_TYPE.ACTION));

            serverThreads.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error has occurred during server closure", e.toString());
        } finally {
            //Close all connections
            for (SocketStream client : clientStreams.values()) {
                try {
                    client.close();
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "A problem has occurred while disconnecting " +
                            "clients during shutdown sequence", e);
                }
            }

            //Empty connected users list box
            Platform.runLater(() -> userListBox.getItems().clear());
        }
    }

    /**
     * Creates and executes the Runnable that will handle new incoming socket connections
     */
    private void startIncomingConnectionHandler() {
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
                        clientStreams.put(clientResponse.getUsername(), clientStream);

                        Message newUserAnnouncement = new Message(clientResponse.getUsername() + " joined the chat.",
                                this.serverName, Message.MESSAGE_TYPE.STANDARD);
                        sendMessage(newUserAnnouncement);
                        appendMessageToChatBox(newUserAnnouncement);

                        var newUserListBox = new ListView<String>();
                        newUserListBox.getItems().addAll(clientStreams.keySet().stream().sorted().toList());
                        updateLocalUserListBox(newUserListBox);
                        updateClientsUserListBox(newUserListBox);
                    } else {
                        clientSocket.close();
                    }
                } catch (IOException | ClassNotFoundException ignored) {

                }
            }
        };
        serverThreads.submit(incomingConnectionHandler);
    }

    /**
     * Creates and executes the Runnable that will handle receiving messages from all registered sockets and then
     * distribute it to every client.
     */
    private void startMessageHandler() {
        Runnable messageHandler = () -> {
            while (!serverSocket.isClosed()) {
                for (String clientName : clientStreams.keySet()) {
                    try {
                        Message message = (Message) clientStreams.get(clientName).getObjectInputStream().readObject();

                        //If message has been received, check if it was meant for end users or for a server.
                        switch (message.getMessageType()) {
                            case STANDARD -> {
                                sendMessage(message);
                                appendMessageToChatBox(message);
                            }
                            case ACTION -> processActionMessage(message);
                        }
                    } catch (SocketTimeoutException ignored) {
                        //No activity on the socket, can proceed further
                    } catch (ClassNotFoundException | IOException e) {
                        //This will most likely mean that there was some kind of interruption on the socket and
                        //that it is no longer usable. As such we should disconnect the client.
                        disconnectClient(clientName);

                        //Send a message to other clients about this event as well
                        var exitMessage = new Message(clientName + " has disconnected from the chat."
                                , serverName, Message.MESSAGE_TYPE.STANDARD);
                        sendMessage(exitMessage);
                        appendMessageToChatBox(exitMessage);
                    }
                }

                //Wait before scanning for messages again to save computer's resources
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {

                }
            }
        };
        serverThreads.submit(messageHandler);
    }

    /**
     * Sends a message
     *
     * @param message A Message object containing all the metadata that the client should receive
     */
    private synchronized void sendMessage(Message message) {
        try {
            for (SocketStream recipient : clientStreams.values()) {
                recipient.getObjectOutputStream().writeObject(message);
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "An error has occurred when sending a message", e);
        }
    }

    /**
     * Based on the provided list create a Message object that will be later interpreted by all clients as an
     * instruction to update their respective user lists.
     *
     * @param newUserListBox
     */
    private synchronized void updateClientsUserListBox(ListView<String> newUserListBox) {
        StringBuilder messageContents = new StringBuilder();

        messageContents.append("userlistboxupdate:");
        for (String user : newUserListBox.getItems()) {
            messageContents.append(user).append(",");
        }
        Message updateMessage = new Message(messageContents.toString(), serverName, Message.MESSAGE_TYPE.ACTION);

        sendMessage(updateMessage);
    }

    /**
     * If message type was ACTION then try to interpret the request and process it if matching string was made
     *
     * @param message A Message object with a message string that contains the command to be processed
     */
    private void processActionMessage(Message message) {
        String action = message.getMessage().split(":")[0];
        switch (action) {
            case "disconnect" -> {
                //This command doesn't have additional parameters after ":" sign
                disconnectClient(message.getUsername());

                //Send a message to other clients about this event as well
                var exitMessage = new Message(message.getUsername() + " has left the chat."
                        , serverName, Message.MESSAGE_TYPE.STANDARD);
                sendMessage(exitMessage);
                appendMessageToChatBox(exitMessage);
            }
            default -> {
                String error = "Unknown request was received from server:" + "\n" +
                        "Command that was attempted to be invoked: " + message.getMessage();
                LOGGER.log(Level.WARNING, error);
            }
        }
    }

    /**
     * Safely disconnects a client from the server
     *
     * @param username A username of a client to be disconnected
     */
    private synchronized void disconnectClient(String username) {
        //Close connection to the client and remove it from the clientStreams list
        try {
            clientStreams.get(username).close();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "A problem has occurred during disconnection from the client", e);
        }
        clientStreams.remove(username);

        //Remove the client for userBoxList and inform other clients about the change
        var newUserListBox = new ListView<String>();
        newUserListBox.getItems().addAll(userListBox.getItems());
        newUserListBox.getItems().remove(username);
        updateLocalUserListBox(newUserListBox);
        updateClientsUserListBox(newUserListBox);
    }

}
