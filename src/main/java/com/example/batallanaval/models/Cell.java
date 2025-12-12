package com.example.batallanaval.models;

public class Cell {

    private Ship ship;
    private boolean shot = false;

    public boolean hasShip() { return ship != null; }
    public Ship getShip() { return ship; }
    public boolean wasShot() { return shot; }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public void markShot() {
        this.shot = true;
    }
}
