package com.example.batallanaval.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int SIZE = 10;

    private final Cell[][] grid = new Cell[SIZE][SIZE];
    private final List<Ship> fleet = new ArrayList<>();

    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================
    public Board() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell();
            }
        }
    }

    // ==========================================================
    // GETTERS
    // ==========================================================
    public int getSize() {
        return SIZE;
    }

    public Cell peek(int row, int col) {
        return grid[row][col];
    }

    public int getFleetSize() {
        return fleet.size();
    }

    public boolean isFleetComplete() {
        return fleet.size() == 10;
    }

    // ==========================================================
    // COLOCACIÓN DE BARCOS
    // ==========================================================
    public boolean canPlaceShip(Ship ship, int row, int col, boolean horizontal) {

        int length = ship.getLength();

        // Límites
        if (horizontal) {
            if (col + length > SIZE) return false;
        } else {
            if (row + length > SIZE) return false;
        }

        // Colisiones
        for (int i = 0; i < length; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);

            if (grid[r][c].hasShip()) return false;
        }

        return true;
    }

    public void placeShip(Ship ship, int row, int col, boolean horizontal) {

        int length = ship.getLength();

        for (int i = 0; i < length; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);

            grid[r][c].placeShip(ship);
        }

        ship.setPlaced(true);
        fleet.add(ship);
    }

    public void removeShip(Ship ship) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].getShip() == ship) {
                    grid[r][c] = new Cell();
                }
            }
        }
        fleet.remove(ship);
    }

    // ==========================================================
    // DISPAROS
    // ==========================================================
    public ShotResult shoot(int row, int col) {

        Cell cell = grid[row][col];

        if (cell.isShot()) return null;

        cell.markShot();

        if (!cell.hasShip()) {
            return ShotResult.MISS;
        }

        Ship ship = cell.getShip();
        ship.registerHit();

        return ship.isSunk() ? ShotResult.SUNK : ShotResult.HIT;
    }

    // ==========================================================
    // FLOTA ALEATORIA
    // ==========================================================
    public void randomizeShips() {

        int[] lengths = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};

        for (int len : lengths) {
            Ship ship = new Ship(len);
            boolean placed = false;

            while (!placed) {
                int row = (int) (Math.random() * SIZE);
                int col = (int) (Math.random() * SIZE);
                boolean horizontal = Math.random() < 0.5;

                if (canPlaceShip(ship, row, col, horizontal)) {
                    placeShip(ship, row, col, horizontal);
                    placed = true;
                }
            }
        }
    }

    // ==========================================================
    // ESTADO DEL JUEGO
    // ==========================================================
    public boolean isGameOver() {
        return !fleet.isEmpty() &&
                fleet.stream().allMatch(Ship::isSunk);
    }

    public void clear() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell();
            }
        }
        fleet.clear();
    }
}
