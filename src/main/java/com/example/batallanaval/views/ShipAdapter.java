package com.example.batallanaval.views;

import com.example.batallanaval.models.Ship;

public class ShipAdapter {

    public static Ship2D toGraphic(Ship ship) {
        return new Ship2D(ship.getSize());
    }
}
