package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.ChatRoom;

public class ChatRoomService {
    private static ChatRoomService instance;
    private List<ChatRoom> chatRooms;

    private ChatRoomService() {
        chatRooms = new ArrayList<>();
    }

    public static ChatRoomService getInstance() {
        if (instance == null) {
            instance = new ChatRoomService();
        }
        return instance;
    }

    public synchronized List<ChatRoom> getAllChatRooms() {
        return new ArrayList<>(chatRooms);
    }

    public boolean checkChatRoomsEmpty() {
        return chatRooms.isEmpty();
    }

    public ChatRoom geChatRoomByIndex(int index) {
        return chatRooms.get(index);
    }

    public void createPublicChatRoom(String roomName, String admin) {
        // Lógica para criar uma sala de chat pública
        chatRooms.add(new ChatRoom(roomName, admin));
    }

    public void createPrivateChatRoom(String roomName, String password, String admin) {
        // Lógica para criar uma sala de chat privada
        chatRooms.add(new ChatRoom(roomName, password, admin));
    }

    public boolean chatRoomNameExists(String roomName) {
        for (ChatRoom chatRoom : chatRooms) {
            if (chatRoom.getName().equals(roomName)) {
                return true;
            }
        }
        return false;
    }

    public ChatRoom getChatRoomByName(String roomName) {
        // chatRooms.contains(oomName);
        for (ChatRoom chatRoom : chatRooms) {
            if (chatRoom.getName().equals(roomName)) {
                return chatRoom;
            }
        }
        return null;
    }

    public int getListSize() {
        return chatRooms.size();
    }

    public int getChatRoomIndexByName(String roomName) {
        for (int i = 0; i < chatRooms.size(); i++) {
            if (chatRooms.get(i).getName().equals(roomName)) {
                return i;
            }
        }
        return -1;
    }

    public boolean requiresPassword(int index) {
        System.out.println("Precisa de senha: " + chatRooms.get(index).requiresPassword());
        return chatRooms.get(index).requiresPassword();
    }

    public boolean comparePassword(int index, String password) {
        String passString = chatRooms.get(index).getPasswordHash();
        System.out.println("Senha:" + chatRooms.get(index).getPasswordHash() + "Senha passada: " + password);
        if (passString.equals(password)) {
            return true;
        }
        return false;
    }

    public boolean isUserBanned(String username, int index) {
        Set<String> bannedUsers = chatRooms.get(index).getBannedUsers();
        if (bannedUsers == null)
            return false;
        for (String bannedUser : bannedUsers) {
            if (bannedUser.equals(username)) {
                return true;
            }
        }
        return false;

    }

    public void banUser(int index, String username) {
        chatRooms.get(index).removeParticipant(username);
        chatRooms.get(index).addBannedUser(username);
    }
    // public boolean chatRoomNameUnique(String roomName){

    // for (ChatRoom chatRoom : chatRooms) {
    // if (chatRoom.getName().equals(roomName)) {
    // return false;
    // }
    // }
    // return true;
    // }
    // Outros métodos para gerenciar as salas de chat
    public void joinChatRoom(int index, String userPort) {
        chatRooms.get(index).addParticipant(userPort);
    }

    public void leaveChatRoom(int index, String userPort) {
        chatRooms.get(index).removeParticipant(userPort);
    }

    public boolean checkUserInChatRoom(String userPort, int index) {
        return chatRooms.get(index).getParticipants().contains(userPort);
    }

    public boolean checkAdminInChatRoom(String username, int index) {
        System.out.println("admin:" + chatRooms.get(index).getAdmin() + " username: " + username);
        return chatRooms.get(index).getAdmin().equals(username);
    }

    public Set<String> showParticipants(int index) {
        return chatRooms.get(index).getParticipants();
    }

    public enum RoomType {
        PUBLIC,
        PRIVATE
    }

    public void closeChatRoom(int index) {
        chatRooms.get(index).destroy();
        chatRooms.remove(index);
    }

}
