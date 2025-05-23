package io.nemesis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Thread{

    private Socket conn;
    private String clientIp;
    private TCPServer tcpServer;
    private BufferedReader reader;
    private OutputStream writer;
    private LocalDateTime lastMessage;
    private int strikeCount;

    private static final Logger logger = Logger.getLogger("Client");

    public Client(Socket conn, TCPServer tcpServer) throws IOException {
        this.conn = conn;
        this.clientIp = conn.getRemoteSocketAddress().toString();
        this.tcpServer = tcpServer;
        this.reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        this.writer = conn.getOutputStream();
        this.strikeCount = 0;
        this.lastMessage = LocalDateTime.now();
    }

    public int getStrikeCount() {
        return strikeCount;
    }

    public void setStrikeCount(int strikeCount) {
        this.strikeCount = strikeCount;
    }

    public LocalDateTime getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LocalDateTime lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getClientIp() {
        return clientIp;
    }

    public OutputStream getWriter() {
        return writer;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Client Initiated.");
        try {
            String message;
            while((message = reader.readLine()) != null) {
                logger.log(Level.INFO, "Received Message from Client : "+ message);
                if (message.equalsIgnoreCase(":quit")) {
                    this.conn.close();
                }
                this.tcpServer.handleMessages(this, message + "\n");
            }
            logger.log(Level.INFO, "Post While Looop!");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could Not Read Message From Client : " + e.getMessage(), e);
        }
    }
}
