package com.example.batallanaval.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class CanvasMarkerRenderer {

    private final double cellSize;

    public CanvasMarkerRenderer(double cellSize) {
        this.cellSize = cellSize;
    }

    public Canvas drawMiss() {
        Canvas canvas = new Canvas(cellSize, cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Estilo: X Roja (Agua)
        gc.setStroke(Color.RED);
        gc.setLineWidth(4);

        // Dibujamos la X con un pequeño margen (padding)
        double p = cellSize * 0.25; // padding
        gc.strokeLine(p, p, cellSize - p, cellSize - p);
        gc.strokeLine(cellSize - p, p, p, cellSize - p);

        return canvas;
    }

    public Canvas drawHit() {
        Canvas canvas = new Canvas(cellSize, cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double cx = cellSize / 2;
        double cy = cellSize / 2 + 5;
        double radius = cellSize * 0.3;

        // 1. Cuerpo de la Bomba (Negro)
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // 2. Brillo de la bomba (Blanco)
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - radius + 5, cy - radius + 5, 6, 6);

        // 3. Mecha (Línea curva)
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.beginPath();
        gc.moveTo(cx, cy - radius); // Parte superior bomba
        gc.quadraticCurveTo(cx + 10, cy - radius - 10, cx + 15, cy - radius - 5);
        gc.stroke();

        // 4. Chispita (Rojo/Amarillo)
        gc.setFill(Color.ORANGE);
        gc.fillOval(cx + 13, cy - radius - 8, 6, 6);

        return canvas;
    }

    public Canvas drawSunk() {
        Canvas canvas = new Canvas(cellSize, cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double w = cellSize;
        double h = cellSize;

        // Dibujamos una llama estilizada

        // 1. Fuego Exterior (Rojo)
        gc.setFill(Color.ORANGERED);
        gc.fillPolygon(
                new double[]{w*0.2, w*0.8, w*0.5}, // Puntos X
                new double[]{h*0.8, h*0.8, h*0.1}, // Puntos Y
                3 // Número de puntos
        );
        gc.fillOval(w*0.2, h*0.6, w*0.6, h*0.3); // Base redonda

        // 2. Fuego Interior (Amarillo)
        gc.setFill(Color.YELLOW);
        gc.fillPolygon(
                new double[]{w*0.35, w*0.65, w*0.5},
                new double[]{h*0.8, h*0.8, h*0.3},
                3
        );
        gc.fillOval(w*0.35, h*0.7, w*0.3, h*0.2);

        return canvas;
    }
}