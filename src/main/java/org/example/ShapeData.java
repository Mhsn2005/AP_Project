package org.example;

import java.io.Serializable;

public class ShapeData implements Serializable {
    private final String type; // نوع شکل (مانند خط، دایره)
    private final double startX, startY, endX, endY;
    private final String color;

    public ShapeData(String type, double startX, double startY, double endX, double endY, String color) {
        this.type = type;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public String getColor() {
        return color;
    }
}
