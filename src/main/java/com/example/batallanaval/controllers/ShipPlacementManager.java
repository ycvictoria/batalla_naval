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

/**
 * Clase encargada de gestionar toda la lógica de arrastrar y soltar (Drag-and-Drop)
 * para la colocación inicial de los barcos en el tablero del jugador humano.
 * Maneja la creación de barcos arrastrables desde el menú, la validación de posición
 * en el tablero y la gestión de la orientación (horizontal/vertical).
 */
public class ShipPlacementManager {
    /** Referencia al controlador principal del juego para acceder a la lógica del tablero. */
    private final GameController controller;
    /** Referencia al visualizador del tablero para manipular elementos como el highlight. */
    private final BoardVisualizer visualizer;
    /** El panel (Pane) que contiene el tablero y donde se añaden los barcos visuales. */
    private final Pane shipsPane;
    /** Tamaño en píxeles de una celda del tablero. */
    private final double cellSize;
    /** Indica la orientación actual de colocación de los barcos (true para horizontal). */
    private boolean isHorizontal = true;
    /** Renderizador para dibujar los barcos en los objetos Canvas. */
    private final CanvasShipRenderer renderer = new CanvasShipRenderer();

    /**
     * Constructor del gestor de colocación de barcos.
     * @param controller El controlador principal del juego.
     * @param visualizer El visualizador del tablero para el highlight.
     * @param shipsPane El panel donde se colocarán los barcos visuales.
     * @param cellSize El tamaño de la celda en píxeles.
     */
    public ShipPlacementManager(GameController controller, BoardVisualizer visualizer, Pane shipsPane, double cellSize) {
        this.controller = controller;
        this.visualizer = visualizer;
        this.shipsPane = shipsPane;
        this.cellSize = cellSize;
        setupBoardDragHandlers();
    }

    /**
     * Configura los manejadores de eventos de arrastre y soltado en el panel del tablero
     * para permitir la colocación de barcos.
     */
    private void setupBoardDragHandlers() {
        // Arrastras el barco sobre el tablero.
        shipsPane.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                int size = Integer.parseInt(e.getDragboard().getString());
                int col = (int) (e.getX() / cellSize);
                int row = (int) (e.getY() / cellSize);
                // Corrección de límites.
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

        // Maneja el evento de soltar el barco en el tablero.
        shipsPane.setOnDragDropped(e -> {
            boolean success = false;
            Dragboard db = e.getDragboard();
            if (db.hasString()) {
                int size = Integer.parseInt(db.getString());
                int col = (int) (e.getX() / cellSize);
                int row = (int) (e.getY() / cellSize);

                // Corrección de límites.
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
                    controller.checkFleetComplete();
                    success = true;
                }
            }
            // Ocultar highlight al soltar exitosamente
            visualizer.getSelectionHighlight().setVisible(false);
            e.setDropCompleted(success);
            e.consume();
        });

        // Click derecho para rotar (Atajo)
        shipsPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) toggleOrientation();
        });
        // Ocultar el highlight al salir del área de arrastre
        shipsPane.setOnDragExited(e -> {
            visualizer.getSelectionHighlight().setVisible(false);
        });
    }

    /**
     * Configura un objeto Canvas que representa un barco en el menú para que sea arrastrable.
     * Al detectar el arrastre, se inicia la operación D&D y se configura la imagen de arrastre.
     * @param canvas El Canvas del menú que representa el barco.
     * @param size El tamaño lógico del barco.
     */
    public void createDraggableShip(Canvas canvas, int size) {
        renderer.render(canvas, size);

        canvas.setOnDragDetected(e -> {
            Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(size));
            db.setContent(content);

            // Rotación al arrastrar desde menú
            WritableImage snapshot = canvas.snapshot(null, null);
            if (!isHorizontal) {
                snapshot = rotateImage(snapshot);
            }

            db.setDragView(snapshot);
            e.consume();
        });
        //Maneja el evento cuando la operación de arrastre finaliza.
        canvas.setOnDragDone(e -> {
            visualizer.getSelectionHighlight().setVisible(false);
            if (e.getTransferMode() == TransferMode.MOVE) {
                // Truco del fantasma en el menú
                canvas.setOpacity(0);
                canvas.setMouseTransparent(true);
            }
        });
    }

    /**
     * Coloca un nuevo barco visual (Canvas) en el panel del tablero.
     * Además, configura los manejadores de arrastre para que este barco colocado pueda ser movido nuevamente.
     * @param col La columna inicial (índice 0-9).
     * @param row La fila inicial (índice 0-9).
     * @param size El tamaño del barco.
     * @param placedShip La referencia al objeto Ship del modelo lógico.
     */
    private void placeVisualShip(int col, int row, int size, Ship placedShip) {
        Canvas canvas = new Canvas(size * cellSize, cellSize);
        renderer.render(canvas, size);

        if (!isHorizontal) {
            canvas.setRotate(90);
            // Ajuste de layout para mantener el barco centrado visualmente después de la rotación
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

    /**
     * Configura los manejadores de arrastre para un barco ya colocado en el tablero,
     * permitiendo al usuario moverlo o devolverlo al menú.
     * @param canvas El Canvas visual que representa el barco en el tablero.
     * @param shipRef La referencia lógica del barco que se va a arrastrar.
     * @param size El tamaño del barco.
     */
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

            // 4. Hace el barco invisible e intangible des pues de iniciar el arrastre.
            javafx.application.Platform.runLater(() -> {
                canvas.setOpacity(0.0);           // Invisible
                canvas.setMouseTransparent(true); // Intangible (para que el mouse vea el tablero)
            });

            e.consume();
        });
        // Maneja el evento cuando la operación de arrastre finaliza.
        canvas.setOnDragDone(e -> {
            // 5. Borrar el canvas viejo del Pane
            shipsPane.getChildren().remove(canvas);

            // 6. Forzar el apagado del cuadro verde/rojo
            visualizer.getSelectionHighlight().setVisible(false);

            // 7. Si el drop falló (el TransferMode es null, es decir, no se soltó en una zona válida)
            if (e.getTransferMode() == null) {
                controller.returnShipToPanel(size); // Devuélvelo a su casa
            }
            e.consume();
        });
    }

    /**
     * Actualiza la posición, tamaño y color del rectángulo de resaltado (highlight)
     * para indicar al usuario la posición potencial de colocación del barco y si
     * es una posición válida (verde) o inválida (rojo).
     * @param col La columna de inicio del resaltado.
     * @param row La fila de inicio del resaltado.
     * @param size El tamaño lógico del barco (longitud).
     */
    private void updateHighlight(int col, int row, int size) {
        Rectangle rect = visualizer.getSelectionHighlight();
        // Ocultar si está fuera de los límites
        if (col < 0 || row < 0 || col >= 10 || row >= 10) { rect.setVisible(false); return; }

        rect.setWidth(isHorizontal ? size * cellSize : cellSize);
        rect.setHeight(isHorizontal ? cellSize : size * cellSize);
        rect.setLayoutX(col * cellSize);
        rect.setLayoutY(row * cellSize);

        // Comprueba si el barco cabe dentro del límite 10x10
        boolean fit = isHorizontal ? (col + size <= 10) : (row + size <= 10);
        rect.setFill(fit ? Color.rgb(0, 255, 0, 0.4) : Color.rgb(255, 0, 0, 0.4));
        rect.setVisible(true);
    }

    /**
     * Crea una nueva imagen WritableImage rotada 90 grados en el sentido de las agujas del reloj
     * a partir de una imagen de entrada.
     * @param img La imagen WritableImage de entrada.
     * @return Una nueva WritableImage rotada.
     */
    private WritableImage rotateImage(WritableImage img) {
        // Intercambiar ancho y alto para la rotación de 90 grados
        Canvas rotCanvas = new Canvas(img.getHeight(), img.getWidth());
        javafx.scene.canvas.GraphicsContext gc = rotCanvas.getGraphicsContext2D();
        // Mover el punto de rotación al borde superior derecho
        gc.translate(rotCanvas.getWidth(), 0);
        gc.rotate(90);
        // Dibujar la imagen original
        gc.drawImage(img, 0, 0);
        return rotCanvas.snapshot(null, null);
    }

    /**
     * Alterna la orientación de colocación de los barcos (de horizontal a vertical o viceversa).
     * @return true si la orientación actual es horizontal, false si es vertical.
     */
    public boolean toggleOrientation() {
        isHorizontal = !isHorizontal;
        return isHorizontal;
    }

    /**
     * Devuelve la orientación actual de colocación de los barcos.
     * @return true si los barcos se colocan horizontalmente, false si es vertical.
     */
    public boolean isHorizontal() {
        return isHorizontal;
    }
}