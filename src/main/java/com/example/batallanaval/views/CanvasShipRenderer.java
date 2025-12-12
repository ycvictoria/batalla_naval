package com.example.batallanaval.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CanvasShipRenderer implements ShipRenderer {

    @Override
    public void render(Canvas canvas, int size) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        // Calculamos el margen (padding)
        double padding = h * 0.15;

        // Calculamos ancho y alto restando el margen de ambos lados
        double shipW = w - (padding * 2);
        double shipH = h - (padding * 2);

        // Pasamos 'padding' directamente como X e Y iniciales
        switch (size) {
            case 4 -> drawCarrier(gc, padding, padding, shipW, shipH);
            case 3 -> drawSubmarine(gc, padding, padding, shipW, shipH);
            case 2 -> drawDestroyer(gc, padding, padding, shipW, shipH);
            default -> drawFrigate(gc, padding, padding, shipW, shipH);
        }
    }

    private void drawCarrier(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.rgb(100, 100, 110)); // Gris oscuro
        double[] xPoints = { x, x + w - 20, x + w, x + w - 20, x };
        double[] yPoints = { y, y, y + h / 2, y + h, y + h };
        gc.fillPolygon(xPoints, yPoints, 5);

        // Pista de aterrizaje
        gc.setStroke(Color.WHITESMOKE);
        gc.setLineWidth(2);
        gc.setLineDashes(10, 5);
        gc.strokeLine(x + 10, y + h / 2, x + w - 30, y + h / 2);
        gc.setLineDashes(null);
    }

    private void drawSubmarine(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.rgb(60, 70, 90)); // Azul acero
        gc.fillRoundRect(x, y, w, h, h, h);
        gc.setFill(Color.BLACK);
        gc.fillOval(x + w/2 - 5, y + 5, 10, 10); // Torreta
    }

    private void drawDestroyer(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.rgb(130, 130, 135)); // Gris medio
        gc.fillPolygon(new double[]{x, x+w, x+w-10, x}, new double[]{y, y+h/2, y+h, y+h}, 4);
        // Cañones
        gc.setFill(Color.BLACK);
        gc.fillOval(x + w*0.2, y + h*0.3, 10, 10);
        gc.fillOval(x + w*0.7, y + h*0.3, 10, 10);
    }

    private void drawFrigate(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.rgb(200, 200, 200)); // Gris claro
        gc.fillPolygon(new double[]{x, x+w, x}, new double[]{y, y+h/2, y+h}, 3);
    }
}