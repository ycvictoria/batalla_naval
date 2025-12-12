package com.example.batallanaval.models.factories;

import com.example.batallanaval.models.Ship;

public class DestroyerFactory implements ShipFactory {
    @Override
    public Ship create() {
        return new Ship(2);
    }
}
