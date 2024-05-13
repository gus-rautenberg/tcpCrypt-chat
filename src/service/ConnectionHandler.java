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
    private ChatRoomService chatRoomService;
    // private BufferedInputStream;
    private String clientUsername;

    public ConnectionHandler(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // this.clientUsername = bufferedReader.readLine();
            // connHandlers.add(this);
            // broadcastMessage("SERVER: " + clientUsername + " has entered the chat");

            this.chatRoomService = ChatRoomService.getInstance();
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

    @Override
    public void run() {
        UserService userService = new UserService(bufferedWriter, clientSocket);
        while ((clientSocket.isConnected())) {
            try {
                String messageFromClient;
                messageFromClient = bufferedReader.readLine();
                String[] words = messageFromClient.split(" ");
                String messageToSend;
                switch (words[0]) {
                    case "REGISTRO":
                        userService.register(words, this);
                        break;
                    case "CRIAR_SALA":
                        createRoom(words);
                        break;

                    case "LISTAR_SALAS":
                        listAllChatRooms(words);
                        break;

                    case "ENTRAR_SALA":
                        joinChatRoom(words);

                        break;
                    case "SAIR_SALA":
                        leaveChatRoom(words);

                        break;

                    case "ENVIAR_MENSAGEM":
                        if (!userExists(clientUsername)) {
                            System.out.println("User does not exists");
                            messageToSend = "ERRO User does not exists. Register modafoca";
                            BufferedWriter bufWriter = new BufferedWriter(
                                    new OutputStreamWriter(clientSocket.getOutputStream()));
                            sendMessageToUniqueClient(messageToSend, bufWriter);
                            break;
                        }
                        if (!chatRoomService.chatRoomNameExists(words[1])) {
                            System.out.println("Chat Room does not exist or tipped wrong name");
                            messageToSend = "Chat Room does not exist or tipped wrong name";
                            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
                            break;
                        }

                        // fazer listagem + adcionar os usarios no meu chatHandler
                        // System.out.println("Message: " + words[2]);
                        // System.out.println("Chat Room: " + words[1]);
                        // System.out.println("Cliente : " + clientUsername);
                        int index = chatRoomService.getChatRoomIndexByName(words[1]);
                        Set<String> chat_participants;
                        chat_participants = chatRoomService.showParticipants(index);
                        if (!chat_participants.contains(clientUsername)) {
                            System.out.println("The user does not belong to the room");
                            messageToSend = "The user does not belong to the room";
                            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

                            break;
                        }

                        // for que vai comecar do words2 até o final, String Message += " " + words[i]
                        // System.out.println("Participants: " + chat_participants);
                        for (String participant : chat_participants) {
                            broadcastMessageChat(words[2], participant, words[1]);
                        }
                        break;

                    case "FECHAR_SALA":
                        deleteChatRoom(words);
                        break;

                    case "BANIR_USUARIO":
                        banUser(words);
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
        for (ConnectionHandler clientHandler : connHandlers.keySet()) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
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
        for (ConnectionHandler clientHandler : connHandlers.keySet()) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)
                        && clientHandler.clientUsername.equals(participants)) {
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

    public void removeClientHandler() {
        connHandlers.remove(this);
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

    public void sendMessageToUniqueClient(String message, BufferedWriter bufWriter) throws IOException {
        bufWriter.write(message);
        bufWriter.newLine();
        bufWriter.flush();
    }

    public void createRoom(String[] words) throws IOException {
        String messageToSend;
        if (!userExists(clientUsername)) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            sendMessageToUniqueClient(messageToSend, bufWriter);
            return;
        }

        if (chatRoomService.chatRoomNameExists(words[2])) {
            System.out.println("Chat Room Name already exists");
            messageToSend = "Chat Room Name already exists. Enter a new name";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            System.out.println("ChatRooms :" + chatRoomService.getAllChatRooms());
            return;
        }
        if (words[1].equals("PUBLICA")) {

            chatRoomService.createPublicChatRoom(words[2], clientUsername);
            System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());

        } else {
            chatRoomService.createPrivateChatRoom(words[2], words[3], clientUsername);
            System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());
        }
    }

    public void listAllChatRooms(String[] words) throws IOException {
        String messageToSend;
        if (!userExists(clientUsername)) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }
        messageToSend = chatRoomService.getAllChatRooms().toString(); // arrumar print
        sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
    }

    public void joinChatRoom(String[] words) throws IOException {
        String messageToSend;
        if (!userExists(clientUsername)) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }
        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        if (chatRoomService.checkUserInChatRoom(clientUsername, index)) {
            System.out.println("Client already in this Room");
            messageToSend = "You are already in this room";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }
        // System.out.println("Teste1:" + !chatRoomService.comparePassword(index,
        // words[2]));
        if (chatRoomService.requiresPassword(index)
                && !chatRoomService.comparePassword(index, words[2])) {
            System.out.println("Wrong password");
            messageToSend = "Wrong password";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }

        chatRoomService.joinChatRoom(index, clientUsername);
        System.out.println("Chat Room Joined: " + chatRoomService.showParticipants(index));
    }

    public void leaveChatRoom(String[] words) throws IOException {
        String messageToSend;

        if (!userExists(clientUsername)) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }
        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }
        int index = chatRoomService.getChatRoomIndexByName(words[1]);
        if (!chatRoomService.checkUserInChatRoom(clientUsername, index)) {
            System.out.println("Client is not a member in this Room");
            messageToSend = "You're not a member in this Room";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }
        chatRoomService.leaveChatRoom(index, clientUsername);
        System.out.println("Chat Room Participants: " + chatRoomService.showParticipants(index));
    }

    public void deleteChatRoom(String[] words) throws IOException {
        String messageToSend;
        if (!userExists(clientUsername)) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }
        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }
        int index = chatRoomService.getChatRoomIndexByName(words[1]);
        System.out.println("Client Username: " + clientUsername);
        if (!chatRoomService.checkAdminInChatRoom(clientUsername, index)) {
            System.out.println("Client is not an administrator in this Room");
            messageToSend = "You're not an administrator in this Room";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }

        chatRoomService.closeChatRoom(index);
    }

    public void banUser(String[] words) throws IOException {
        String messageToSend;
        if (!userExists(clientUsername)) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()));
            sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }
        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }
        int index = chatRoomService.getChatRoomIndexByName(words[1]);
        if (!chatRoomService.checkAdminInChatRoom(clientUsername, index)) {
            System.out.println("Client is not an administrator in this Room");
            messageToSend = "You're not an administrator in this Room";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }
        if (!chatRoomService.checkUserInChatRoom(words[2], index)) {
            System.out.println("User does not exist or tipped wrong name");
            messageToSend = "User does not exist or tipped wrong name";
            sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }
        chatRoomService.banUser(index, words[2]);
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