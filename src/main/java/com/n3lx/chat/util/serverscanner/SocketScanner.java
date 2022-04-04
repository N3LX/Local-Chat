package com.n3lx.chat.util.serverscanner;

import com.n3lx.chat.util.Message;
import com.n3lx.chat.util.SocketStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

class SocketScanner implements Callable<Boolean> {

    private final String ipAddress;
    private final int port;

    public SocketScanner(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public Boolean call() {
        try {
            //Attempt connection to a socket
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, port), 500);

            //If no exception was caught thus far - check if other side
            //of the connection is indeed an instance of Server class
            SocketStream potentialServer = new SocketStream(socket);

            Message serverName = (Message) potentialServer.getObjectInputStream().readObject();
            Message serverWelcomeMessage = (Message) potentialServer.getObjectInputStream().readObject();

            //If objects from input stream could be cast to Message class assume it is a Server instance,
            //now inform the server that it was just a ping and that this scan is not a Client
            potentialServer.getObjectOutputStream()
                    .writeObject(new Message("Ping", "", Message.MESSAGE_TYPE.ACTION));

            potentialServer.close();
            return true;
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }

}
