package io.nemesis;

public class Application {

    public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer();
        tcpServer.startServer();
    }
}
