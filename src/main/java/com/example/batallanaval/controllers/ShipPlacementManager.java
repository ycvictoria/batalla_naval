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

    // =====================================================================
    // MANEJO DEL TABLERO (SOLTAR BARCOS)
    // =====================================================================
    private void setupBoardDragHandlers() {
        // ARRASTRAR SOBRE EL TABLERO (MOSTRAR CUADRO VERDE/ROJO)
        shipsPane.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                int size = Integer.parseInt(e.getDragboard().getString());
                int col = (int) (e.getX() / cellSize);
                int row = (int) (e.getY() / cellSize);

                // CORRECCIÓN DE LÍMITES
                if (isHorizontal) {
                    col = Math.min(col, 10 - size);
                } else {
                    row = Math.min(row, 10 - size);
                }
                col = Math.max(0, col);
                row = Math.max(0, row);

                updateHighlight(col, row, size);
            }
            e.consume();
        });

        // SOLTAR EN EL TABLERO (COLOCAR BARCO)
        shipsPane.setOnDragDropped(e -> {
            boolean success = false;
            Dragboard db = e.getDragboard();
            if (db.hasString()) {
                int size = Integer.parseInt(db.getString());
                int col = (int) (e.getX() / cellSize);
                int row = (int) (e.getY() / cellSize);

                // CORRECCIÓN DE LÍMITES (Igual que arriba)
                if (isHorizontal) {
                    col = Math.min(col, 10 - size);
                } else {
                    row = Math.min(row, 10 - size);
                }
                col = Math.max(0, col);
                row = Math.max(0, row);

                Board board = controller.getPlayerLogical();
                Ship tempShip = new Ship(size);

                if (board.canPlaceShip(tempShip, row, col, isHorizontal)) {
                    board.placeShip(tempShip, row, col, isHorizontal);
                    // Colocamos visualmente y pasamos la referencia del barco
                    placeVisualShip(col, row, size, tempShip);
                    controller.consumeShip(size);
                    controller.checkFleetComplete();
                    success = true;
                }
            }
            // Ocultar highlight al soltar exitosamente
            visualizer.getSelectionHighlight().setVisible(false);
            e.setDropCompleted(success);
            e.consume();
        });

        // CLICK DERECHO PARA ROTAR (Atajo)
        shipsPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) toggleOrientation();
        });

        shipsPane.setOnDragExited(e -> {
            visualizer.getSelectionHighlight().setVisible(false);
        });
    }

    // =====================================================================
    // 1. ARRASTRAR DESDE EL MENÚ (CREATE)
    // =====================================================================
    public void createDraggableShip(Canvas canvas, int size) {
        renderer.render(canvas, size);

        canvas.setOnDragDetected(e -> {
            Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(size));
            db.setContent(content);

            // Magia de Rotación al arrastrar desde menú
            WritableImage snapshot = canvas.snapshot(null, null);
            if (!isHorizontal) {
                snapshot = rotateImage(snapshot);
            }

            db.setDragView(snapshot);
            e.consume();
        });

        canvas.setOnDragDone(e -> {
            // SEGURIDAD: Apagar cuadro verde por si acaso
            visualizer.getSelectionHighlight().setVisible(false);

            if (e.getTransferMode() == TransferMode.MOVE) {
                // Truco del fantasma en el menú
                canvas.setOpacity(0);
                canvas.setMouseTransparent(true);
            }
        });
    }

    // =====================================================================
    // 2. ARRASTRAR DESDE EL TABLERO (MOVE / REARRANGE)
    // =====================================================================
    private void placeVisualShip(int col, int row, int size, Ship placedShip) {
        Canvas canvas = new Canvas(size * cellSize, cellSize);
        renderer.render(canvas, size);

        if (!isHorizontal) {
            canvas.setRotate(90);
            double offset = cellSize * (1 - size) / 2.0;
            canvas.setLayoutX((col * cellSize) + offset);
            canvas.setLayoutY((row * cellSize) - offset);
        } else {
            canvas.setLayoutX(col * cellSize);
            canvas.setLayoutY(row * cellSize);
        }

        // Configurar para que se pueda volver a arrastrar
        setupDragForPlacedShip(canvas, placedShip, size);

        shipsPane.getChildren().add(canvas);
    }

    public void setupDragForPlacedShip(Canvas canvas, Ship shipRef, int size) {
        canvas.setOnDragDetected(e -> {
            // 1. Iniciar el arrastre
            Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(size));
            db.setContent(content);

            // 2. Preparar la imagen (snapshot)
            WritableImage snapshot = canvas.snapshot(null, null);

            // Lógica de rotación visual:
            // Si el canvas está rotado pero queremos horizontal -> corregimos la foto
            // Si el canvas está normal pero queremos vertical -> corregimos la foto
            boolean isShipVerticalVisual = (canvas.getRotate() != 0);
            boolean wantVertical = !isHorizontal;

            if (!isShipVerticalVisual && wantVertical) {
                snapshot = rotateImage(snapshot);
            } else if (isShipVerticalVisual && !wantVertical) {
                snapshot = rotateImage(snapshot);
            }

            db.setDragView(snapshot);

            // 3. Quitar el barco de la lógica del tablero (para liberar las celdas)
            controller.getPlayerLogical().removeShip(shipRef);
            controller.restoreShip(size);
            // 4. SOLUCIÓN MAESTRA: Usar Platform.runLater
            // Esto espera un "pulso" a que el arrastre inicie bien antes de hacer el barco intangible.
            // Así el tablero de abajo podrá detectar el mouse y mostrar el cuadro verde.
            javafx.application.Platform.runLater(() -> {
                canvas.setOpacity(0.0);           // Invisible
                canvas.setMouseTransparent(true); // Intangible (para que el mouse vea el tablero)
            });

            e.consume();
        });

        canvas.setOnDragDone(e -> {
            // 5. Borrar el canvas viejo del Pane (ya no sirve, pondremos uno nuevo o volverá al menú)
            shipsPane.getChildren().remove(canvas);

            // 6. IMPORTANTE: Forzar el apagado del cuadro verde/rojo
            visualizer.getSelectionHighlight().setVisible(false);

            // 7. Si el drop falló (lo soltó en el agua o fuera de la ventana)
            if (e.getTransferMode() == null) {
                controller.returnShipToPanel(size); // Devuélvelo a su casa
            }
            e.consume();
        });
    }

    // =====================================================================
    // UTILIDADES
    // =====================================================================
    private void updateHighlight(int col, int row, int size) {
        Rectangle rect = visualizer.getSelectionHighlight();
        if (col < 0 || row < 0 || col >= 10 || row >= 10) { rect.setVisible(false); return; }

        rect.setWidth(isHorizontal ? size * cellSize : cellSize);
        rect.setHeight(isHorizontal ? cellSize : size * cellSize);
        rect.setLayoutX(col * cellSize);
        rect.setLayoutY(row * cellSize);

        boolean fit = isHorizontal ? (col + size <= 10) : (row + size <= 10);
        rect.setFill(fit ? Color.rgb(0, 255, 0, 0.4) : Color.rgb(255, 0, 0, 0.4));
        rect.setVisible(true);
    }

    private WritableImage rotateImage(WritableImage img) {
        Canvas rotCanvas = new Canvas(img.getHeight(), img.getWidth());
        javafx.scene.canvas.GraphicsContext gc = rotCanvas.getGraphicsContext2D();
        gc.translate(rotCanvas.getWidth(), 0);
        gc.rotate(90);
        gc.drawImage(img, 0, 0);
        return rotCanvas.snapshot(null, null);
    }

    public boolean toggleOrientation() {
        isHorizontal = !isHorizontal;
        return isHorizontal;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }
}