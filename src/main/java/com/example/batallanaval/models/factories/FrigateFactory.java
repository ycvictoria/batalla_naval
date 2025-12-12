package com.example.batallanaval.models.factories;

import com.example.batallanaval.models.Ship;

public class FrigateFactory implements ShipFactory {
    @Override
    public Ship create() {
        return new Ship(1);
    }
}
