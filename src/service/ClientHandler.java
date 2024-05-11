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
import java.io.IOException;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private ChatRoomService chatRoomService;
    // private BufferedInputStream;
    private String clientUsername;

    public ClientHandler(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            // broadcastMessage("SERVER: " + clientUsername + " has entered the chat");
            
            this.chatRoomService = ChatRoomService.getInstance();
        } catch (IOException e) {
            closeEverything(clientSocket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        while((clientSocket.isConnected())) {
            try {
                String messageFromClient;

                messageFromClient = bufferedReader.readLine();
                String[] words = messageFromClient.split(" ");
                String messageToSend;
                switch (words[0]) {
                    case "CRIAR_SALA":
                        if(chatRoomService.chatRoomNameExists(words[2])) {
                            System.out.println("Chat Room Name already exists");
                            messageToSend = "Chat Room Name already exists. Enter a new name";
                            for(ClientHandler ClientHandler : clientHandlers) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    ClientHandler.bufferedWriter.write(messageToSend);
                                    ClientHandler.bufferedWriter.newLine();
                                    ClientHandler.bufferedWriter.flush(); 
                                }
                            }
                            System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());
                            break;
                        }

                        if(words[1].equals("PUBLICA")) {
                            chatRoomService.createPublicChatRoom(words[2], clientSocket.getRemoteSocketAddress().toString());
                            System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());

                        } else {
                            chatRoomService.createPrivateChatRoom(words[2], words[3], clientSocket.getRemoteSocketAddress().toString());
                        }
                        break;
                    case "LISTAR_SALAS":
                        for(ClientHandler ClientHandler : clientHandlers) {
                            if(ClientHandler.clientUsername.equals(clientUsername)) {
                                messageToSend = chatRoomService.getAllChatRooms().toString();
                                ClientHandler.bufferedWriter.write(chatRoomService.getAllChatRooms().toString());
                                ClientHandler.bufferedWriter.newLine();
                                ClientHandler.bufferedWriter.flush(); 
                            }
                        }
                        break;
                    case "ENTRAR_SALA":
                        int index = chatRoomService.getChatRoomIndexByName(words[2]);
                        System.out.println("Index: " + index);
                        if(!chatRoomService.chatRoomNameExists(words[2])) {
                            System.out.println("Chat Room does not exist or tipped wrong name");
                            messageToSend = "Chat Room does not exist or tipped wrong name";
                            for(ClientHandler ClientHandler : clientHandlers) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    ClientHandler.bufferedWriter.write(messageToSend);
                                    ClientHandler.bufferedWriter.newLine();
                                    ClientHandler.bufferedWriter.flush(); 
                                }
                            }
                            break;
                        }
                        if(chatRoomService.requiresPassword(index) && !chatRoomService.comparePassword(index, words[3])) {
                            System.out.println("Wrong password");
                            messageToSend = "Wrong password";
                            for(ClientHandler ClientHandler : clientHandlers) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    ClientHandler.bufferedWriter.write(messageToSend);
                                    ClientHandler.bufferedWriter.newLine();
                                    ClientHandler.bufferedWriter.flush(); 
                               }
                            }
                            break;
                        }
                        String userPort = clientSocket.getRemoteSocketAddress().toString();
                        chatRoomService.joinChatRoom(index, userPort);

                        break;
                    case "SAIR_SALA":
                        break;
                    case "ENVIAR_MENSAGEM":
                        break;
                    case "FECHAR_SALA":
                        break;
                    case "BANIR_USUARIO":
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

    public void broadcastMessage(String messageToSend) {
        for(ClientHandler clientHandler : clientHandlers) {
            try {
                if(!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush(); 
                }
            } catch (IOException e) {
                closeEverything(clientSocket, bufferedReader, bufferedWriter);
            }
        }
    }
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat");
    }
    public void closeEverything(Socket clientSocket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    // public void run() {
    //     try {
    //         ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
    //         output.flush();
    //         ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

    //         String message = "";
    //         do {
    //             message = (String) input.readObject();
    //             System.out.println("Client>> " + message);

                

    //         } while (!message.equals("SAIR"));

    //         output.close();
    //         input.close();
    //         clientSocket.close();
    //     } catch (Exception e) {
    //         System.err.println("Error: " + e);
    //     }
    // }