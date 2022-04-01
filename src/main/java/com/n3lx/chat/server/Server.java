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
import java.util.LinkedList;
import java.util.List;
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
    private List<SocketStream> clientStreams;
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
            clientStreams = new LinkedList<>();

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
            serverThreads.shutdown();
            serverSocket.close();

            while (!serverThreads.isTerminated()) {
                Thread.sleep(10);
            }

            for (SocketStream client : clientStreams) {
                client.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error has occurred during server closure", e.toString());
        } catch (InterruptedException ignored) {

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

                        clientStreams.add(clientStream);

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
                try {
                    clientStreamsLock.lock();
                    for (SocketStream client : clientStreams) {
                        Message message = (Message) client.getObjectInputStream().readObject();

                        switch (message.getMessageType()) {
                            case STANDARD:
                                sendMessage(message);
                                appendMessageToChatBox(message);
                                break;
                            case ACTION:
                                //TODO Implement special message handling
                                break;
                        }
                    }
                    clientStreamsLock.unlock();
                } catch (SocketTimeoutException ignored) {
                    //No activity on the socket, can proceed further
                } catch (ClassNotFoundException | IOException e) {
                    LOGGER.log(Level.WARNING, "Could not parse incoming message", e);
                }
            }
        };
        serverThreads.submit(messageHandler);
    }

    private void sendMessage(Message message) {
        clientStreamsLock.lock();
        try {
            for (SocketStream recipient : clientStreams) {
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

}
