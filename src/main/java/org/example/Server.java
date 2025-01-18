package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final String DB_URL = "jdbc:sqlite:userdb.sqlite";

    // داده‌های بوم مشترک
    private final List<String> sharedCanvasData = Collections.synchronizedList(new ArrayList<>());

    // داده‌های بوم کاربران به صورت جداگانه
    private final Map<String, List<String>> individualCanvasData = Collections.synchronizedMap(new HashMap<>());

    // لیست کلاینت‌های متصل و ارتباط آن‌ها
    private final Map<String, PrintWriter> clients = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        String clientName = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String command;
            while ((command = in.readLine()) != null) {
                if (command.startsWith("login")) {
                    handleLoginCommand(command, out);
                } else if (command.startsWith("register")) {
                    handleRegisterCommand(command, out);
                } else if (command.startsWith("draw")) {
                    handleDrawCommand(clientName, command);
                } else if (command.startsWith("requestClientCanvas")) {
                    handleClientCanvasRequest(command, out);
                } else if (command.startsWith("setName")) {
                    clientName = command.split(" ", 2)[1];
                    registerClient(clientName, out);
                } else {
                    out.println("Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (clientName != null) {
                unregisterClient(clientName);
            }
        }
    }

    private void handleLoginCommand(String command, PrintWriter out) {
        String[] parts = command.split(" ", 3);
        if (parts.length < 3) {
            out.println("Invalid login command format");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    out.println("Login successful!");
                } else {
                    out.println("Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("Error during login: " + e.getMessage());
        }
    }

    private void handleRegisterCommand(String command, PrintWriter out) {
        String[] parts = command.split(" ", 3);
        if (parts.length < 3) {
            out.println("Invalid register command format");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "INSERT INTO Users (username, password) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();
                out.println("Registration successful!");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                out.println("Username already exists.");
            } else {
                e.printStackTrace();
                out.println("Error during registration: " + e.getMessage());
            }
        }
    }

    private void handleDrawCommand(String clientName, String command) {
        synchronized (sharedCanvasData) {
            sharedCanvasData.add(command);
        }

        if (clientName != null) {
            synchronized (individualCanvasData) {
                individualCanvasData.computeIfAbsent(clientName, k -> new ArrayList<>()).add(command);
            }
        }
        System.out.println("Broadcasting draw command: " + command);

        synchronized (clients) {
            for (PrintWriter writer : clients.values()) {
                writer.println(command);
            }
        }
    }

    private void handleClientCanvasRequest(String command, PrintWriter out) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            out.println("Invalid request format");
            return;
        }

        String targetClientName = parts[1];
        if (!individualCanvasData.containsKey(targetClientName)) {
            out.println("Invalid client name");
            return;
        }

        List<String> clientCanvas = individualCanvasData.get(targetClientName);
        for (String action : clientCanvas) {
            out.println("canvasData " + action);
        }
    }

    private void registerClient(String clientName, PrintWriter out) {
        synchronized (clients) {
            clients.put(clientName, out);
        }
        synchronized (individualCanvasData) {
            individualCanvasData.putIfAbsent(clientName, new ArrayList<>());
        }
        broadcastClientList();
    }

    private void unregisterClient(String clientName) {
        synchronized (clients) {
            clients.remove(clientName);
        }
        synchronized (individualCanvasData) {
            individualCanvasData.remove(clientName);
        }
        broadcastClientList();
    }

    private void broadcastClientList() {
        String clientList = "clients " + String.join(",", clients.keySet());
        System.out.println("Broadcasting client list: " + clientList);
        synchronized (clients) {
            for (PrintWriter writer : clients.values()) {
                writer.println(clientList);
            }
        }
    }
}
