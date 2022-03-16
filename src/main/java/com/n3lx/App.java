package com.n3lx;

/**
 * The purpose of this class is solely to a call the actual LocalChatApp class that extends Application interface.
 * It is done this way solely to make it possible for a JAR file to start (it cannot start in a class that inherits
 * Application interface from JavaFX)
 */
public class App {

    public static void main(String[] args) {
        LocalChatApp.main(args);
    }

}
