package com.example.batallanaval.models;


public class DestroyerFactory extends ShipFactory {
    @Override
    public Ship createShip() { return new Ship(2); }
}
