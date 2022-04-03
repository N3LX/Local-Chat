package com.n3lx.chat;

import com.n3lx.chat.client.Client;
import com.n3lx.chat.server.Server;
import com.n3lx.chat.util.serverscanner.ServerScanner;
import javafx.scene.control.ListView;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientServerTest extends ApplicationTest {

    @Test
    public void testHandshake() throws InterruptedException, TimeoutException {
        var serverChatBox = new ListView<String>();
        var serverUserListBox = new ListView<String>();
        var clientChatBox = new ListView<String>();
        var clientUserListBox = new ListView<String>();

        Server server = new Server("Test server", "", serverChatBox, serverUserListBox);
        Client client = new Client("localhost", "Client", clientChatBox, clientUserListBox);

        server.start();
        client.start();

        //Wait for 300ms for handshake to happen
        Thread.sleep(300);

        assertEquals(3, clientChatBox.getItems().size());
        assertEquals("Test server: Welcome to Test server", clientChatBox.getItems().get(0));
        assertEquals("Test server: Message of the day:\n", clientChatBox.getItems().get(1));
        assertEquals("Test server: Client joined the chat.", clientChatBox.getItems().get(2));

        assertEquals(2, serverChatBox.getItems().size());
        assertEquals("Test server: Server has started, your IP is: " + ServerScanner.getLocalhostIP()
                , serverChatBox.getItems().get(0));
        assertEquals("Test server: Client joined the chat.", serverChatBox.getItems().get(1));

        assertEquals(1, serverUserListBox.getItems().size());
        assertEquals("Client", serverUserListBox.getItems().get(0));

        assertEquals(1, clientUserListBox.getItems().size());
        assertEquals("Client", clientUserListBox.getItems().get(0));

        client.stop();
        server.stop();
    }

    @Test
    public void testClientSideDisconnection() throws InterruptedException, TimeoutException {
        var serverChatBox = new ListView<String>();
        var serverUserListBox = new ListView<String>();
        var clientChatBox = new ListView<String>();
        var clientUserListBox = new ListView<String>();

        Server server = new Server("Test server", "", serverChatBox, serverUserListBox);
        Client client = new Client("localhost", "Client", clientChatBox, clientUserListBox);

        server.start();
        client.start();

        //Wait for 300ms for handshake to happen
        Thread.sleep(300);

        client.stop();

        //Wait for 200ms for server to register that client has disconnected
        Thread.sleep(200);

        assertEquals(3, serverChatBox.getItems().size());
        assertEquals("Test server: Server has started, your IP is: " + ServerScanner.getLocalhostIP()
                , serverChatBox.getItems().get(0));
        assertEquals("Test server: Client joined the chat.", serverChatBox.getItems().get(1));
        assertEquals("Test server: Client has left the chat.", serverChatBox.getItems().get(2));

        assertEquals(0, serverUserListBox.getItems().size());

        server.stop();
    }

    @Test
    public void testServerSideDisconnection() throws InterruptedException, TimeoutException {
        var serverChatBox = new ListView<String>();
        var serverUserListBox = new ListView<String>();
        var clientChatBox = new ListView<String>();
        var clientUserListBox = new ListView<String>();

        Server server = new Server("Test server", "", serverChatBox, serverUserListBox);
        Client client = new Client("localhost", "Client", clientChatBox, clientUserListBox);

        server.start();
        client.start();

        //Wait for 300ms for handshake to happen
        Thread.sleep(300);

        server.stop();

        //Wait for 200ms for client to register that server has disconnected
        Thread.sleep(200);

        assertEquals(4, clientChatBox.getItems().size());
        assertEquals("Test server: Welcome to Test server", clientChatBox.getItems().get(0));
        assertEquals("Test server: Message of the day:\n", clientChatBox.getItems().get(1));
        assertEquals("Test server: Client joined the chat.", clientChatBox.getItems().get(2));
        assertEquals("Test server: Server is shutting down...", clientChatBox.getItems().get(3));

        client.stop();
    }

    @Test
    public void testUserListBoxAfterInvokingStopMethod() throws InterruptedException {
        var serverChatBox = new ListView<String>();
        var serverUserListBox = new ListView<String>();
        var clientChatBox = new ListView<String>();
        var clientUserListBox = new ListView<String>();

        Server server = new Server("Test server", "", serverChatBox, serverUserListBox);
        Client client = new Client("localhost", "Client", clientChatBox, clientUserListBox);

        server.start();
        client.start();

        //Wait for 300ms for handshake to happen
        Thread.sleep(300);

        server.stop();
        client.stop();

        //Wait for 200ms for all Platform.runLater() actions to process
        Thread.sleep(200);

        assertEquals(0, clientUserListBox.getItems().size());
        assertEquals(0, serverUserListBox.getItems().size());
    }

    @Test
    public void testSendMessage() throws InterruptedException {
        final int CLIENT_COUNT = 16;

        var serverChatBox = new ListView<String>();
        var serverUserListBox = new ListView<String>();
        Server server = new Server("Test server", "", serverChatBox, serverUserListBox);
        server.start();

        //Create a bunch of clients, connect them to the server and post a message
        var clientChatBoxes = new ArrayList<ListView<String>>();
        var clients = new ArrayList<Client>();
        for (int x = 0; x < CLIENT_COUNT; x++) {
            var clientChatBox = new ListView<String>();
            Client client = new Client("localhost", "Client " + x, clientChatBox, new ListView<>());
            client.start();

            clients.add(client);
            clientChatBoxes.add(clientChatBox);

            //Wait for 200ms so that the client establishes the connection to the server
            Thread.sleep(200);

            client.sendMessage("test message");
        }

        assertEquals(CLIENT_COUNT, serverUserListBox.getItems().size());

        //Give server a while to process all messages
        Thread.sleep(500);

        //Check if both clients and server have received their messages
        for (int x = 0; x < CLIENT_COUNT; x++) {
            assertTrue("Could not find Client " + x + " on the server chat box",
                    serverChatBox.getItems().contains("Client " + x + ": test message"));
            assertTrue("Could not find the message from Client " + x + " on the corresponding client chat box",
                    clientChatBoxes.get(x).getItems().contains("Client " + x + ": test message"));
            clients.get(x).stop();
        }
        server.stop();
    }

    @Test
    public void testSendMessageConcurrent() throws InterruptedException {
        final int CLIENT_COUNT = 32;
        ExecutorService executor = Executors.newFixedThreadPool(8);

        var serverChatBox = new ListView<String>();
        var serverUserListBox = new ListView<String>();
        Server server = new Server("Test server", "", serverChatBox, serverUserListBox);
        server.start();

        //Create a bunch of clients, connect them to the server and post a message
        var clients = new ArrayList<Client>();
        for (int x = 0; x < CLIENT_COUNT; x++) {
            Client client = new Client("localhost", "Client " + x, new ListView<>(), new ListView<>());
            clients.add(client);

            Runnable startClientAndSendMessage = () -> {
                client.start();

                //Wait for 200ms so that the client establishes the connection to the server
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {

                }

                client.sendMessage("test message");
            };
            executor.submit(startClientAndSendMessage);
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        //Wait a while so that the server processes the 32 clients and their messages
        Thread.sleep(300 * CLIENT_COUNT);

        assertEquals(CLIENT_COUNT, serverUserListBox.getItems().size());

        //Check if both clients and server have received their messages
        for (int x = 0; x < CLIENT_COUNT; x++) {
            assertTrue("Could not find Client " + x + " on the server chat box",
                    serverChatBox.getItems().contains("Client " + x + ": test message"));
            clients.get(x).stop();
        }
        server.stop();
    }

}
