package model;

import java.util.HashSet;
import java.util.Set;

public class User {

    private String username;
    private String passwordHash; // Hash da senha (para fins de autenticação)
    private Set<String> connectedChatRooms; // Conjunto de nomes de chatrooms conectados

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.connectedChatRooms = new HashSet<>(); // Inicializa o conjunto vazio
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<String> getConnectedChatRooms() {
        return connectedChatRooms;
    }

    public void connectToChatRoom(String chatRoomName) {
        connectedChatRooms.add(chatRoomName);
    }

    public void disconnectFromChatRoom(String chatRoomName) {
        connectedChatRooms.remove(chatRoomName);
    }

    public boolean isConnectedToChatRoom(String chatRoomName) {
        return connectedChatRooms.contains(chatRoomName);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", connectedChatRooms=" + connectedChatRooms +
                '}';
    }
}
