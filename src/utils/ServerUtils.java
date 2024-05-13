package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import service.ConnectionHandler;

public class ServerUtils {
    private BufferedWriter bufferedWriter;

    public ServerUtils(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;

    }

    public void sendMessageToUniqueClient(String message, BufferedWriter bufWriter) throws IOException {
        bufWriter.write(message);
        bufWriter.newLine();
        bufWriter.flush();
    }

    public boolean userExists(String username) {
        for (ConnectionHandler handler : ConnectionHandler.connHandlers.keySet()) {
            if (handler.getClientUsername().equals(username) && username != null) {
                return true; // Encontrou o usuário
            }
        }
        return false; // Usuário não encontrado
    }

    public boolean userAlreadyRegistered(Socket clientSocket) throws IOException {
        for (ConnectionHandler clientHandler : ConnectionHandler.connHandlers.keySet()) {
            if (clientHandler.getClientSocket().getRemoteSocketAddress()
                    .equals(clientSocket.getRemoteSocketAddress())) {
                return true;
            }
        }
        return false;
    }

}
