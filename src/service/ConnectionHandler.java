package service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.Set;
import java.util.HashMap;
import java.security.MessageDigest;
import service.UserService;
import utils.ServerUtils;
import service.AuthenticationService;

public class ConnectionHandler implements Runnable {
    public static HashMap<ConnectionHandler, String> connHandlers = new HashMap<>(); // talvez tenha que ser sett
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public AuthenticationService authHandler;
    public boolean crypto = false;
    // private BufferedInputStream;
    private String clientUsername;
    // private ChatRoomService chatRoomService;

    public ConnectionHandler(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // this.clientUsername = bufferedReader.readLine();
            // connHandlers.add(this);
            // broadcastMessage("SERVER: " + clientUsername + " has entered the chat");

            // this.chatRoomService = ChatRoomService.getInstance();
            authHandler = new AuthenticationService(this.bufferedWriter, clientSocket);
        } catch (IOException e) {
            closeEverything();
        }
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public AuthenticationService gAuthenticationService() {
        return authHandler;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
    public HashMap<ConnectionHandler, String> getConnHandlers() {
        return connHandlers;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    @Override
    public void run() {
        UserService userService = new UserService(bufferedWriter, clientSocket);
        ChatRoomHandler chatRoomHandler = new ChatRoomHandler(bufferedWriter, clientSocket);
        while ((clientSocket.isConnected())) {
            try {
                String messageFromClient;
                messageFromClient = bufferedReader.readLine();
                try {
                    if(crypto) {
                        System.out.println("Agora ta criptografado");
                        messageFromClient = authHandler.decryptMessageFromClient(messageFromClient);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } 
                    String[] words = messageFromClient.split(" ");
                
                switch (words[0]) {
                    case "REGISTRO":
                        userService.register(words, this, authHandler);
                        break;
                    case "AUTENTICACAO":    
                        authHandler.sendPublicKeyToClient();
                        break;
                    case "CHAVE_SIMETRICA":
                        // System.out.println("CHAVE_SIMETRICA: " + words[1]);
                        authHandler.decryptSimetricKey(words[1]);
                        crypto = true;
                        break;
                        
                    case "CRIAR_SALA":
                        chatRoomHandler.createRoom(words, this, authHandler);
                        break;

                    case "LISTAR_SALAS":
                        chatRoomHandler.listAllChatRooms(words, this, authHandler);
                        break;

                    case "ENTRAR_SALA":
                        chatRoomHandler.joinChatRoom(words, this, authHandler);
                        break;

                    case "SAIR_SALA":
                        chatRoomHandler.leaveChatRoom(words, this, authHandler);
                        break;

                    case "ENVIAR_MENSAGEM":
                        chatRoomHandler.sendMessage(words, this, authHandler);
                        break;

                    case "FECHAR_SALA":
                        chatRoomHandler.deleteChatRoom(words, this, authHandler);
                        break;

                    case "BANIR_USUARIO":
                        chatRoomHandler.banUser(words, this, authHandler);
                        break;

                    default:
                        
                        break;
                }
                // broadcastMessage(messageFromClient);
            } catch (IOException ignorException) {
                closeEverything();
                break;
            }
        }
    }


    public void closeEverything() {
        // removeClientHandler();
        // ServerUtils utils = new ServerUtils(bufferedWriter);
        // utils.broadcastMessageEveryone();
        try {
            ChatRoomHandler chatRoomHandler = new ChatRoomHandler(this.bufferedWriter, this.clientSocket);

            chatRoomHandler.removeClientFromAllRooms(this);
            ConnectionHandler.connHandlers.remove(this);
            if (this.bufferedReader != null) {
                this.bufferedReader.close();
            }
            if (this.bufferedWriter != null) {
                this.bufferedWriter.close();
            }
            if (this.clientSocket != null) {
                this.clientSocket.close();
            }
        } catch (IOException ignorException) {
            
        }
    }

    public void sendMessageToUniqueClient(String message, BufferedWriter bufWriter) throws IOException {
        bufWriter.write(message);
        bufWriter.newLine();
        bufWriter.flush();
    }

    

}

// public void run() {
// try {
// ObjectOutputStream output = new
// ObjectOutputStream(clientSocket.getOutputStream());
// output.flush();
// ObjectInputStream input = new
// ObjectInputStream(clientSocket.getInputStream());

// String message = "";
// do {
// message = (String) input.readObject();
// System.out.println("Client>> " + message);

// } while (!message.equals("SAIR"));

// output.close();
// input.close();
// clientSocket.close();
// } catch (Exception e) {
// System.err.println("Error: " + e);
// }
// }