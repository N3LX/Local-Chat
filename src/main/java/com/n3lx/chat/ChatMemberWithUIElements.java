package com.n3lx.chat;

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

    protected synchronized void updateLocalUserListBox(ListView<String> newUserListBox) {
        userListBox.getItems().clear();
        userListBox.getItems().addAll(newUserListBox.getItems());
    }

    protected void appendMessageToChatBox(Message message) {
        chatBox.getItems().add(message.getUsername() + ": " + message.getMessage());
    }

}
