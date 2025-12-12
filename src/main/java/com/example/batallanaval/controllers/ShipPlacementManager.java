package com.example.batallanaval.controllers;

import com.example.batallanaval.models.Board;
import com.example.batallanaval.models.Ship;
import com.example.batallanaval.views.BoardVisualizer;
import com.example.batallanaval.views.CanvasShipRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ShipPlacementManager {
    private final GameController controller;
    private final BoardVisualizer visualizer;
    private final Pane shipsPane;
    private final double cellSize;
    private boolean isHorizontal = true;
    private final CanvasShipRenderer renderer = new CanvasShipRenderer();

    public ShipPlacementManager(GameController controller, BoardVisualizer visualizer, Pane shipsPane, double cellSize) {
        this.controller = controller;
        this.visualizer = visualizer;
        this.shipsPane = shipsPane;
        this.cellSize = cellSize;
        setupBoardDragHandlers();
    }

    private void setupBoardDragHandlers() {
        shipsPane.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                int size = Integer.parseInt(e.getDragboard().getString());
                int col = (int) (e.getX() / cellSize);
                int row = (int) (e.getY() / cellSize);

                // Esto asegura que el barco nunca empiece en una celda que lo haga salirse
                if (isHorizontal) {
                    // Si es horizontal, la columna máxima es (10 - tamaño)
                    col = Math.min(col, 10 - size);
                } else {
                    // Si es vertical, la fila máxima es (10 - tamaño)
                    row = Math.min(row, 10 - size);
                }
                // Aseguramos que nunca sea negativo (por si acaso)
                col = Math.max(0, col);
                row = Math.max(0, row);

                updateHighlight(col, row, size);
            }
            e.consume();
        });

        shipsPane.setOnDragDropped(e -> {
            boolean success = false;
            Dragboard db = e.getDragboard();
            if (db.hasString()) {
                int size = Integer.parseInt(db.getString());
                int col = (int) (e.getX() / cellSize);
                int row = (int) (e.getY() / cellSize);

                if (isHorizontal) {
                    col = Math.min(col, 10 - size);
                } else {
                    row = Math.min(row, 10 - size);
                }
                col = Math.max(0, col);
                row = Math.max(0, row);

                // Lógica de Board
                Board board = controller.getPlayerLogical();
                Ship tempShip = new Ship(size); // Barco temporal para validar

                if (board.canPlaceShip(tempShip, row, col, isHorizontal)) {
                    board.placeShip(tempShip, row, col, isHorizontal);
                    placeVisualShip(col, row, size);
                    controller.checkFleetComplete();
                    success = true;
                }
            }
            visualizer.getSelectionHighlight().setVisible(false);
            e.setDropCompleted(success);
            e.consume();
        });

        // Rotar con click derecho
        shipsPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) isHorizontal = !isHorizontal;
        });
    }

    public void createDraggableShip(Canvas canvas, int size) {
        renderer.render(canvas, size);

        canvas.setOnDragDetected(e -> {
            Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(size));
            db.setContent(content);

            WritableImage snapshot = canvas.snapshot(null, null);
            db.setDragView(snapshot);
            e.consume();
        });

        canvas.setOnDragDone(e -> {
            if (e.getTransferMode() == TransferMode.MOVE) {
                // TRUCO DEL FANTASMA:
                // En lugar de borrarlo (remove), lo hacemos totalmente transparente.
                // Así sigue ocupando espacio y los Labels NO se mueven.
                canvas.setOpacity(0);
                canvas.setMouseTransparent(true); // Para que no puedas agarrar el fantasma
            }
        });
    }

    private void updateHighlight(int col, int row, int size) {
        Rectangle rect = visualizer.getSelectionHighlight();
        if (col < 0 || row < 0 || col >= 10 || row >= 10) { rect.setVisible(false); return; }

        rect.setWidth(isHorizontal ? size * cellSize : cellSize);
        rect.setHeight(isHorizontal ? cellSize : size * cellSize);
        rect.setLayoutX(col * cellSize);
        rect.setLayoutY(row * cellSize);

        // Validación visual simple
        boolean fit = isHorizontal ? (col + size <= 10) : (row + size <= 10);
        rect.setFill(fit ? Color.rgb(0, 255, 0, 0.4) : Color.rgb(255, 0, 0, 0.4));
        rect.setVisible(true);
    }

    private void placeVisualShip(int col, int row, int size) {
        Canvas canvas = new Canvas(size * cellSize, cellSize); // Horizontal base
        renderer.render(canvas, size);

        if (!isHorizontal) {
            canvas.setRotate(90);
            // Corregir pivote de rotación
            double offset = cellSize * (1 - size) / 2.0;
            canvas.setLayoutX((col * cellSize) + offset);
            canvas.setLayoutY((row * cellSize) - offset);
        } else {
            canvas.setLayoutX(col * cellSize);
            canvas.setLayoutY(row * cellSize);
        }
        canvas.setMouseTransparent(true); // ¡Importante!
        shipsPane.getChildren().add(canvas);
    }
}