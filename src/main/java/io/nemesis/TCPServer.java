package io.nemesis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer {
    //consts
    public int PORT = 6969;

    public static final Logger logger = Logger.getLogger("TCPServer");

    public ConcurrentHashMap<String, Client> clientConnectionMap = new ConcurrentHashMap<>();

    public void startServer(){
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            logger.log(Level.INFO, "Server Started and Listening on Port : "+PORT);
            boolean serverRunning = true;
            while(serverRunning){
                Socket connection = serverSocket.accept();
                logger.log(Level.INFO, "Client Connected with IP : "+connection.getInetAddress().getHostAddress());
                Client client = new Client(connection, this);
                clientConnectionMap.put(connection.getRemoteSocketAddress().toString(), client);
                client.start();
            }
        }catch (IOException e){
            logger.log(Level.SEVERE, "Could Not Start Server at Port : "+PORT, e);
            System.exit(1);
        }
    }

    public void handleMessages(Socket conn, String message) throws IOException {
        broadcastMessage(conn, message);
    }

    public void broadcastMessage(Socket socketConn, String message) throws IOException {
        for(var conn : clientConnectionMap.values()){
            if(!conn.getClientIp().equalsIgnoreCase(socketConn.getRemoteSocketAddress().toString())){
                conn.getWriter().write(message.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

}
