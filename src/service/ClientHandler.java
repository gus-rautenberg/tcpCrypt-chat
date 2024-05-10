package service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.io.IOException;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    // private BufferedInputStream;
    private String clientUsername;

    public ClientHandler(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat");
        } catch (IOException e) {
            closeEverything(clientSocket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        while((clientSocket.isConnected())) {
            try {
                String messageFromClient;

                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(clientSocket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for(ClientHandler clientHandler : clientHandlers) {
            try {
                if(!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush(); 
                }
            } catch (IOException e) {
                closeEverything(clientSocket, bufferedReader, bufferedWriter);
            }
        }
    }
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat");
    }
    public void closeEverything(Socket clientSocket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    // public void run() {
    //     try {
    //         ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
    //         output.flush();
    //         ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

    //         String message = "";
    //         do {
    //             message = (String) input.readObject();
    //             System.out.println("Client>> " + message);

                

    //         } while (!message.equals("SAIR"));

    //         output.close();
    //         input.close();
    //         clientSocket.close();
    //     } catch (Exception e) {
    //         System.err.println("Error: " + e);
    //     }
    // }