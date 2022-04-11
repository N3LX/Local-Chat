package com.n3lx.chat;

import com.n3lx.chat.util.Message;
import com.n3lx.ui.util.Preferences;
import javafx.application.Platform;
import javafx.scene.control.ListView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;

/**
 * Classes implementing this abstract class server as a model for ChatController and LocalChatApp view.
 * An abstract class that contains some common variables/methods that child classes implement in identical way.
 */
public abstract class ChatMember {

    protected final ListView<String> chatBox;
    protected final ListView<String> userListBox;

    protected ChatMember(ListView<String> chatBox, ListView<String> userList) {
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
        StringBuilder parsedMessage = new StringBuilder();

        if (Preferences.getAllowTimestamps()) {
            NumberFormat formatter = new DecimalFormat("00");
            LocalDateTime timestamp = message.getTimestamp();
            parsedMessage.append("[");
            parsedMessage.append(formatter.format(timestamp.getHour())).append(":")
                    .append(formatter.format(timestamp.getMinute())).append(":")
                    .append(formatter.format(timestamp.getSecond()));
            parsedMessage.append("] ");
        }

        parsedMessage.append(message.getUsername()).append(": ").append(message.getMessage());
        Platform.runLater(() -> chatBox.getItems().add(parsedMessage.toString()));
    }

}
