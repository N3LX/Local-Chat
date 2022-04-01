package com.n3lx.chat;

import com.n3lx.chat.client.Client;
import com.n3lx.chat.server.Server;
import com.n3lx.chat.util.serverscanner.ServerScanner;
import javafx.scene.control.ListView;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

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
        assertEquals("Test server: Server will shut down shortly", clientChatBox.getItems().get(3));

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

        assertEquals(0, clientUserListBox.getItems().size());
        assertEquals(0, serverUserListBox.getItems().size());
    }

}
