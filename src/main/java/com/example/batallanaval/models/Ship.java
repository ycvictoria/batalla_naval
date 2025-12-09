package com.example.batallanaval.models;
public class Ship {

    private final int length;
    private int hits = 0;
    private boolean placed = false;

    public Ship(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public int getSize() {   // ← NECESARIO para Ship2D
        return length;
    }

    public void registerHit() { hits++; }

    public boolean isSunk() { return hits >= length; }

    public boolean isPlaced() { return placed; }

    public void setPlaced(boolean p) { placed = p; }
}
