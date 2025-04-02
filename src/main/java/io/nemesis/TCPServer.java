package io.nemesis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer {
    public static final ZoneOffset ZERO_ZONE_OFFSET = ZoneOffset.ofTotalSeconds(0);
    //consts
    public int PORT = 6969;
    public double MESSAGES_LIMIT = 1.0;
    public int STRIKE_LIMIT = 10;
    public double BANNED_FOR = 60 * 1.0;
    public boolean IS_SENSITIVE_MODE = true;

    public static final Logger logger = Logger.getLogger("TCPServer");

    public ConcurrentHashMap<String, Client> clientConnectionMap = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, LocalDateTime> bannedClients = new ConcurrentHashMap<>();


    public void startServer(){
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            logger.log(Level.INFO, "Server Started and Listening on Port : "+PORT);
            boolean serverRunning = true;
            while(serverRunning){
                Socket connection = serverSocket.accept();
                logger.log(Level.INFO, "Client Connected with IP : "+ sensitive(connection.getRemoteSocketAddress().toString()));
                Client client = new Client(connection, this);
                clientConnectionMap.put(connection.getRemoteSocketAddress().toString(), client);
                client.start();
            }
        }catch (IOException e){
            logger.log(Level.SEVERE, "Could Not Start Server at Port : "+PORT, e);
            System.exit(1);
        }
    }

    public void handleMessages(Client client, String message) throws IOException {
        //Check if Client Ban has been served!
        if(bannedClients.containsKey(client.getClientIp())){
            if(timeDifferenceInSeconds(bannedClients.get(client.getClientIp()), LocalDateTime.now()) > BANNED_FOR){
               //Client has Served Ban Hence is now Getting Unbanned!
                bannedClients.remove(client.getClientIp());
                client.setStrikeCount(0);
            }
        }
        if(bannedClients.containsKey(client.getClientIp())){
            double bannedFor = BANNED_FOR - timeDifferenceInSeconds(bannedClients.get(client.getClientIp()), LocalDateTime.now());
            String msg = "You are Banned USER : for : " + bannedFor + " secs! \n";
            client.getWriter().write(msg.getBytes(StandardCharsets.UTF_8));
        }else{
            //client is not banned
            if(timeDifferenceInSeconds(client.getLastMessage(), LocalDateTime.now()) > MESSAGES_LIMIT){
                client.setStrikeCount(0);
                client.setLastMessage(LocalDateTime.now());
            }else{
                //Sent Message Too Early
                client.setStrikeCount(client.getStrikeCount() + 1);
                if (client.getStrikeCount() > STRIKE_LIMIT) {
                    logger.log(Level.WARNING, "USER : "+sensitive(client.getClientIp()) + " Has Exceeding Strike Limit, Hence Banning!");
                    bannedClients.put(client.getClientIp(), LocalDateTime.now());
                    client.getWriter().write("You are Banned USER for Exceeding Strike Limit \n".getBytes(StandardCharsets.UTF_8));
                    return;
                }
            }
            broadcastMessage(client, message);
        }
    }

    public void broadcastMessage(Client client, String message) throws IOException {
        for(var conn : clientConnectionMap.values()){
            if(!conn.getClientIp().equalsIgnoreCase(client.getClientIp())){
                conn.getWriter().write(message.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public long timeDifferenceInSeconds(LocalDateTime earlier, LocalDateTime later){
        return later.toEpochSecond(ZERO_ZONE_OFFSET) - earlier.toEpochSecond(ZERO_ZONE_OFFSET);
    }

    public String sensitive(String message){
        if(IS_SENSITIVE_MODE){
            return "[PROTECTED_IP]";
        }else{
            return message;
        }
    }

}
