package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final String DB_URL = "jdbc:sqlite:userdb.sqlite";

    // Shared data model for canvas
    private final List<String> canvasData = Collections.synchronizedList(new ArrayList<>());
    private final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

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
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            // Add client writer to broadcast list
            synchronized (clientWriters) {
                clientWriters.add(out);
            }

            // Send current canvas data to the new client
            synchronized (canvasData) {
                for (String action : canvasData) {
                    out.println(action);
                }
            }

            // Handle incoming commands from the client
            String command;
            while ((command = in.readLine()) != null) {
                if (command.startsWith("draw")) {
                    handleDrawCommand(command);
                } else if (command.startsWith("login")) {
                    handleLoginCommand(command, out);
                } else if (command.startsWith("register")) {
                    handleRegisterCommand(command, out);
                } else {
                    out.println("Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            synchronized (clientWriters) {
                clientWriters.removeIf(writer -> writer.checkError());
            }
        }
    }

    private void handleDrawCommand(String command) {
        // Store the draw action in the canvas data
        synchronized (canvasData) {
            canvasData.add(command);
        }

        // Broadcast the draw action to all connected clients
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(command);
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
}
