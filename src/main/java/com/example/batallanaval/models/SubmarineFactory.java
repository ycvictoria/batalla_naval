package com.example.batallanaval.models;

import com.example.batallanaval.models.Ship;
import com.example.batallanaval.models.ShipFactory;

public class SubmarineFactory extends ShipFactory {
    @Override
    public Ship createShip() { return new Ship(3); }
}