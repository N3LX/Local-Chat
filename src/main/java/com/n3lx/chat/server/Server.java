package com.n3lx.chat.server;

import com.n3lx.chat.Message;
import com.n3lx.chat.util.Settings;
import com.n3lx.chat.util.SocketStream;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String serverName;
    private final String userName;
    private final String welcomeMessage;
    private final ListView<String> chatBox;
    private final ListView<String> userListBox;

    private ServerSocket serverSocket;
    private List<SocketStream> clients;
    private ExecutorService serverThreads;

    public Server(String serverName, String userName, String welcomeMessage, ListView<String> chatBox, ListView<String> userList) {
        this.serverName = serverName;
        this.userName = userName;
        this.welcomeMessage = welcomeMessage;
        this.chatBox = chatBox;
        this.userListBox = userList;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(Settings.PORT);
            serverThreads = Executors.newCachedThreadPool();
            clients = new LinkedList<>();

            startIncomingConnectionHandler();
            startMessageHandler();
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

            for (SocketStream client : clients) {
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

                    synchronized (clients) {
                        clients.add(clientStream);
                    }

                    Message newUserAnnouncement = new Message(clientResponse.getUsername() + " joined the chat.",
                            this.serverName, Message.MESSAGE_TYPE.STANDARD);
                    clientStream.getObjectOutputStream().writeObject(newUserAnnouncement);
                    appendMessageToChatBox(newUserAnnouncement);
                } catch (IOException | ClassNotFoundException ignored) {

                }
            }
        };
        serverThreads.submit(incomingConnectionHandler);
    }

    private void startMessageHandler() {
        Runnable messageHandler = () -> {
            while (!serverSocket.isClosed()) {
                synchronized (clients) {
                    for (SocketStream client : clients) {
                        try {
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
                        } catch (SocketTimeoutException ignored) {
                            //No activity on the socket, can proceed further
                        } catch (ClassNotFoundException | IOException e) {
                            LOGGER.log(Level.WARNING, "Could not parse incoming message", e);
                        }
                    }
                }

                //Sleep for a while so that the other threads contesting the client list can access it
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        serverThreads.submit(messageHandler);
    }

    private synchronized void appendMessageToChatBox(Message message) {
        StringBuilder msg = new StringBuilder();
        msg.append(message.getUsername()).append(": ").append(message.getMessage());
        chatBox.getItems().add(msg.toString());
    }

    private synchronized void sendMessage(Message message) {
        synchronized (clients) {
            try {
                for (SocketStream recipient : clients) {
                    recipient.getObjectOutputStream().writeObject(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
