package com.example.batallanaval.models;

import java.io.Serializable;

/**
 * Representa un barco.
 * Implementa Serializable para poder ser transferido o guardado.
 */
public class  Ship implements Serializable {

    // Identificador de versión para la serialización.
    private static final long serialVersionUID = 1L;
    private final int length;
    private int hits = 0;
    private boolean placed = false;

    /**
     * Constructor para un nuevo barco.
     * @param length La longitud (tamaño) del barco.
     */
    public Ship(int length) {
        this.length = length;
    }

    /**
     * Obtiene la longitud del barco.
     * @return La longitud del barco.
     */
    public int getLength() {
        return length;
    }

    /**
     * Obtiene la longitud del barco
     * @return la longitud del barco
     */
    public int getSize() {
        return length;
    }

    /**
     * Registra un impacto en el barco, incrementando el contador de golpes.
     */
    public void registerHit() {
        hits++;
    }

    /**
     * Determina si el barco ha sido hundido (el número de hits iguala o supera su longitud).
     * @return true si el barco está hundido, false en caso contrario.
     */
    public boolean isSunk() {
        return hits >= length;
    }

    /**
     * Verifica si el barco ha sido colocado en el tablero.
     * @return true si el barco ya está colocado, false si aún no lo está.
     */
    public boolean isPlaced() {
        return placed;
    }

    /**
     * Establece el estado de colocación del barco.
     * @param p true si el barco ha sido colocado, false en caso contrario.
     */
    public void setPlaced(boolean p) {
        placed = p;
    }

    public int getRemainingLife() {
        return Math.max(0, length - hits);
    }
}
