package com.example.batallanaval.models;

import com.example.batallanaval.models.Ship;

public class FrigateFactory extends ShipFactory {
    @Override
    public Ship createShip() { return new Ship(1); }
}