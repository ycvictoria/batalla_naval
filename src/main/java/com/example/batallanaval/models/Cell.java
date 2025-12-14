
package com.example.batallanaval.models;

import java.io.Serializable;

/**
 * Representa una celda individual dentro del tablero de Batalla Naval.
 * Almacena el estado de la celda: si contiene un barco y si ha sido disparada.
 */
public class Cell implements Serializable {

    /** ID de serialización para asegurar la compatibilidad al guardar/cargar. */
    private static final long serialVersionUID = 1L;

    /** Indica si esta celda contiene una parte de un barco. */
    private boolean hasShip;

    /** Referencia al objeto Ship si hasShip es true; de lo contrario, es null. */
    private Ship ship;

    /** Indica si esta celda ha sido objeto de un disparo. */
    private boolean shot;

    /**
     * Verifica si la celda contiene un barco.
     * @return true si la celda contiene una parte de un barco, false en caso contrario.
     */
    public boolean hasShip() {
        return hasShip;
    }

    /**
     * Obtiene la referencia al barco que ocupa esta celda.
     * @return El objeto Ship si existe, o null si la celda está vacía.
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Verifica si la celda ha sido disparada previamente.
     * @return true si la celda ya fue disparada, false si no lo ha sido.
     */
    public boolean isShot() {
        return shot;
    }

    /**
     * Verifica si la celda está vacía (no tiene barco).
     * @return true si no hay barco en la celda (es agua), false si hay un barco.
     */
    public boolean isEmpty() {
        return !hasShip;
    }

    /**
     * Asigna un barco a esta celda, marcándola como ocupada.
     * @param ship La instancia del barco que se coloca en esta celda.
     */
    public void placeShip(Ship ship) {
        this.hasShip = true;
        this.ship = ship;
    }

    /**
     * Marca esta celda como disparada.
     */
    public void markShot() {
        this.shot = true;
    }
}
