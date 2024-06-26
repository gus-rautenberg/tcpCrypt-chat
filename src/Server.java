import java.io.IOError;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import service.ConnectionHandler;

public class Server {
    private ServerSocket serverSocket;

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server started. Listening on port: " + port);
        } catch (Exception e) {
            System.err.println("Error: Failed to start the server on port " + port);
            e.printStackTrace();
        }
    }

    public void start() {
        try {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket);
                Thread threadConnectionHandler = new Thread(connectionHandler);
                threadConnectionHandler.start();
            }
        } catch (IOException e) {

            System.err.println("Error: " + e);
        }
    }

    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

        } catch (IOException igException) {
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8080);
        server.start();
    }

}