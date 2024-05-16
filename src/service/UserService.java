package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import utils.ServerUtils;

public class UserService {
    private ServerUtils serverUtils;
    private Socket clientSocket;

    public UserService(BufferedWriter bufferedWriter, Socket clientSocket) {
        this.serverUtils = new ServerUtils(bufferedWriter);
        this.clientSocket = clientSocket;
    }

    public void register(String[] words, ConnectionHandler connectionHandler, AuthenticationService authHandler) throws IOException {
        String messageToSend;
        if (serverUtils.userAlreadyRegistered(clientSocket)) {
            System.out.println("User already registered1");
            messageToSend = "ERRO User already registered.";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(authHandler.encryptMessage(messageToSend), bufWriter);
            return;
        }
        if (serverUtils.userExists(words[1])) {
            System.out.println("User already exists");
            messageToSend = "ERRO User already exists. Enter a new username";
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);
            return;
        }
        

        
        System.out.println("Usuario criado");
        registerUser(words[1], connectionHandler);
        messageToSend = "REGISTRO_OK";
        BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);
    }

    public void registerUser(String username, ConnectionHandler connectionHandler) {
        connectionHandler.setClientUsername(username);
        ConnectionHandler.connHandlers.put(connectionHandler, username);

    }

}
