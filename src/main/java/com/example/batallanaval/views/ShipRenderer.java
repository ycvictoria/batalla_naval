package com.example.batallanaval.views;

import javafx.scene.canvas.Canvas;

/**
 * Define la interfaz para objetos que son responsables de renderizar un barco
 * en un componente Canvas de JavaFX.
 */
public interface ShipRenderer {
    /**
     * Renderiza la representación visual de un barco en el Canvas dado.
     * @param canvas El Canvas de JavaFX donde se dibujará el barco.
     * @param size El tamaño del barco (o el tamaño de la celda si el barco ocupa varias celdas).
     */
    void render(Canvas canvas, int size);
}
