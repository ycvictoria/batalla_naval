package com.example.batallanaval.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * Clase encargada de generar los objetos Canvas que representan
 * los marcadores visuales en el tablero de Batalla Naval:
 * Fallo (Miss), Impacto (Hit) y Hundido (Sunk).
 * Cada marcador se dibuja en un Canvas cuyo tamaño es igual a una celda.
 */
public class CanvasMarkerRenderer {
    private final double cellSize;

    /**
     * Constructor para el renderizador.
     * @param cellSize El tamaño en píxeles de una celda del tablero,
     * que determinará el tamaño de los Canvas generados.
     */
    public CanvasMarkerRenderer(double cellSize) {
        this.cellSize = cellSize;
    }

    /**
     * Dibuja y devuelve un Canvas que representa un "Fallo" (Miss) en el agua.
     * El marcador es una 'X' roja.
     * @return Un objeto Canvas con el dibujo de fallo.
     */
    public Canvas drawMiss() {
        Canvas canvas = new Canvas(cellSize, cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Estilo: X Roja (Agua)
        gc.setStroke(Color.RED);
        gc.setLineWidth(4);
        // Dibujamos la X con un pequeño margen (padding)
        double p = cellSize * 0.25; // Define el margen del 25% del tamaño de la celda
        gc.strokeLine(p, p, cellSize - p, cellSize - p);
        gc.strokeLine(cellSize - p, p, p, cellSize - p);

        return canvas;
    }

    /**
     * Dibuja y devuelve un Canvas que representa un "Impacto" (Hit) en un barco.
     * El marcador es un estilo de "bomba" con una mecha.
     * @return Un objeto Canvas con el dibujo de impacto.
     */
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
        // Curva cuadrática para darle forma a la mecha
        gc.quadraticCurveTo(cx + 10, cy - radius - 10, cx + 15, cy - radius - 5);
        gc.stroke();

        // 4. Chispita (Rojo/Amarillo)
        gc.setFill(Color.ORANGE);
        gc.fillOval(cx + 13, cy - radius - 8, 6, 6);

        return canvas;
    }

    /**
     * Dibuja y devuelve un Canvas que representa un barco "Hundido" (Sunk).
     * El marcador es un efecto de "fuego/explosión".
     * @return Un objeto Canvas con el dibujo de barco hundido.
     */
    public Canvas drawSunk() {
        Canvas canvas = new Canvas(cellSize, cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = cellSize;
        double h = cellSize;

        // 1. Fuego Exterior (Rojo)
        gc.setFill(Color.ORANGERED);
        // Triángulo superior de la llama
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