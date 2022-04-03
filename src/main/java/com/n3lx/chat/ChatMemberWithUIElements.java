package com.n3lx.chat;

import javafx.application.Platform;
import javafx.scene.control.ListView;

public abstract class ChatMemberWithUIElements {

    protected final ListView<String> chatBox;
    protected final ListView<String> userListBox;

    private ChatMemberWithUIElements() {
        chatBox = new ListView<>();
        userListBox = new ListView<>();
        throw new UnsupportedOperationException("You need to call (ListView<>,ListView<>) constructor of this class" +
                " in order for it to function properly!");
    }

    protected ChatMemberWithUIElements(ListView<String> chatBox, ListView<String> userList) {
        this.chatBox = chatBox;
        this.userListBox = userList;
    }

    public abstract void start();

    public abstract void stop();

    protected synchronized void updateLocalUserListBox(ListView<String> newUserListBox) {
        Platform.runLater(() -> {
            userListBox.getItems().clear();
            userListBox.getItems().addAll(newUserListBox.getItems());
        });
    }

    protected synchronized void appendMessageToChatBox(Message message) {
        Platform.runLater(() -> chatBox.getItems().add(message.getUsername() + ": " + message.getMessage()));
    }

}
