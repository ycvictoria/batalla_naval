package com.example.batallanaval.models;

import com.example.batallanaval.observer.*;

import java.util.ArrayList;
import java.util.List;

public class Board implements Observable {

    private final int SIZE = 10;

    private final Cell[][] grid = new Cell[SIZE][SIZE];
    private final List<Ship> ships = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();

    public Board() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = new Cell();
    }

    public int getSize() { return SIZE; }

    public Cell getCell(int r, int c) { return grid[r][c]; }

    @Override
    public void addObserver(Observer o) { observers.add(o); }

    @Override
    public void removeObserver(Observer o) { observers.remove(o); }

    @Override
    public void notifyObservers(Event e) {
        for (Observer obs : observers)
            obs.update(e);
    }

    public boolean canPlace(Ship ship, int row, int col) {
        if (col < 0 || row < 0 || row >= SIZE) return false;
        if (col + ship.getSize() > SIZE) return false;

        for (int i = 0; i < ship.getSize(); i++) {
            if (grid[row][col + i].hasShip())
                return false;
        }
        return true;
    }

    public void place(Ship ship, int row, int col) {
        ship.setRow(row);
        ship.setCol(col);

        for (int i = 0; i < ship.getSize(); i++)
            grid[row][col + i].setShip(ship);

        ship.setPlaced(true);
        if (!ships.contains(ship))
            ships.add(ship);

        notifyObservers(new Event(EventType.FLEET_UPDATED, this, null));
    }

    public ShotResult shoot(int r, int c) {

        if (r < 0 || c < 0 || r >= SIZE || c >= SIZE) return null;

        Cell cell = grid[r][c];

        if (cell.wasShot()) return null;

        cell.markShot();

        if (!cell.hasShip()) {
            notifyObservers(new Event(EventType.MISS, this, new int[]{r, c}));
            return ShotResult.MISS;
        }

        Ship s = cell.getShip();
        s.hit();

        if (s.isSunk()) {
            notifyObservers(new Event(EventType.SUNK, this, s));
            if (isGameOver()) {
                notifyObservers(new Event(EventType.GAME_OVER, this, null));
            }
            return ShotResult.SUNK;
        }

        notifyObservers(new Event(EventType.HIT, this, new int[]{r, c}));
        return ShotResult.HIT;
    }

    public boolean isGameOver() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    public int remainingShips() {
        int count = 0;
        for (Ship s : ships)
            if (!s.isSunk()) count++;
        return count;
    }
    public void randomize(Fleet fleet) {

        for (Ship ship : fleet.getShips()) {

            boolean placed = false;

            while (!placed) {

                int row = (int) (Math.random() * SIZE);
                int col = (int) (Math.random() * SIZE);

                // Por ahora colocamos horizontal (como tu implementación previa)
                if (canPlace(ship, row, col)) {
                    place(ship, row, col);
                    placed = true;
                }
            }
        }
    }

}
