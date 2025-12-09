package com.example.batallanaval.models;

public class CarrierFactory extends ShipFactory {
    @Override
    public Ship createShip() { return new Ship(4); }
}