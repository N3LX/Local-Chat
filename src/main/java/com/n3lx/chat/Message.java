package com.n3lx.chat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Objects of this class will be exchanged between client and server to allow for more complex communication.
 */
public class Message implements Serializable {

    private final String message;

    private final String username;

    private final LocalDateTime timestamp;

    private final MESSAGE_TYPE messageType;

    public Message(String message, String username, MESSAGE_TYPE messageType) {
        if (message == null || username == null || messageType == null) {
            throw new IllegalArgumentException("None of the provided values can be null");
        }

        this.message = message;
        this.username = username;
        this.messageType = messageType;
        timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public MESSAGE_TYPE getMessageType() {
        return messageType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /*
        Enum that will allow the server/client classes to differentiate between end user and server-client communication
        */
    enum MESSAGE_TYPE {STANDARD, ACTION}

}
