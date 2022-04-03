package com.n3lx.ui;


import com.n3lx.chat.ChatMemberWithUIElements;
import com.n3lx.chat.client.Client;
import com.n3lx.chat.server.Server;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

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

    private ChatMemberWithUIElements chatClass;

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
        chatClass = null;
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        hostButton.setDisable(false);
        stopHostingButton.setDisable(true);
        sendButton.setDisable(true);
        messageTextField.clear();
        messageTextField.setDisable(true);
    }

    public void startChat(ChatMemberWithUIElements chatClass) {
        this.chatClass = chatClass;
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

        chatClass.start();
    }

    public void linkChatBox(ListView<String> chatBox) {
        this.chatBox = chatBox;
    }

    public void linkUserListBox(ListView<String> userListBox) {
        this.userListBox = userListBox;
    }

    public void linkChatMenuBarButtons(MenuItem connectButton, MenuItem disconnectButton, MenuItem hostButton, MenuItem stopHostingButton) {
        this.connectButton = connectButton;
        this.disconnectButton = disconnectButton;
        this.hostButton = hostButton;
        this.stopHostingButton = stopHostingButton;
    }

    public void linkMessageBox(TextField messageTextField, Button sendButton) {
        this.messageTextField = messageTextField;
        this.sendButton = sendButton;
    }


    public ListView<String> getChatBox() {
        return chatBox;
    }

    public ListView<String> getUserListBox() {
        return userListBox;
    }

}
