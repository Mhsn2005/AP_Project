package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;

public class Client extends Application {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static GraphicsContext gc;
    private static Stage primaryStage;

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
    public static void initializeCanvas(GraphicsContext graphicsContext) {
        gc = graphicsContext;
        new Thread(Client::connectToServer).start();
    }

    private static void connectToServer() {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Listen for incoming draw commands
            String command;
            while ((command = in.readLine()) != null) {
                if (command.startsWith("draw")) {
                    applyDrawCommand(command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String sendCommand(String command) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(command);
            return in.readLine();
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

    private static void applyDrawCommand(String command) {
        String[] parts = command.split(" ");
        if (parts.length < 5) return;

        Platform.runLater(() -> {
            gc.setStroke(Color.web(parts[1]));
            gc.setLineWidth(Double.parseDouble(parts[2]));
            double startX = Double.parseDouble(parts[3]);
            double startY = Double.parseDouble(parts[4]);
            double endX = Double.parseDouble(parts[5]);
            double endY = Double.parseDouble(parts[6]);
            gc.strokeLine(startX, startY, endX, endY);
        });
    }
}