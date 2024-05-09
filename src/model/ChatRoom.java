package model;
import java.util.HashSet;
import java.util.Set;

public class ChatRoom {

    private String name;
    private boolean isPrivate;
    private String passwordHash; // Hash da senha para salas privadas
    private Set<String> participants; // Conjunto de nomes de usuários participantes

    public ChatRoom(String name, boolean isPrivate, String passwordHash) {
        this.name = name;
        this.isPrivate = isPrivate;
        this.passwordHash = passwordHash;
        this.participants = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void addParticipant(String username) {
        participants.add(username);
    }

    public void removeParticipant(String username) {
        participants.remove(username);
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
