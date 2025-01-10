package clients;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;

public class SingleClient {
    private static final String HOST = "localhost"; // Sunucu adresi
    private static final int PORT1 = 5001;         // Server1 portu
    private static final int PORT2 = 5002;         // Server2 portu
    private static final int PORT3 = 5003;         // Server3 portu

    public static void main(String[] args) {
        // ExecutorService kullanarak çoklu iş parçacığı (thread) yönetimi
        ExecutorService executorService = Executors.newFixedThreadPool(3); // 3 sunucu için 3 iş parçacığı

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("Bağlantı başarılı: Sunuculara bağlanıldı.");

            while (true) {
                System.out.println("ABONELİK İŞLEMLERİ:");
                System.out.println("1. Abone Ol");
                System.out.println("2. Login Ol");
                System.out.println("3. Logout Ol");
                System.out.println("4. Çıkış");

                System.out.println("Seçiminizi girin:");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        String subscriptionMessage = "SUBSCRIBE: ID=1; Name=Elanur İmirgi; Status=Subscriber; Interests=teknoloji,spor,müzik";
                        executorService.submit(() -> sendToServer(subscriptionMessage, PORT1));
                        executorService.submit(() -> sendToServer(subscriptionMessage, PORT2));
                        executorService.submit(() -> sendToServer(subscriptionMessage, PORT3));
                        break;
                    case "2":
                        String loginMessage = "LOGIN: ID=1; Status=Online";
                        executorService.submit(() -> sendToServer(loginMessage, PORT1));
                        executorService.submit(() -> sendToServer(loginMessage, PORT2));
                        executorService.submit(() -> sendToServer(loginMessage, PORT3));
                        break;
                    case "3":
                        String logoutMessage = "LOGOUT: ID=1; Status=Offline";
                        executorService.submit(() -> sendToServer(logoutMessage, PORT1));
                        executorService.submit(() -> sendToServer(logoutMessage, PORT2));
                        executorService.submit(() -> sendToServer(logoutMessage, PORT3));
                        break;
                    case "4":
                        System.out.println("Çıkış yapılıyor...");
                        executorService.shutdown();
                        return;
                    default:
                        System.out.println("Geçersiz seçim yaptınız.");
                }
            }

        } catch (Exception ex) {
            System.err.println("Genel bir hata oluştu.");
            ex.printStackTrace();
        }
    }

    private static void sendToServer(String message, int port) {
        try (Socket socket = new Socket(HOST, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(message);
            String response = in.readLine();
            System.out.println("Server " + port + " response: " + response);
        } catch (IOException e) {
            System.out.println("Sunucuya bağlanırken hata oluştu: " + e.getMessage());
        }
    }
}
