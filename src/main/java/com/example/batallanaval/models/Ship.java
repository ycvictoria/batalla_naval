package com.example.batallanaval.models;

public class Ship {

    private final int size;
    private int hits = 0;
    private int row, col;
    private boolean placed = false;

    public Ship(int size) {
        this.size = size;
    }

    public int getSize() { return size; }

    public void hit() { hits++; }

    public boolean isSunk() { return hits >= size; }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }

    public boolean isPlaced() { return placed; }
    public void setPlaced(boolean placed) { this.placed = placed; }

}
