package model;

import java.util.HashSet;
import java.util.Set;


public class ChatRoom {

    private String name;
    private boolean isPrivate;
    private String passwordHash;
    private Set<String> participants;
    private Set<String> bannedUsers;
    private String adminPort;

    // Construtor para sala pública (sem senha)
    public ChatRoom(String name, String adminPort) {
        this.name = name;
        this.isPrivate = false;
        this.passwordHash = null;
        this.participants = new HashSet<>();
        this.bannedUsers = new HashSet<>();
        this.adminPort = adminPort;
        participants.add(adminPort);
    }

    // Construtor para sala privada (com senha)
    public ChatRoom(String name, String passwordHash, String adminPort) {
        this.name = name;
        this.isPrivate = true;
        this.passwordHash = passwordHash;
        this.participants = new HashSet<>();
        this.bannedUsers = new HashSet<>();
        this.adminPort = adminPort;
        participants.add(adminPort);
    }

    public void destroy() {
        // Limpar recursos ou fazer outras tarefas de liberação aqui
        this.name = null;
        this.passwordHash = null;
        this.participants.clear();
        this.adminPort = null;
    }

    public String getName() {
        return name;
    }

    public String getAdmin() {
        return adminPort;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean requiresPassword() {
        return isPrivate;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void addParticipant(String username) {
        participants.add(username);
    }
    public void addBannedUser(String username) {
        bannedUsers.add(username);
    }
    public Set<String> getBannedUsers(){
        return bannedUsers;
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
