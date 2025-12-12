package com.example.batallanaval.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BoardVisualizer {
    private Pane shipsPane;
    private double cellSize;

    // Highlight del Jugador (Verde/Rojo)
    private final Rectangle selectionHighlight = new Rectangle();

    // Highlight del Enemigo (Mira Amarilla/Naranja) - ¡NUEVO!
    private final Rectangle targetHighlight = new Rectangle();

    public BoardVisualizer(Pane shipsPane, double cellSize) {
        this.shipsPane = shipsPane;
        this.cellSize = cellSize;
        initializePlayerHighlight();
    }

    // --- LÓGICA JUGADOR ---
    private void initializePlayerHighlight() {
        selectionHighlight.setVisible(false);
        selectionHighlight.setArcWidth(5);
        selectionHighlight.setArcHeight(5);
        selectionHighlight.setMouseTransparent(true);
        shipsPane.getChildren().add(selectionHighlight);
    }

    public void drawGrid() {
        drawGrid(this.shipsPane); // Dibuja en el panel principal por defecto
    }

    // Método genérico para dibujar líneas en cualquier panel
    public void drawGrid(Pane targetPane) {
        double size = cellSize * 10;
        Canvas grid = new Canvas(size, size);
        GraphicsContext gc = grid.getGraphicsContext2D();

        // Dibujamos líneas blancas semitransparentes
        gc.setStroke(Color.web("#FFFFFF", 0.2));
        gc.setLineWidth(1);

        for (int i = 0; i <= 10; i++) {
            gc.strokeLine(i * cellSize, 0, i * cellSize, size);
            gc.strokeLine(0, i * cellSize, size, i * cellSize);
        }

        // Agregar al fondo del panel objetivo
        if (!targetPane.getChildren().isEmpty())
            targetPane.getChildren().add(0, grid);
        else
            targetPane.getChildren().add(grid);
    }

    // --- LÓGICA ENEMIGO (MIRA Y LÍNEAS) ---
    public void prepareEnemyBoard(Pane enemyLayer) {
        // 1. Configurar la mira (Highlight)
        targetHighlight.setWidth(cellSize);
        targetHighlight.setHeight(cellSize);
        targetHighlight.setFill(Color.rgb(255, 165, 0, 0.3)); // Naranja transparente
        targetHighlight.setStroke(Color.ORANGE);
        targetHighlight.setStrokeWidth(2);
        targetHighlight.setVisible(false);
        targetHighlight.setMouseTransparent(true);

        enemyLayer.getChildren().add(targetHighlight);
    }

    public void updateTargetHighlight(int col, int row) {
        // Validar que esté dentro del tablero (0-9)
        if (col < 0 || row < 0 || col >= 10 || row >= 10) {
            targetHighlight.setVisible(false);
            return;
        }
        targetHighlight.setLayoutX(col * cellSize);
        targetHighlight.setLayoutY(row * cellSize);
        targetHighlight.setVisible(true);
    }

    public void hideTargetHighlight() {
        targetHighlight.setVisible(false);
    }

    public Rectangle getSelectionHighlight() { return selectionHighlight; }
}