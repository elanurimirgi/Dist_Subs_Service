package servers;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Server2 {
    private static AtomicInteger capacity = new AtomicInteger(100);
    private static boolean isSubscribed = false;
    private static boolean isLoggedIn = false;

    public static void main(String[] args) {
        connectToServers("localhost", 5001);
        connectToServers("localhost", 5003);

        // Start a thread to modify capacity randomly every 5 seconds
        new Thread(() -> {
            Random random = new Random();
            while (true) {
                try {
                    Thread.sleep(5000);  // Wait for 5 seconds
                    int change = random.nextInt(21) - 10; // Random change between -10 and 10
                    capacity.addAndGet(change);
                    if (capacity.get() < 0) {
                        capacity.set(0); // Ensure capacity does not go below 0
                    }
                    System.out.println("Capacity updated to: " + capacity.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(5002)) {
            System.out.println("Server2 running on port 5002...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void connectToServers(String host, int port) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Connected to server on port " + port);
            out.println("HELLO_SERVER1");
        } catch (IOException e) {
            System.out.println("Failed to connect to server on port " + port);
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String message = in.readLine();
                System.out.println("Received: " + message);

                if (message.startsWith("CAPACITY_INFO")) {
                    // Handle capacity info request
                    long timestamp = System.currentTimeMillis() / 1000;  // UNIX epoch time
                    out.println(String.format("server_id: 1, server_status: %d, timestamp: %d",
                                               capacity.get(), timestamp));
                } else if (message.startsWith("SUBSCRIBE")) {
                    // Handle subscribe request
                    if (!isSubscribed) {
                        isSubscribed = true;
                        capacity.decrementAndGet();
                        out.println("SUBSCRIBED. Current Capacity: " + capacity.get());
                    } else {
                        out.println("ALREADY_SUBSCRIBED");
                    }
                } else if (message.startsWith("UNSUBSCRIBE")) {
                    // Handle unsubscribe request
                    if (isSubscribed) {
                        isSubscribed = false;
                        capacity.incrementAndGet();
                        out.println("UNSUBSCRIBED. Current Capacity: " + capacity.get());
                    } else {
                        out.println("NOT_SUBSCRIBED");
                    }
                } else if (message.startsWith("LOGIN")) {
                    // Handle login request
                    if (isSubscribed) {
                        isLoggedIn = true;
                        out.println("LOGGED_IN. Welcome back, you are already subscribed.");
                    } else {
                        out.println("SUBSCRIBE_FIRST");
                    }
                } else if (message.startsWith("HELLO_SERVER1")) {
                    // Handle hello message from other servers
                    out.println("Server1 Connection Established");
                } else {
                    // Handle invalid requests
                    out.println("INVALID_REQUEST");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
