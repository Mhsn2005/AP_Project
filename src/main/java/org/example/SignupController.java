package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class SignupController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button signupButton;
    @FXML
    private Hyperlink loginLink;

    @FXML
    private void initialize() {
        signupButton.setOnAction(e -> handleSignup());
        loginLink.setOnAction(e -> switchToLogin());
    }

    private void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
        } else if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
        } else {
            // Send signup request to the server
            String response = Client.sendCommand("register " + username + " " + password);
            if (response.contains("successful")) {
                showAlert(Alert.AlertType.INFORMATION, "Signup", "Registration successful. Please login.");
                switchToLogin(); // Switch to login page after successful signup
            } else {
                showAlert(Alert.AlertType.ERROR, "Signup Failed", response);
            }
        }
    }

    private void switchToLogin() {
        switchScene("login.fxml");
    }

    private void switchScene(String fxmlFile) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxmlFile))));
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
