package com.example.batallanaval.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Clase encargada de toda la representación visual de los tableros,
 * incluyendo la cuadrícula, los highlights de selección y de objetivo.
 */
public class BoardVisualizer {
    private Pane shipsPane;
    private double cellSize;
    // Highlight del Jugador (Verde/Rojo)
    private final Rectangle selectionHighlight = new Rectangle();
    // Highlight del Enemigo (Mira Amarilla/Naranja)
    private final Rectangle targetHighlight = new Rectangle();

    /**
     * Constructor para el visualizador del tablero.
     * @param shipsPane El panel donde se dibujarán los elementos del tablero del jugador.
     * @param cellSize El tamaño en píxeles de una sola celda del tablero.
     */
    public BoardVisualizer(Pane shipsPane, double cellSize) {
        this.shipsPane = shipsPane;
        this.cellSize = cellSize;
        initializePlayerHighlight();
    }

    /**
     * Inicializa las propiedades visuales del highlight de selección del jugador.
     */
    private void initializePlayerHighlight() {
        selectionHighlight.setVisible(false);
        selectionHighlight.setArcWidth(5);
        selectionHighlight.setArcHeight(5);
        selectionHighlight.setMouseTransparent(true);
        shipsPane.getChildren().add(selectionHighlight);
    }

    /**
     * Dibuja la cuadrícula en el panel principal (`shipsPane`).
     */
    public void drawGrid() {
        drawGrid(this.shipsPane); // Dibuja en el panel principal por defecto
    }

    /**
     * Dibuja las líneas de la cuadrícula de 10x10 en un panel objetivo.
     * @param targetPane El panel donde se dibujará la cuadrícula.
     */
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

    /**
     * Prepara el highlight de objetivo para el tablero enemigo y lo añade a la capa enemiga.
     * @param enemyLayer La capa (Pane) donde se dibujará la mira enemiga.
     */
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

    /**
     * Actualiza la posición del highlight de objetivo (mira) en el tablero enemigo.
     * @param col Columna (0-9) donde se moverá la mira.
     * @param row Fila (0-9) donde se moverá la mira.
     */
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

    /**
     * Oculta el highlight de objetivo (mira) del enemigo.
     */
    public void hideTargetHighlight() {
        targetHighlight.setVisible(false);
    }

    /**
     * Asegura que el highlight de selección del jugador esté en el panel correcto.
     * Útil si el panel es limpiado o recreado.
     */
    public void recreateHighlight() {
        if (!shipsPane.getChildren().contains(selectionHighlight)) {
            shipsPane.getChildren().add(selectionHighlight);
            selectionHighlight.toBack(); // Para que no tape nada importante
        }
    }

    /**
     * Obtiene el objeto Rectangle usado como highlight de selección.
     * @return El objeto Rectangle del highlight de selección.
     */
    public Rectangle getSelectionHighlight() {
        return selectionHighlight;
    }
}