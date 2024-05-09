import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;
    private boolean running;

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            this.running = true;
        } catch (IOException e) {
            System.err.println("Error: Failed to start the server on port " + port);
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Server started. Port : " + this.serverSocket.getLocalPort() + "\nWaiting for clients...");

        while (running) {
            try {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Handle client communication in a separate thread
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();

            } catch (IOException e) {
                System.err.println("Error: Failed to accept client connection");
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
            System.out.println("Server stopped.");
        } catch (IOException e) {
            System.err.println("Error: Failed to stop the server");
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {

        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
                // Example: Read and print an object received from the client
                Object receivedObject = in.readObject();
                System.out.println("Received from client: " + receivedObject);

                // Example: Send a response back to the client
                out.writeObject("Hello from the server!");

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error handling client connection");
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error: Failed to close client socket");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 8080; // Define the port on which the server will listen

        Server server = new Server(port);
        server.start();
    }
}
