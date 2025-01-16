package org.example;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

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

    private GraphicsContext gc;
    private double startX, startY;
    private boolean isEraser = false;

    @FXML
    public void initialize() {
        gc = drawingCanvas.getGraphicsContext2D();
        setupCanvas();

        clearButton.setOnAction(e -> clearCanvas());
        thicknessSlider.valueProperty().addListener((obs, oldVal, newVal) -> setThickness(newVal.doubleValue()));

        colorPicker.getItems().addAll("Black", "Red", "Blue", "Green", "Yellow", "White");
        colorPicker.setValue("Black"); // مقدار پیش‌فرض
        colorPicker.setOnAction(e -> setColor());

        shapePicker.getItems().addAll("Free Draw", "Line", "Rectangle", "Circle", "Eraser");
        shapePicker.setValue("Free Draw"); // مقدار پیش‌فرض
        shapePicker.setOnAction(e -> toggleShapeMode());
    }

    private void setupCanvas() {
        // تنظیم رنگ پس‌زمینه بوم
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
        if (shape == null) return; // اگر مقدار انتخاب نشده باشد، کاری انجام نده

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
                // استفاده از رنگ پیش‌زمینه برای پاک کردن
                gc.clearRect(endX - 10, endY - 10, 20, 20);
                break;
        }
    }

    private void setColor() {
        String selectedColor = colorPicker.getValue();
        if (selectedColor == null) return; // اگر مقداری انتخاب نشده باشد، خروج از متد

        Color color;
        switch (selectedColor) {
            case "Red": color = Color.RED; break;
            case "Blue": color = Color.BLUE; break;
            case "Green": color = Color.GREEN; break;
            case "Yellow": color = Color.YELLOW; break;
            case "White": color = Color.WHITE; break;
            default: color = Color.BLACK; break;
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
        if (shape == null) return; // اگر مقدار انتخاب نشده باشد، خروج از متد

        isEraser = shape.equals("Eraser");
        if (isEraser) {
            // تنظیم رنگ پاک‌کن به رنگ پس‌زمینه
            gc.setStroke(Color.web("#ecf0f1"));
        } else {
            setColor();
        }
    }

    private void clearCanvas() {
        // پاک کردن کل بوم با رنگ پیش‌زمینه
        gc.setFill(Color.web("#ecf0f1"));
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
    }
}
