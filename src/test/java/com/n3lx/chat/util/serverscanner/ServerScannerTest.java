package com.n3lx.chat.util.serverscanner;

import com.n3lx.chat.server.Server;
import com.n3lx.chat.util.Settings;
import javafx.scene.control.ListView;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class ServerScannerTest extends ApplicationTest {

    @Test
    public void testScanningWhenNoServersAreRunning() {
        assertEquals(0, ServerScanner.getListOfOnlineServers().size());
    }

    @Test
    public void testScanningIfServerIsFoundButNotAnInstanceOfServerClass() {
        Executors.newCachedThreadPool().submit(() -> {
            try {
                var dummyServer = new ServerSocket(Settings.PORT);
                var connection = dummyServer.accept();
                var dummyOutputStream = new ObjectOutputStream(connection.getOutputStream());
                var dummyInputStream = new ObjectInputStream(connection.getInputStream());

                connection.close();
                dummyOutputStream.close();
                dummyInputStream.close();
                dummyServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        var servers = ServerScanner.getListOfOnlineServers();
        assertEquals(0, servers.size());
    }

    @Test
    public void testScanningIsServerIsRunningOnTheNetwork() {
        var serverChatBox = new ListView<String>();
        var serverUserListBox = new ListView<String>();

        Server server = new Server("Test server", "", serverChatBox, serverUserListBox);
        server.start();

        var servers = ServerScanner.getListOfOnlineServers();
        assertEquals(1, servers.size());
        assertEquals(ServerScanner.getLocalhostIP(), servers.get(0));

        server.stop();
    }

}
