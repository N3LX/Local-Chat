package com.n3lx.chat;

import com.n3lx.chat.client.Client;
import com.n3lx.chat.server.Server;
import javafx.scene.control.ListView;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.assertEquals;

public class ClientServerTest extends ApplicationTest {

    @Test
    public void testHandshake() throws InterruptedException {
        var serverChatBox = new ListView<String>();
        var serverUserListBox = new ListView<String>();
        var clientChatBox = new ListView<String>();
        var clientUserListBox = new ListView<String>();

        Server server = new Server("Test server", "Admin", "", serverChatBox, serverUserListBox);
        Client client = new Client("localhost", "Client", clientChatBox, clientUserListBox);

        server.start();
        client.start();

        Thread.sleep(100);

        assertEquals(3, clientChatBox.getItems().size());
        assertEquals("Test server: Welcome to Test server", clientChatBox.getItems().get(0));
        assertEquals("Test server: Message of the day:\n", clientChatBox.getItems().get(1));
        assertEquals("Test server: Client joined the chat.", clientChatBox.getItems().get(2));

        assertEquals(1, serverChatBox.getItems().size());
        assertEquals("Test server: Client joined the chat.", serverChatBox.getItems().get(0));

        server.stop();
        client.stop();
    }

}
