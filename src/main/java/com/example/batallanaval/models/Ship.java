package com.example.batallanaval.models;

import java.io.Serializable;

public class  Ship implements Serializable {
    //para serializar
    private static final long serialVersionUID = 1L;

    private final int length;
    private int hits = 0;
    private boolean placed = false;

    public Ship(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public int getSize() {
        return length;
    }

    public void registerHit() {
        hits++;
    }

    public boolean isSunk() {
        return hits >= length;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean p) {
        placed = p;
    }
}
