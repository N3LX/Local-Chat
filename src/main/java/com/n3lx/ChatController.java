package com.n3lx;


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

    private boolean isRunning;

    private ChatController() {
        isRunning = false;
    }

    public static ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
        }
        return instance;
    }

    public void stopChat() {
        if (chatClass == null) {
            return;
        }
        chatClass.stop();

        //Make it not possible to restart the chat session with the same chatClass instance.
        chatClass = null;

        //Restore the UI elements to their original state from application startup
        setInitialUIState();

        isRunning = false;
    }

    public void startChat(ChatMember chatClass) {
        if (!checkIfAllUIElementsWereProvided() && chatClass != null) {
            throw new NullPointerException("ChatController class did not receive all necessary references to continue\n" +
                    "This is a critical error and as such application stability is no longer guaranteed\n");
        }

        if (isRunning) {
            throw new IllegalStateException("Cannot invoke startChat() as the session in this " +
                    "instance is already running");
        }

        this.chatClass = chatClass;

        //Depending on the instance type some UI elements will need to be modified to avoid unwanted use-cases.
        if (chatClass instanceof Server) {
            connectButton.setDisable(true);
            disconnectButton.setDisable(true);
            hostButton.setDisable(true);
            stopHostingButton.setDisable(false);
            stopHostingButton.setOnAction(actionEvent -> stopChat());
            sendButton.setDisable(true);
            messageTextField.setDisable(true);
        } else {
            Client client = (Client) chatClass;
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            disconnectButton.setOnAction(actionEvent -> stopChat());
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
        isRunning = true;
    }

    public void registerChatBox(ListView<String> chatBox) {
        if (!isRunning) {
            this.chatBox = chatBox;
            setInitialUIState();
        }
    }

    public void registerUserListBox(ListView<String> userListBox) {
        if (!isRunning) {
            this.userListBox = userListBox;
            setInitialUIState();
        }
    }

    public void registerChatMenuBarButtons(MenuItem connectButton, MenuItem disconnectButton,
                                           MenuItem hostButton, MenuItem stopHostingButton) {
        if (!isRunning) {
            this.connectButton = connectButton;
            this.disconnectButton = disconnectButton;
            this.hostButton = hostButton;
            this.stopHostingButton = stopHostingButton;
            setInitialUIState();
        }
    }

    public void registerMessageBox(TextField messageTextField, Button sendButton) {
        if (!isRunning) {
            this.messageTextField = messageTextField;
            this.sendButton = sendButton;
            setInitialUIState();
        }
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

    private void setInitialUIState() {
        if (checkIfAllUIElementsWereProvided()) {
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            hostButton.setDisable(false);
            stopHostingButton.setDisable(true);
            sendButton.setDisable(true);
            messageTextField.clear();
            messageTextField.setDisable(true);
        }
    }

}
