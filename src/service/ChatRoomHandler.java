package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;
import model.ChatRoom;

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

    public void sendMessage(String[] words, ConnectionHandler connectionHandler, AuthenticationService authHandler) throws IOException {
        String messageToSend;
        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register please.";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);
            return;
        }
        String remetente = connectionHandler.getClientUsername();

        if(!chatRoomService.chatRoomNameExists(words[1])){  
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "ERRO Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
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
            messageToSend = "ERRO The user does not belong to the room";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
            return;
        }
        String entireMessage = "";
        for(int i = 2; i < words.length; i++){
            entireMessage += words[i] + " ";
        }
        for(String participant : chat_participants){
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

    public void createRoom(String[] words, ConnectionHandler connectionHandler, AuthenticationService authHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register please.";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);
            return;
        }

        if (chatRoomService.chatRoomNameExists(words[2])) {
            System.out.println("Chat Room Name already exists");
            messageToSend = "ERRO Chat Room Name already exists. Enter a new name";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
            // System.out.println("ChatRooms :" + chatRoomService.getAllChatRooms());
            return;
        }
        messageToSend = "CRIAR_SALA_OK";
        if (words[1].equals("PUBLICA")) {

            chatRoomService.createPublicChatRoom(words[2], connectionHandler.getClientUsername());
            // System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());
            
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);

        } else {
            chatRoomService.createPrivateChatRoom(words[2], words[3], connectionHandler.getClientUsername());
            // System.out.println("Chat Room Created :" + chatRoomService.getAllChatRooms());
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
        }
    }

    public void listAllChatRooms(String[] words, ConnectionHandler connectionHandler, AuthenticationService authHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register please.";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }
        if(chatRoomService.getAllChatRooms().isEmpty()){
            System.out.println("No chat rooms");
            messageToSend = "ERRO No chat rooms";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), bufWriter);
        }

        for (ChatRoom chatRoom : chatRoomService.getAllChatRooms()) {
            messageToSend = "NOME: " + chatRoom.getName() +  " Privada:" + chatRoom.isPrivate(); // arrumar print
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
        }
        
    }

    public void removeClientFromAllRooms(ConnectionHandler connectionHandler) throws IOException{
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register please.";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        for (ChatRoom chatRoom : chatRoomService.getAllChatRooms()) {
            int index = chatRoomService.getChatRoomIndexByName(chatRoom.getName());
            if(!chatRoomService.checkChatRoomsEmpty()){
                if (chatRoomService.checkUserInChatRoom(connectionHandler.getClientUsername(), index)) {
                    chatRoom.removeParticipant(connectionHandler.getClientUsername());
                    messageToSend = "SAIU " + chatRoom.getName() + " " + connectionHandler.getClientUsername();
            
                    Set<String> chat_participants;
                    chat_participants = chatRoomService.showParticipants(index);
            
                    for(String participant : chat_participants){
                        serverUtils.broadcastJoinChat(messageToSend, participant, chatRoom.getName(), connectionHandler.getClientUsername()); 
                    }
                }
            }
            if(chatRoomService.checkAdminInChatRoom(connectionHandler.getClientUsername(), index)) {
                Set<String> chat_participants;
                chat_participants = chatRoomService.showParticipants(index);
                messageToSend = "SALA_FECHADA " + chatRoom.getName();
                for(String participant : chat_participants){
                    serverUtils.broadcastJoinChat(messageToSend, participant, chatRoom.getName(), connectionHandler.getClientUsername()); 
                }
                chatRoomService.closeChatRoom(index);
                
            }
        }

    }

    public void joinChatRoom(String[] words, ConnectionHandler connectionHandler, AuthenticationService authHandler) throws IOException {
        String messageToSend;
        
        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register please.";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }


        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "ERRO Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        if(chatRoomService.isUserBanned(connectionHandler.getClientUsername(), index)) {
            System.out.println("User banned");
            messageToSend = "ERRO User banned";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), bufWriter);

            return;
        }

        if (chatRoomService.checkUserInChatRoom(connectionHandler.getClientUsername(), index)) {
            System.out.println("Client already in this Room");
            messageToSend = "ERRO You are already in this room";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
            return;
        }
        // System.out.println("Teste1:" + !chatRoomService.comparePassword(index,
        // words[2]));
        if (chatRoomService.requiresPassword(index)
                && !chatRoomService.comparePassword(index, words[2])) {
            System.out.println("Wrong password");
            messageToSend = "ERRO Wrong password";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);

            return;
        }
        chatRoomService.joinChatRoom(index, connectionHandler.getClientUsername());
        // System.out.println("Chat Room Joined: " + chatRoomService.showParticipants(index));
        messageToSend = "ENTRAR_SALA_OK " + words[1] + " " + chatRoomService.showParticipants(index);
        serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
        messageToSend = "ENTROU " + words[1] + " " + connectionHandler.getClientUsername();
        
        Set<String> chat_participants;
        chat_participants = chatRoomService.showParticipants(index);

        for(String participant : chat_participants){
            serverUtils.broadcastJoinChat(messageToSend, participant, words[1], connectionHandler.getClientUsername()); 
        }
    }

    public void leaveChatRoom(String[] words, ConnectionHandler connectionHandler, AuthenticationService authHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register please.";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "ERRO Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);

            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        if (!chatRoomService.checkUserInChatRoom(connectionHandler.getClientUsername(), index)) {
            System.out.println("Client is not a member in this Room");
            messageToSend = "ERRO You're not a member in this Room";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
            return;
        }

        chatRoomService.leaveChatRoom(index, connectionHandler.getClientUsername());

        System.out.println("Chat Room Participants: " + chatRoomService.showParticipants(index));
        messageToSend = "SAIR_SALA_OK " + words[1];
        serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
        messageToSend = "SAIU " + words[1] + " " + connectionHandler.getClientUsername();
        
        Set<String> chat_participants;
        chat_participants = chatRoomService.showParticipants(index);

        for(String participant : chat_participants){
            serverUtils.broadcastJoinChat(messageToSend, participant, words[1], connectionHandler.getClientUsername()); 
        }
    }

    public void deleteChatRoom(String[] words, ConnectionHandler connectionHandler, AuthenticationService authHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register please.";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "ERRO Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);

            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        // System.out.println("Client Username: " + connectionHandler.getClientUsername());

        if (!chatRoomService.checkAdminInChatRoom(connectionHandler.getClientUsername(), index)) {
            System.out.println("Client is not an administrator in this Room");
            messageToSend = "ERRO You're not an administrator in this Room";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
            return;
        }
        messageToSend = "FECHAR_SALA_OK " + words[1];
        serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
        messageToSend = "SALA_FECHADA " + words[1];
        
        Set<String> chat_participants;
        chat_participants = chatRoomService.showParticipants(index);

        for(String participant : chat_participants){

            serverUtils.broadcastJoinChat(messageToSend, participant, words[1], connectionHandler.getClientUsername()); 
        }
        chatRoomService.closeChatRoom(index);
    }

    public void banUser(String[] words, ConnectionHandler connectionHandler, AuthenticationService authHandler) throws IOException {
        String messageToSend;

        if (!serverUtils.userExists(connectionHandler.getClientUsername())) {
            System.out.println("User does not exists");
            messageToSend = "ERRO User does not exists. Register please.";
            BufferedWriter bufWriter = new BufferedWriter(
            new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);

            return;
        }

        if (!chatRoomService.chatRoomNameExists(words[1])) {
            System.out.println("Chat Room does not exist or tipped wrong name");
            messageToSend = "ERRO Chat Room does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);

            return;
        }

        int index = chatRoomService.getChatRoomIndexByName(words[1]);

        if (!chatRoomService.checkAdminInChatRoom(connectionHandler.getClientUsername(), index)) {
            System.out.println("Client is not an administrator in this Room");
            messageToSend = "ERRO You're not an administrator in this Room";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);

            return;
        }

        if (!chatRoomService.checkUserInChatRoom(words[2], index)) {
            System.out.println("User does not exist or tipped wrong name");
            messageToSend = "ERRO User does not exist or tipped wrong name";
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);

            return;
        }
        
        BufferedWriter auxBuffer = null;
        AuthenticationService auxHandler = null;
        chatRoomService.banUser(index, words[2]);
        // HashMap<ConnectionHandler, String> connHandlers = connectionHandler.getConnHandlers();
        for (ConnectionHandler handler : connectionHandler.getConnHandlers().keySet()) {
            if (handler.getClientUsername().equals(words[2])) {
                auxBuffer = handler.getBufferedWriter();
                auxHandler = handler.gAuthenticationService(); 
            }
        }
        //messageToSend = "Teste antes do Banido";        
        //serverUtils.sendMessageToUniqueClient(auxHandler.encryptMessage(messageToSend), auxBuffer);
        // System.out.println("AuxHandler " + auxHandler);
        messageToSend = "BANIDO_DA_SALA " + words[1];
        serverUtils.sendMessageToUniqueClient(auxHandler.encryptMessage(messageToSend), auxBuffer);
        // System.out.println("AuthHandler " + authHandler);
        //System.out.println("messageToSend: " + messageToSend);
        //System.out.println("Auxbuffer" + auxBuffer);
       

        messageToSend = "BANIMENTO_OK " + words[2];
        serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), this.bufferedWriter);
        //System.out.println("this.bufferedWriter: " + this.bufferedWriter);

        messageToSend = "SAIU " + words[1] + " " + words[2];
        
        Set<String> chat_participants;
        chat_participants = chatRoomService.showParticipants(index);
        //System.out.println("messageToSend: " + messageToSend);
        for(String participant : chat_participants){
            serverUtils.broadcastBanUser(messageToSend, participant, words[1], connectionHandler.getClientUsername(), words[2]); 
        }
    }
}
