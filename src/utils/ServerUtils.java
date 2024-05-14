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

    public void broadcastMessageChat(String messageToSend, String participant, String chatRoom, String remetente) {
        for(ConnectionHandler clientHandler : ConnectionHandler.connHandlers.keySet()) {
            try {

                if(!clientHandler.getClientUsername().equals(remetente) && clientHandler.getClientUsername().equals(participant)){
                    String finalMessage = "MENSAGEM " + chatRoom + " " + clientHandler.getClientUsername() + ": " + messageToSend;
                    sendMessageToUniqueClient(finalMessage, clientHandler.getBufferedWriter());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void broadcastJoinChat(String messageToSend, String participant, String chatRoom, String remetente) {
        for(ConnectionHandler clientHandler : ConnectionHandler.connHandlers.keySet()) {
            try {

                if(!clientHandler.getClientUsername().equals(remetente) && clientHandler.getClientUsername().equals(participant)){
                    sendMessageToUniqueClient(messageToSend, clientHandler.getBufferedWriter());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
