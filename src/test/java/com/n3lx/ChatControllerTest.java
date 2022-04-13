package com.n3lx;

import com.n3lx.chat.server.Server;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.lang.reflect.Field;

public class ChatControllerTest extends ApplicationTest {

    private Server testServer;

    @Before
    public void instantiateServer() {
        testServer = new Server("server",
                "welcome",
                new ListView<>(),
                new ListView<>());
    }

    @Before
    /**
     * Reset the instance field of singleton controller class using reflection.
     */
    public void resetChatControllerSingleton() throws Exception {
        Field instance = ChatController.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
        instance.setAccessible(false);
    }

    @Test(expected = NullPointerException.class)
    public void testStartChatWithUnregisteredElements() {
        ChatController
                .getInstance()
                .startChat(testServer);
    }


    @Test(expected = IllegalStateException.class)
    /**
     * The method start() should fail if ran twice without using stop() first.
     */
    public void testStartChatIfRanTwiceWithRegisteredElements() {
        ChatController.getInstance().registerChatBox(new ListView<>());
        ChatController.getInstance().registerChatMenuBarButtons(new MenuItem(),
                new MenuItem(),
                new MenuItem(),
                new MenuItem());
        ChatController.getInstance().registerMessageBox(new TextField(), new Button());
        ChatController.getInstance().registerUserListBox(new ListView<>());
        try {
            ChatController.getInstance().startChat(testServer);
            ChatController.getInstance().startChat(testServer);
        } finally {
            ChatController.getInstance().stopChat();
        }

    }

    @Test(expected = NullPointerException.class)
    /**
     * The method start() should fail if ran twice without using stop() first.
     */
    public void testRestartingChatWithoutProvidingNewChatMemberClass() {
        ChatController.getInstance().registerChatBox(new ListView<>());
        ChatController.getInstance().registerChatMenuBarButtons(new MenuItem(),
                new MenuItem(),
                new MenuItem(),
                new MenuItem());
        ChatController.getInstance().registerMessageBox(new TextField(), new Button());
        ChatController.getInstance().registerUserListBox(new ListView<>());

        try {
            ChatController.getInstance().startChat(testServer);
            //Stop it and attempt to start it again with null argument
            ChatController.getInstance().stopChat();
            ChatController.getInstance().startChat(null);
        } finally {
            ChatController.getInstance().stopChat();
        }
    }

}
