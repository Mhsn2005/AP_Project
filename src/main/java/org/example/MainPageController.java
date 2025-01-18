package org.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPageController {

    @FXML
    private Canvas drawingCanvas;

    @FXML
    private Button clearButton;

    @FXML
    private Slider thicknessSlider;

    @FXML
    private ComboBox<String> colorPicker;

    @FXML
    private ComboBox<String> shapePicker;

    @FXML
    private ListView<String> clientList;

    private static GraphicsContext gc;
    private double startX, startY;
    private boolean isEraser = false;

    private final Map<String, String> clientCanvases = new HashMap<>(); // نقشه‌ای برای ذخیره بوم کلاینت‌ها
    private String currentClient = "Shared"; // بوم فعلی

    @FXML
    public void initialize() {
        gc = drawingCanvas.getGraphicsContext2D();
        setupCanvas();

        clearButton.setOnAction(e -> clearCanvas());
        thicknessSlider.valueProperty().addListener((obs, oldVal, newVal) -> setThickness(newVal.doubleValue()));

        colorPicker.getItems().addAll("Black", "Red", "Blue", "Green", "Yellow", "White");
        colorPicker.setValue("Black");
        colorPicker.setOnAction(e -> setColor());

        shapePicker.getItems().addAll("Free Draw", "Line", "Rectangle", "Circle", "Eraser");
        shapePicker.setValue("Free Draw");
        shapePicker.setOnAction(e -> toggleShapeMode());

        clientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> switchToClientCanvas(newVal));
    }

    private void setupCanvas() {
        gc.setFill(Color.web("#ecf0f1"));
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        drawingCanvas.setOnMousePressed(this::startDrawing);
        drawingCanvas.setOnMouseDragged(this::draw);
        drawingCanvas.setOnMouseReleased(this::finishDrawing);
    }

    private void startDrawing(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();

        String shape = shapePicker.getValue();
        if (shape == null || shape.equals("Free Draw") || shape.equals("Eraser")) {
            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.stroke();
        }
    }

    private void draw(MouseEvent e) {
        String shape = shapePicker.getValue();
        if (shape == null || shape.equals("Free Draw") || shape.equals("Eraser")) {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        }
    }

    private void finishDrawing(MouseEvent e) {
        double endX = e.getX();
        double endY = e.getY();

        String shape = shapePicker.getValue();
        if (shape == null) return;

        switch (shape) {
            case "Line":
                gc.strokeLine(startX, startY, endX, endY);
                break;
            case "Rectangle":
                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), width, height);
                break;
            case "Circle":
                double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                break;
            case "Eraser":
                gc.clearRect(endX - 10, endY - 10, 20, 20);
                break;
        }

        sendShapeToServer(shape, startX, startY, endX, endY, colorPicker.getValue());
    }

    private void sendShapeToServer(String shapeType, double startX, double startY, double endX, double endY, String color) {
        String command = "draw " + shapeType + " " + startX + " " + startY + " " + endX + " " + endY + " " + color + " " + currentClient;
        Client.sendCommand(command);
    }

    private void setColor() {
        String selectedColor = colorPicker.getValue();
        if (selectedColor == null) return;

        Color color;
        switch (selectedColor) {
            case "Red":
                color = Color.RED;
                break;
            case "Blue":
                color = Color.BLUE;
                break;
            case "Green":
                color = Color.GREEN;
                break;
            case "Yellow":
                color = Color.YELLOW;
                break;
            case "White":
                color = Color.WHITE;
                break;
            default:
                color = Color.BLACK;
                break;
        }

        if (!isEraser) {
            gc.setStroke(color);
            gc.setFill(color);
        }
    }

    private void setThickness(double thickness) {
        gc.setLineWidth(thickness);
    }

    private void toggleShapeMode() {
        String shape = shapePicker.getValue();
        if (shape == null) return;

        isEraser = shape.equals("Eraser");
        gc.setStroke(isEraser ? Color.web("#ecf0f1") : Color.web(colorPicker.getValue()));
    }

    private void clearCanvas() {
        gc.setFill(Color.web("#ecf0f1"));
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
    }

    private void switchToClientCanvas(String clientName) {
        if (clientName == null || clientName.equals(currentClient)) return;

        currentClient = clientName;
        Client.setCurrentClient(clientName);

        System.out.println("Switching to client canvas: " + clientName); // لاگ برای بررسی

        gc.setFill(Color.web("#ecf0f1"));
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        String clientCanvasData = clientCanvases.getOrDefault(clientName, "");
        loadCanvasData(clientCanvasData);
    }

    static void loadCanvasData(String canvasData) {
        if (canvasData != null && !canvasData.isEmpty()) {
            String[] actions = canvasData.split(";");
            for (String action : actions) {
                String[] parts = action.split(" ");
                String shape = parts[0];
                double startX = Double.parseDouble(parts[1]);
                double startY = Double.parseDouble(parts[2]);
                double endX = Double.parseDouble(parts[3]);
                double endY = Double.parseDouble(parts[4]);
                String color = parts[5];

                gc.setStroke(Color.web(color));
                switch (shape) {
                    case "Line":
                        gc.strokeLine(startX, startY, endX, endY);
                        break;
                    case "Rectangle":
                        gc.strokeRect(startX, startY, endX - startX, endY - startY);
                        break;
                    case "Circle":
                        double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                        gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                        break;
                }
            }
        }
    }

    public static void updateCanvas(String action) {
        Platform.runLater(() -> {
            String[] parts = action.split(" ");
            if (parts.length < 7) return;

            String shape = parts[1];
            double startX = Double.parseDouble(parts[2]);
            double startY = Double.parseDouble(parts[3]);
            double endX = Double.parseDouble(parts[4]);
            double endY = Double.parseDouble(parts[5]);
            String color = parts[6];

            gc.setStroke(Color.web(color));
            switch (shape) {
                case "Line":
                    gc.strokeLine(startX, startY, endX, endY);
                    break;
                case "Rectangle":
                    gc.strokeRect(startX, startY, endX - startX, endY - startY);
                    break;
                case "Circle":
                    double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                    gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                    break;
            }
        });
    }

    private static MainPageController instance;

    public MainPageController() {
        instance = this;
    }

    public static MainPageController getInstance() {
        return instance;
    }


    public static void updateClientList(List<String> clients) {
        if (getInstance() != null) {
            Platform.runLater(() -> {
                getInstance().clientList.getItems().clear();
                getInstance().clientList.getItems().addAll(clients);
            });
        }
    }

    public static String getCurrentClient() {
        return Client.getCurrentClient();
    }

    }