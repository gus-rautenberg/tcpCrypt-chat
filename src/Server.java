import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
                System.out.println("Client connected: " + clientSocket);

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8080);
        server.start();
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                output.flush();
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

                String message = "";
                do {
                    message = (String) input.readObject();
                    System.out.println("Client>> " + message);

                    

                } while (!message.equals("SAIR"));

                output.close();
                input.close();
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("Error: " + e);
            }
        }
    }
}