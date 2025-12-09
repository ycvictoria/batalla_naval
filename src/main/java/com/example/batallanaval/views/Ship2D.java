
package com.example.batallanaval.views;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Representación visual de un barco.
 */
public class Ship2D extends Group {

    private int size;
    private boolean horizontal = true;
    private final int CELL = 40;

    public Ship2D(int size) {
        this.size = size;
        draw(Color.LIGHTGRAY);
    }

    private void draw(Color color) {
        getChildren().clear();
        for (int i = 0; i < size; i++) {
            Rectangle r = new Rectangle(CELL, CELL, color);
            r.setStroke(Color.BLACK);
            r.setStrokeWidth(2);
            if (horizontal) r.setTranslateX(i * CELL);
            else r.setTranslateY(i * CELL);
            getChildren().add(r);
        }
    }

    public void toggleOrientation() {
        horizontal = !horizontal;
        draw(Color.LIGHTGRAY);
    }

    public boolean isHorizontal() { return horizontal; }

    public void setColor(Color c) { draw(c); }
}
