package com.n3lx.chat.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class for allowing easier access to socket's input/output streams.
 */
public class SocketStream {

    private final Socket socket;

    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    public SocketStream(Socket socket) throws IOException {
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        socket.setSoTimeout(300);
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public void close() throws IOException {
        socket.close();
        objectInputStream.close();
        objectOutputStream.close();
    }

}
