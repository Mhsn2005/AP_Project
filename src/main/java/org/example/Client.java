package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client extends Application {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    private static Stage primaryStage;
    private static PrintWriter out;
    private static BufferedReader in;

    private static String clientName;
    private static final Map<String, String> clientCanvases = new HashMap<>();
    private static List<String> connectedClients = new ArrayList<>();
    private static String currentClient = "Shared";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Collaborative Drawing App");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connectToServer(String name) {
        clientName = name;
        new Thread(() -> {
            try (Socket socket = new Socket(HOST, PORT)) {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // ارسال نام کلاینت به سرور
                out.println(clientName);

                // پردازش دستورات از سرور
                String command;
                while ((command = in.readLine()) != null) {
                    if (command.startsWith("clients")) {
                        updateClientList(command);
                    } else if (command.startsWith("draw")) {
                        updateSharedCanvas(command);
                    } else if (command.startsWith("canvasData")) {
                        updateClientCanvas(command);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void updateClientList(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) return;

        Platform.runLater(() -> {
            connectedClients = Arrays.asList(parts[1].split(","));
            MainPageController.updateClientList(connectedClients);
        });
    }

    private static void updateSharedCanvas(String command) {
        Platform.runLater(() -> MainPageController.updateCanvas(command));
    }

    private static void updateClientCanvas(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) return;

        String client = parts[0];
        String canvasData = parts[1];

        clientCanvases.put(client, canvasData);

        if (MainPageController.getCurrentClient().equals(client)) {
            Platform.runLater(() -> MainPageController.loadCanvasData(canvasData));
        }
    }
    public static String sendCommand(String command) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(command);

            String temp = in.readLine();
            System.out.println(temp);
            return temp;

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Unable to connect to server.";
        }
    }



    public static void showMainPage() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(Client.class.getResource("mainPage.fxml"));
                Scene scene = new Scene(loader.load());
                primaryStage.setScene(scene);
                primaryStage.setTitle("Collaborative Drawing App - Main Page");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static String getCurrentClient() {
        return currentClient;
    }

    public static void setCurrentClient(String client) {
        currentClient = client;
    }
}
