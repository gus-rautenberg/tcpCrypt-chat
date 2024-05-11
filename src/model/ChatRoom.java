package model;

import java.util.HashSet;
import java.util.Set;

public class ChatRoom {

    private String name;
    private boolean isPrivate;
    private String passwordHash; 
    private Set<String> participants;
    private String adminPort; 

    // Construtor para sala pública (sem senha)
    public ChatRoom(String name, String adminPort) {
        this.name = name;
        this.isPrivate = false; 
        this.passwordHash = null; 
        this.participants = new HashSet<>();
        this.adminPort = adminPort;
        participants.add(adminPort);
    }

    // Construtor para sala privada (com senha)
    public ChatRoom(String name, String passwordHash, String adminPort) {
        this.name = name;
        this.isPrivate = true; 
        this.passwordHash = passwordHash;
        this.participants = new HashSet<>();
        this.adminPort = adminPort;
        participants.add(adminPort);
    }

    public String getName() {
        return name;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean requiresPassword() {
        return isPrivate;
    }

    // public boolean checkPassword(String inputPassword) {
    //     return isPrivate && passwordHash != null && passwordHash.equals(hashPassword(inputPassword));
    // }

    public Set<String> getParticipants() {
        return participants;
    }

    public void addParticipant(String username) {
        participants.add(username);
    }

    public void removeParticipant(String username) {
        participants.remove(username);
    }
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "name='" + name + '\'' +
                ", isPrivate=" + isPrivate +
                ", participants=" + participants +
                '}';
    }

}
