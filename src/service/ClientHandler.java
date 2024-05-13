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

public class ClientHandler implements Runnable {
    public static HashMap<ClientHandler, String> clientHandlers = new HashMap<>(); // talvez tenha que ser sett
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
            // this.clientUsername = bufferedReader.readLine();
            // clientHandlers.add(this);
            // broadcastMessage("SERVER: " + clientUsername + " has entered the chat");

            this.chatRoomService = ChatRoomService.getInstance();
        } catch (IOException e) {
            closeEverything(clientSocket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        while ((clientSocket.isConnected())) {
            try {
                String messageFromClient;
                messageFromClient = bufferedReader.readLine();
                String[] words = messageFromClient.split(" ");
                String messageToSend;
                switch (words[0]) {
                    case "REGISTRO":
                        if(userExists(words[1])) {
                            System.out.println("User already exists");
                            messageToSend = "ERRO User already exists. Enter a new username";
                            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            bufWriter.write(messageToSend);
                            bufWriter.newLine();
                            bufWriter.flush();
                            break;
                        }
                        System.out.println("Usuario criado");
                        registerUser(words[1]);
                        messageToSend = "REGISTRO_OK";
                        for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                            if(ClientHandler.clientUsername.equals(clientUsername)) {
                                if (ClientHandler.clientUsername.equals(clientUsername)) {
                                    ClientHandler.bufferedWriter.write(messageToSend);
                                    ClientHandler.bufferedWriter.newLine();
                                    ClientHandler.bufferedWriter.flush();
                                }
                            }
                        }
                        break;
                    case "CRIAR_SALA":
                        if(!userExists(clientUsername)) { 
                            System.out.println("User does not exists");
                            messageToSend = "ERRO User does not exists. Register modafoca";
                            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            bufWriter.write(messageToSend);
                            bufWriter.newLine();
                            bufWriter.flush();
                            break;
                        }

                        if (chatRoomService.chatRoomNameExists(words[2])) {
                            System.out.println("Chat Room Name already exists");
                            messageToSend = "Chat Room Name already exists. Enter a new name";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());
                            break;
                        }
                        if(words[1].equals("PUBLICA")) {

                            chatRoomService.createPublicChatRoom(words[2], clientUsername);
                            System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());

                        } else {
                            chatRoomService.createPrivateChatRoom(words[2], words[3], clientUsername);
                        }
                        break;
                    case "LISTAR_SALAS":
                        if(!userExists(clientUsername)) { 
                            System.out.println("User does not exists");
                            messageToSend = "ERRO User does not exists. Register modafoca";
                            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            bufWriter.write(messageToSend);
                            bufWriter.newLine();
                            bufWriter.flush();
                            break;
                        }
                        for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                            if(ClientHandler.clientUsername.equals(clientUsername)) {
                                if (ClientHandler.clientUsername.equals(clientUsername)) {
                                    messageToSend = chatRoomService.getAllChatRooms().toString(); // arrumar print
                                    ClientHandler.bufferedWriter.write(chatRoomService.getAllChatRooms().toString());
                                    ClientHandler.bufferedWriter.newLine();
                                    ClientHandler.bufferedWriter.flush();
                                }
                            }
                        }

                        break;
                    case "ENTRAR_SALA":
                        if(!userExists(clientUsername)) { 
                            System.out.println("User does not exists");
                            messageToSend = "ERRO User does not exists. Register modafoca";
                            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            bufWriter.write(messageToSend);
                            bufWriter.newLine();
                            bufWriter.flush();
                            break;
                        }
                        if (!chatRoomService.chatRoomNameExists(words[1])) { 
                            System.out.println("Chat Room does not exist or tipped wrong name");
                            messageToSend = "Chat Room does not exist or tipped wrong name";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        int index = chatRoomService.getChatRoomIndexByName(words[1]);
                        
                        if (chatRoomService.checkUserInChatRoom(clientUsername, index)) {
                            System.out.println("Client already in this Room");
                            messageToSend = "You are already in this room";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        // System.out.println("Teste1:" + !chatRoomService.comparePassword(index,
                        // words[2]));
                        if (chatRoomService.requiresPassword(index)
                                && !chatRoomService.comparePassword(index, words[2])) {
                            System.out.println("Wrong password");
                            messageToSend = "Wrong password";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }

                        chatRoomService.joinChatRoom(index, clientUsername);
                        System.out.println("Chat Room Joined: " + chatRoomService.showParticipants(index));

                        break;
                    case "SAIR_SALA":
                        if(!userExists(clientUsername)) { 
                            System.out.println("User does not exists");
                            messageToSend = "ERRO User does not exists. Register modafoca";
                            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            bufWriter.write(messageToSend);
                            bufWriter.newLine();
                            bufWriter.flush();
                            break;
                        }
                        if (!chatRoomService.chatRoomNameExists(words[1])) {
                            System.out.println("Chat Room does not exist or tipped wrong name");
                            messageToSend = "Chat Room does not exist or tipped wrong name";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        index = chatRoomService.getChatRoomIndexByName(words[1]);
                        if (!chatRoomService.checkUserInChatRoom(clientUsername, index)) {
                            System.out.println("Client is not a member in this Room");
                            messageToSend = "You're not a member in this Room";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        chatRoomService.leaveChatRoom(index, clientUsername);
                        System.out.println("Chat Room Participants: " + chatRoomService.showParticipants(index));
                        break;

                    case "ENVIAR_MENSAGEM":
                    if(!userExists(clientUsername)) { 
                        System.out.println("User does not exists");
                        messageToSend = "ERRO User does not exists. Register modafoca";
                        BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        bufWriter.write(messageToSend);
                        bufWriter.newLine();
                        bufWriter.flush();
                        break;
                    }
                    if(!chatRoomService.chatRoomNameExists(words[1])){  
                        System.out.println("Chat Room does not exist or tipped wrong name");
                        messageToSend = "Chat Room does not exist or tipped wrong name";
                        for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                            if(ClientHandler.clientUsername.equals(clientUsername)) {
                                if (ClientHandler.clientUsername.equals(clientUsername)) {
                                    ClientHandler.bufferedWriter.write(messageToSend);
                                    ClientHandler.bufferedWriter.newLine();
                                    ClientHandler.bufferedWriter.flush();
                                }
                            }
                        }
                        break;
                    }

                    // fazer listagem + adcionar os usarios no meu chatHandler
                    //System.out.println("Message: " + words[2]);
                    //System.out.println("Chat Room: " + words[1]);   
                    //System.out.println("Cliente : " +  clientUsername);
                    index = chatRoomService.getChatRoomIndexByName(words[1]);
                    Set<String> chat_participants;
                    chat_participants = chatRoomService.showParticipants(index);
                    if(!chat_participants.contains(clientUsername)){
                        System.out.println("The user does not belong to the room");
                        messageToSend = "The user does not belong to the room";
                        for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                            if(ClientHandler.clientUsername.equals(clientUsername)) {
                                if (ClientHandler.clientUsername.equals(clientUsername)) {
                                    ClientHandler.bufferedWriter.write(messageToSend);
                                    ClientHandler.bufferedWriter.newLine();
                                    ClientHandler.bufferedWriter.flush();
                                }
                            }
                        }
                        break;
                    }

                    //for que vai comecar do words2 até o final, String Message +=  " " + words[i]
                    // System.out.println("Participants: " + chat_participants);
                    for(String participant : chat_participants){
                        broadcastMessageChat(words[2], participant, words[1]); 
                    }
                    break;

                    case "FECHAR_SALA":
                    if(!userExists(clientUsername)) { 
                        System.out.println("User does not exists");
                        messageToSend = "ERRO User does not exists. Register modafoca";
                        BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        bufWriter.write(messageToSend);
                        bufWriter.newLine();
                        bufWriter.flush();
                        break;
                    }
                        if (!chatRoomService.chatRoomNameExists(words[1])) {
                            System.out.println("Chat Room does not exist or tipped wrong name");
                            messageToSend = "Chat Room does not exist or tipped wrong name";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        index = chatRoomService.getChatRoomIndexByName(words[1]);
                        System.out.println("Client Username: " + clientUsername);
                        if (!chatRoomService.checkAdminInChatRoom(clientUsername, index)) {
                            System.out.println("Client is not an administrator in this Room");
                            messageToSend = "You're not an administrator in this Room";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        
                        chatRoomService.closeChatRoom(index);
                        break;

                    case "BANIR_USUARIO":
                        if(!userExists(clientUsername)) { 
                            System.out.println("User does not exists");
                            messageToSend = "ERRO User does not exists. Register modafoca";
                            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            bufWriter.write(messageToSend);
                            bufWriter.newLine();
                            bufWriter.flush();
                            break;
                        }
                        if (!chatRoomService.chatRoomNameExists(words[1])) {
                            System.out.println("Chat Room does not exist or tipped wrong name");
                            messageToSend = "Chat Room does not exist or tipped wrong name";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        index = chatRoomService.getChatRoomIndexByName(words[1]);
                        if (!chatRoomService.checkAdminInChatRoom(clientUsername, index)) {
                            System.out.println("Client is not an administrator in this Room");
                            messageToSend = "You're not an administrator in this Room";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        if (!chatRoomService.checkUserInChatRoom(words[2], index)) {
                            System.out.println("User does not exist or tipped wrong name");
                            messageToSend = "User does not exist or tipped wrong name";
                            for (ClientHandler ClientHandler : clientHandlers.keySet()) {
                                if(ClientHandler.clientUsername.equals(clientUsername)) {
                                    if (ClientHandler.clientUsername.equals(clientUsername)) {
                                        ClientHandler.bufferedWriter.write(messageToSend);
                                        ClientHandler.bufferedWriter.newLine();
                                        ClientHandler.bufferedWriter.flush();
                                    }
                                }
                            }
                            break;
                        }
                        chatRoomService.banUser(index, words[2]);
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

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers.keySet()) {
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

    public void broadcastMessageChat(String messageToSend, String participants, String chatRoom) {
        for(ClientHandler clientHandler : clientHandlers.keySet()) {
            try {
                if(!clientHandler.clientUsername.equals(clientUsername) && clientHandler.clientUsername.equals(participants)){
                    String finalMessage = "MENSAGEM " + chatRoom + " " + clientUsername + ": " + messageToSend;
                    clientHandler.bufferedWriter.write(finalMessage);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush(); 
                }
            } catch (IOException e) {
                closeEverything(clientSocket, bufferedReader, bufferedWriter);
            }
        }
    }
    public void registerUser(String username) {
        this.clientUsername = username;
        clientHandlers.put(this, username);
    }

    public boolean userExists(String username) {
        for (ClientHandler handler : clientHandlers.keySet()) {
            if (handler.clientUsername.equals(username) && username!= null) {
                return true; // Encontrou o usuário
            }
        }
        return false; // Usuário não encontrado
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat");
    }

    public void closeEverything(Socket clientSocket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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