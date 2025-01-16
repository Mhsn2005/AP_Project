package org.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink signupLink;

    @FXML
    private void initialize() {
        loginButton.setOnAction(e -> handleLogin());
        signupLink.setOnAction(e -> switchToSignup());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Username and Password cannot be empty!");
            return;
        }

        new Thread(() -> {
            String response = Client.sendCommand("login " + username + " " + password);
            Platform.runLater(() -> {
                if ("Login successful!".equals(response)) {
                    showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + username + "!");
                    Client.showMainPage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", response);
                }
            });
        }).start();
    }

    private void switchToSignup() {
        switchScene("signup.fxml");
    }

    private void switchScene(String fxmlFile) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxmlFile)));
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
