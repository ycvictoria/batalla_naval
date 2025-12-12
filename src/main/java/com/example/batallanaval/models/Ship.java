package com.example.batallanaval.models;

import java.io.Serializable;
public class Ship implements Serializable {
    private static final long serialVersionUID = 101L;

    private final int length;
    private int hits = 0;
    private int topRow;
    private int leftCol;
    private boolean isHorizontal = true;
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

    public int getTopRow() { return topRow; }
    public int getLeftCol() { return leftCol; }
    public boolean isHorizontal() { return isHorizontal; }

    public boolean isPlaced() { return placed; }

    public void setPlacement(int row, int col, boolean horizontal) {
        this.topRow = row;
        this.leftCol = col;
        this.isHorizontal = horizontal;
        this.placed = true;
    }

    public void unPlace() {
        this.topRow = -1;
        this.leftCol = -1;
        this.placed = false;
    }
}

