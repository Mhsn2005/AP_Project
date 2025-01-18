
package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanvasState implements Serializable {
    private final List<ShapeData> sharedCanvas; // بوم مشترک
    private final Map<String, List<ShapeData>> clientCanvases; // بوم‌های جداگانه برای هر کلاینت

    public CanvasState() {
        this.sharedCanvas = new ArrayList<>();
        this.clientCanvases = new HashMap<>();
    }

    // افزودن شکل به بوم مشترک
    public synchronized void addToSharedCanvas(ShapeData shape) {
        sharedCanvas.add(shape);
    }

    // گرفتن داده‌های بوم مشترک
    public synchronized List<ShapeData> getSharedCanvas() {
        return new ArrayList<>(sharedCanvas);
    }

    // افزودن شکل به بوم کلاینت خاص
    public synchronized void addToClientCanvas(String clientName, ShapeData shape) {
        clientCanvases.computeIfAbsent(clientName, k -> new ArrayList<>()).add(shape);
    }

    // گرفتن داده‌های بوم کلاینت خاص
    public synchronized List<ShapeData> getClientCanvas(String clientName) {
        return clientCanvases.getOrDefault(clientName, new ArrayList<>());
    }

    // گرفتن لیست کلاینت‌ها
    public synchronized List<String> getClientList() {
        return new ArrayList<>(clientCanvases.keySet());
    }
}
