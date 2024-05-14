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

public class ConnectionHandler implements Runnable {
    public static HashMap<ConnectionHandler, String> connHandlers = new HashMap<>(); // talvez tenha que ser sett
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
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
        } catch (IOException e) {
            closeEverything(clientSocket, bufferedReader, bufferedWriter);
        }
    }

    public String getClientUsername() {
        return clientUsername;
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
                String[] words = messageFromClient.split(" ");
                switch (words[0]) {
                    case "REGISTRO":
                        userService.register(words, this);
                        break;
                        
                    case "CRIAR_SALA":
                        chatRoomHandler.createRoom(words, this);
                        break;

                    case "LISTAR_SALAS":
                        chatRoomHandler.listAllChatRooms(words, this);
                        break;

                    case "ENTRAR_SALA":
                        chatRoomHandler.joinChatRoom(words, this);
                        break;

                    case "SAIR_SALA":
                        chatRoomHandler.leaveChatRoom(words, this);
                        break;

                    case "ENVIAR_MENSAGEM":
                        chatRoomHandler.sendMessage(words, this);
                        break;

                    case "FECHAR_SALA":
                        chatRoomHandler.deleteChatRoom(words, this);
                        break;

                    case "BANIR_USUARIO":
                        chatRoomHandler.banUser(words, this);
                        break;

                    default:
                        
                        break;
                }
                // broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(clientSocket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }


    public void closeEverything(Socket clientSocket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
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