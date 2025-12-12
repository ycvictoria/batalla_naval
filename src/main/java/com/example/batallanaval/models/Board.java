package com.example.batallanaval.models;

import com.example.batallanaval.models.Ship;
import com.example.batallanaval.models.ShotResult;

import java.util.*;

public class Board {

    private final int SIZE = 10;
    private final Cell[][] grid = new Cell[SIZE][SIZE];

    private final List<Ship> fleet = new ArrayList<>();

    public Board() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = new Cell();
    }

    // ==========================================================
    // BASIC GETTERS
    // ==========================================================
    public int getSize() {
        return SIZE;
    }

    public Cell peek(int row, int col) {
        return grid[row][col];
    }

    public boolean isFleetComplete() {
        return !fleet.isEmpty() && fleet.stream().allMatch(Ship::isPlaced);
    }

    // ==========================================================
    // SHIP PLACEMENT
    // ==========================================================
    public boolean canPlaceShip(Ship ship, int row, int col, boolean horiz) {

        int length = ship.getLength();

        // Boundaries
        if (horiz) {
            if (col + length > SIZE) return false;
        } else {
            if (row + length > SIZE) return false;
        }

        // Check collisions
        for (int i = 0; i < length; i++) {
            int r = row + (horiz ? 0 : i);
            int c = col + (horiz ? i : 0);

            if (grid[r][c].hasShip()) return false;
        }

        return true;
    }

    public void placeShip(Ship ship, int row, int col, boolean horiz) {

        int length = ship.getLength();

        for (int i = 0; i < length; i++) {
            int r = row + (horiz ? 0 : i);
            int c = col + (horiz ? i : 0);

            grid[r][c].ship = ship;
        }

        ship.setPlaced(true);
        fleet.add(ship);
    }

    // ==========================================================
    // SHOOTING LOGIC
    // ==========================================================
    public ShotResult shoot(int row, int col) {

        Cell cell = grid[row][col];

        // Already shot here?
        if (cell.shot) return null;

        cell.shot = true;

        if (!cell.hasShip()) {
            return ShotResult.MISS;
        }

        Ship ship = cell.ship;
        ship.registerHit();

        if (ship.isSunk()) {
            return ShotResult.SUNK;
        }

        return ShotResult.HIT;
    }

    // ==========================================================
    // RANDOM FLEET GENERATION
    // ==========================================================
    public void randomizeShips() {

        int[] lengths = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1}; // típica flota

        for (int len : lengths) {

            Ship s = new Ship(len);
            boolean placed = false;

            while (!placed) {

                int row = (int)(Math.random() * SIZE);
                int col = (int)(Math.random() * SIZE);
                boolean horiz = Math.random() < 0.5;

                if (canPlaceShip(s, row, col, horiz)) {
                    placeShip(s, row, col, horiz);
                    placed = true;
                }
            }
        }
    }

    // ==========================================================
    // INTERNAL CELL CLASS
    // ==========================================================
    public static class Cell {
        private boolean shot = false;
        private Ship ship = null;

        public boolean hasShip() {
            return ship != null;
        }

        public Ship getShip() {
            return ship;
        }

        public boolean isShot() {
            return shot;
        }
    }

    // ==========================================================
    // GAME OVER CHECK
    // ==========================================================

    // Verifica si todos los barcos de la flota han sido hundidos.

    public boolean isGameOver() {
        return !fleet.isEmpty() &&
                fleet.stream().allMatch(Ship::isSunk);
    }
}


