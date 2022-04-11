package com.n3lx.ui;


import com.n3lx.chat.ChatMember;
import com.n3lx.chat.client.Client;
import com.n3lx.chat.server.Server;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

/**
 * A class that allows the UI to communicate with classes that make the chat activity possible.
 * It needs to be fed all required UI elements via methods starting with "link" in their name in order to run
 */
public class ChatController {

    private static ChatController instance;

    private ListView<String> chatBox;
    private ListView<String> userListBox;

    private MenuItem connectButton;
    private MenuItem disconnectButton;
    private MenuItem hostButton;
    private MenuItem stopHostingButton;

    private Button sendButton;
    private TextField messageTextField;

    private ChatMember chatClass;

    private ChatController() {

    }

    public static ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
        }
        return instance;
    }

    public void stop() {
        if (chatClass == null) {
            return;
        }
        chatClass.stop();

        //Restore the UI elements to their original state from application startup
        chatClass = null;
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        hostButton.setDisable(false);
        stopHostingButton.setDisable(true);
        sendButton.setDisable(true);
        messageTextField.clear();
        messageTextField.setDisable(true);
    }

    public void startChat(ChatMember chatClass) {
        if (!checkIfAllUIElementsWereProvided() && chatClass != null) {
            throw new NullPointerException("ChatController class did not receive all necessary references to continue\n" +
                    "This is a critical error and ass such application stability is no longer guaranteed\n");
        }

        this.chatClass = chatClass;

        //Depending on the instance type some UI elements will need to be modified to avoid unwanted use-cases.
        if (chatClass instanceof Server) {
            connectButton.setDisable(true);
            disconnectButton.setDisable(true);
            hostButton.setDisable(true);
            stopHostingButton.setDisable(false);
            stopHostingButton.setOnAction(actionEvent -> stop());
            sendButton.setDisable(true);
            messageTextField.setDisable(true);
        } else {
            Client client = (Client) chatClass;
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            disconnectButton.setOnAction(actionEvent -> stop());
            hostButton.setDisable(true);
            stopHostingButton.setDisable(true);
            sendButton.setDisable(false);
            sendButton.setOnAction(actionEvent -> {
                client.sendMessage(messageTextField.getText());
                messageTextField.clear();
            });
            messageTextField.setOnAction(sendButton.getOnAction());
            messageTextField.setDisable(false);
        }

        //Start the chat
        chatClass.start();
    }

    public void registerChatBox(ListView<String> chatBox) {
        this.chatBox = chatBox;
    }

    public void registerUserListBox(ListView<String> userListBox) {
        this.userListBox = userListBox;
    }

    public void registerChatMenuBarButtons(MenuItem connectButton, MenuItem disconnectButton, MenuItem hostButton, MenuItem stopHostingButton) {
        this.connectButton = connectButton;
        this.disconnectButton = disconnectButton;
        this.hostButton = hostButton;
        this.stopHostingButton = stopHostingButton;
    }

    public void registerMessageBox(TextField messageTextField, Button sendButton) {
        this.messageTextField = messageTextField;
        this.sendButton = sendButton;
    }


    public ListView<String> getChatBox() {
        return chatBox;
    }

    public ListView<String> getUserListBox() {
        return userListBox;
    }

    private boolean checkIfAllUIElementsWereProvided() {
        if (chatBox == null || userListBox == null) {
            return false;
        }
        if (connectButton == null || disconnectButton == null || hostButton == null || stopHostingButton == null) {
            return false;
        }
        if (messageTextField == null || sendButton == null) {
            return false;
        }
        return true;
    }

}
