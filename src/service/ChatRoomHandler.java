package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Set;

import utils.ServerUtils;

public class ChatRoomHandler {
    private ChatRoomService chatRoomService;
    private BufferedWriter bufferedWriter;
    private ServerUtils serverUtils;
    private Socket clientSocket;

    public ChatRoomHandler(BufferedWriter bufferedWriter, Socket clientSocket) {
        this.chatRoomService = ChatRoomService.getInstance();
        this.bufferedWriter = bufferedWriter;
        this.serverUtils = new ServerUtils(bufferedWriter);
        this.clientSocket = clientSocket;
    }

    public void sendMessage(String[] words, ConnectionHandler connectionHandler) throws IOException {
        String messageToSend;
        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);
            return;
        }
        String remetente = connectionHandler.getClientUsername();

        if(!chatRoomService.chatRoomNameExists(words[1])){  
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }
        // fazer listagem + adcionar os usarios no meu chatHandler
        //System.out.println("Message: " + words[2]);
        //System.out.println("Chat Room: " + words[1]);   
        //System.out.println("Cliente : " +  clientUsername);
        int index = chatRoomService.getChatRoomIndexByName(words[1]);
        Set<String> chat_participants;
        chat_participants = chatRoomService.showParticipants(index);
        if(!chat_participants.contains(connectionHandler.getClientUsername())){
            System.out.println("The user does not belong to the room");
            messageToSend = "The user does not belong to the room";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }
        String entireMessage = "";
        for(int i = 2; i < words.length; i++){
            entireMessage += words[i] + " ";
        }
        for(String participant : chat_participants){
            System.out.println(participant);
            serverUtils.broadcastMessageChat(entireMessage, participant, words[1], remetente); 
        }


    }

    // public void removeClientHandler(ConnectionHandler connectionHandler) {
    //     connectionHandler.getConnHandlers().remove(this);
    //     serverUtils.broadcastMessage("SERVER: " + connectionHandler.getClientUsername() + " has left the chat");
    // }

    public ChatRoomService getChatRoomService() {
        return chatRoomService;
    }

    public void createRoom(String[] words, ConnectionHandler connectionHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);
            return;
        }

        if (chatRoomService.chatRoomNameExists(words[2])) {
            System.out.println("Chat Room Name already exists");
            messageToSend = "Chat Room Name already exists. Enter a new name";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            System.out.println("ChatRooms :" + chatRoomService.getAllChatRooms());
            return;
        }

        if (words[1].equals("PUBLICA")) {

            chatRoomService.createPublicChatRoom(words[2], connectionHandler.getClientUsername());
            System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());

        } else {
            chatRoomService.createPrivateChatRoom(words[2], words[3], connectionHandler.getClientUsername());
            System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());
        }
    }

    public void listAllChatRooms(String[] words, ConnectionHandler connectionHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        messageToSend = chatRoomService.getAllChatRooms().toString(); // arrumar print

        serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
    }

    public void joinChatRoom(String[] words, ConnectionHandler connectionHandler) throws IOException {
        String messageToSend;
        
        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }


        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        if(chatRoomService.isUserBanned(connectionHandler.getClientUsername(), index)) {
            System.out.println("User banned");
            messageToSend = "ERRO User banned";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        if (chatRoomService.checkUserInChatRoom(connectionHandler.getClientUsername(), index)) {
            System.out.println("Client already in this Room");
            messageToSend = "You are already in this room";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }
        // System.out.println("Teste1:" + !chatRoomService.comparePassword(index,
        // words[2]));
        if (chatRoomService.requiresPassword(index)
                && !chatRoomService.comparePassword(index, words[2])) {
            System.out.println("Wrong password");
            messageToSend = "Wrong password";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }

        chatRoomService.joinChatRoom(index, connectionHandler.getClientUsername());
        System.out.println("Chat Room Joined: " + chatRoomService.showParticipants(index));
    }

    public void leaveChatRoom(String[] words, ConnectionHandler connectionHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        if (!chatRoomService.checkUserInChatRoom(connectionHandler.getClientUsername(), index)) {
            System.out.println("Client is not a member in this Room");
            messageToSend = "You're not a member in this Room";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }

        chatRoomService.leaveChatRoom(index, connectionHandler.getClientUsername());

        System.out.println("Chat Room Participants: " + chatRoomService.showParticipants(index));
    }

    public void deleteChatRoom(String[] words, ConnectionHandler connectionHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        System.out.println("Client Username: " + connectionHandler.getClientUsername());

        if (!chatRoomService.checkAdminInChatRoom(connectionHandler.getClientUsername(), index)) {
            System.out.println("Client is not an administrator in this Room");
            messageToSend = "You're not an administrator in this Room";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);
            return;
        }

        chatRoomService.closeChatRoom(index);
    }

    public void banUser(String[] words, ConnectionHandler connectionHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register modafoca";
            BufferedWriter bufWriter = new BufferedWriter(
            new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        if (!chatRoomService.checkAdminInChatRoom(connectionHandler.getClientUsername(), index)) {
            System.out.println("Client is not an administrator in this Room");
            messageToSend = "You're not an administrator in this Room";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }

        if (!chatRoomService.checkUserInChatRoom(words[2], index)) {
            System.out.println("User does not exist or tipped wrong name");
            messageToSend = "User does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(messageToSend, this.bufferedWriter);

            return;
        }

        chatRoomService.banUser(index, words[2]);
    }
}
