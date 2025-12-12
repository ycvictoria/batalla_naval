package com.example.batallanaval.models;

import com.example.batallanaval.models.factories.*;

import java.util.List;

public class Fleet {

    private final List<Ship> ships;

    public Fleet() {
        ships = List.of(
                new CarrierFactory().create(),
                new DestroyerFactory().create(),
                new DestroyerFactory().create(),
                new FrigateFactory().create(),
                new FrigateFactory().create()
        );
    }

    public List<Ship> getShips() {
        return ships;
    }
}
