
package com.example.batallanaval.models;

import java.io.Serializable;

/**
 * Representa una celda del tablero.
 */
public class Cell implements Serializable {

    private boolean hasShip;
    private Ship ship;
    private boolean shot;

    public boolean hasShip() {
        return hasShip;
    }

    public Ship getShip() {
        return ship;
    }

    public boolean isShot() {
        return shot;
    }

    public void placeShip(Ship ship) {
        this.hasShip = true;
        this.ship = ship;
    }

    public void markShot() {
        this.shot = true;
    }
}
