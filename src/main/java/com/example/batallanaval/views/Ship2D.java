package com.example.batallanaval.views;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Ship2D extends Group {

    private int size;
    private Color color = Color.LIGHTBLUE;
    private  int CELL = 40; // valor inicial

    public Ship2D(int size) {
        this.size = size;
        draw();
    }

    private void draw() {
        getChildren().clear();

        for (int i = 0; i < size; i++) {
            Rectangle r = new Rectangle(CELL, CELL);
            r.setFill(Color.GRAY);
            r.setStroke(Color.BLACK);
            r.setTranslateX(i * CELL);
            getChildren().add(r);
        }
    }

    public void setColor(Color color) {
        this.color = color;
        draw();
    }

    public void snap(double x, double y) {
        relocate(x, y);
    }
}
