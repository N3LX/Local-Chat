# LocalChat

A chat application for chatting over a local network.

![Image 1](media/1.png)

## Features

- Hosting servers and joining chats
- Saving chat logs to a file
- Switch to show timestamps next to new chat messages
- Dark/Light mode switch

## How to use

### Joining an existing server

Go to **Chat** -> **Connect**, select a server and type your username. Then click **Connect**.

![Image 2](media/2.png)

*Note: The list of available servers refreshes every couple of seconds. If you don't see a server there, but you are
sure there is one running make sure that all [requirements](#requirements) are met.*

### Hosting a server

Go to **Chat** -> **Host**, select the server name and server message (a message that will be displayed to every user
that connects to your chat), then click **Host**.

![Image 3](media/3.png)

*Note: If you would like to attend the chat as well, it is perfectly safe to open another instance of this application
on the same computer. With IDE/Terminal it is rather easy, but you would like to stick to graphical interface then just
make a copy of the .jar file with a slightly different name, and you should be able to open it twice.*

## How to run/install

### Standalone .jar file

*Note: This only works for Windows and Linux machines. Mac users need to compile the application from source in order
for it to work.*

Download the JAR file from [Releases page](https://github.com/N3LX/Local-Chat/releases) and double click on it to run
the application.

### Compile from source

***You will need JDK and Maven for these steps!***

Clone the repository and open the location of your local copy in your terminal, then run the following commands:

`mvn clean package -DskipTests`

`java -jar target/local-chat-1.3-SNAPSHOT-jar-with-dependencies.jar`

## Requirements

- JRE 17
- Network that exists in a private address space (https://www.rfc-editor.org/rfc/rfc1918):
    - 10.0.0.0 - 10.255.255.255
    - 172.16.0.0 - 172.31.255.255
    - 192.168.0.0 - 192.168.255.255
- All attendants need to make sure that they have port 5005 open
- Supported operating system

Application works without issues on Windows and Linux operating systems, however on MacOS it has problems specific to
JavaFX like no application icon being displayed in Dock or necessity to compile the application from source as MacOS
security will not trust JavaFX libraries that originate from .jar file downloaded online.

## Attributions

<a href="https://www.flaticon.com/free-icons/chat-box" title="chat box icons">
Application icon was created by Freepik - Flaticon
</a>
