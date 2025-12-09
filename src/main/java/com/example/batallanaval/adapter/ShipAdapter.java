package com.example.batallanaval.views;

import javafx.scene.paint.Color;
import com.example.batallanaval.models.Ship;

public class ShipAdapter {

    public static Ship2D toGraphic(Ship ship, boolean horizontal) {

        Ship2D s = new Ship2D(ship.getSize());

        if (!horizontal) {
            s.toggleOrientation();
        }

        s.setColor(Color.LIGHTBLUE);
        return s;
    }
}

