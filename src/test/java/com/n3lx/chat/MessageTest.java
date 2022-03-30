package com.n3lx.chat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MessageTest {

    @Test
    public void testMessageConstructorWithNonNullArguments() {
        String message = "message";
        String username = "user";

        for (Message.MESSAGE_TYPE messageType : Message.MESSAGE_TYPE.values()) {
            Message m = new Message(message, username, messageType);
            assertEquals(message, m.getMessage());
            assertEquals(username, m.getUsername());
            assertEquals(messageType, m.getMessageType());
            assertNotNull(m.getTimestamp());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMessageConstructorWithNullMessageArgument() {
        new Message(null, "username", Message.MESSAGE_TYPE.STANDARD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMessageConstructorWithNullUsernameArgument() {
        new Message("message", null, Message.MESSAGE_TYPE.STANDARD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMessageConstructorWithNullMessageTypeArgument() {
        new Message("message", "username", null);
    }

}
