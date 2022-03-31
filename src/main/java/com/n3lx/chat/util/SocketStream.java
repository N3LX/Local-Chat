package com.n3lx.chat.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class for allowing easier access to socket's input/output streams.
 */
public class SocketStream {

    private Socket socket;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public SocketStream(Socket clientSocket) throws IOException {
        socket = clientSocket;
        objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
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
