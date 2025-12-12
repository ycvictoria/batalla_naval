package com.example.batallanaval.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class CanvasShipRenderer implements ShipRenderer {

    @Override
    public void render(Canvas canvas, int size) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);

        // Sombra para profundidad
        gc.setEffect(new DropShadow(10, Color.BLACK));

        double padding = h * 0.1;
        double shipW = w - (padding * 2);
        double shipH = h - (padding * 2);
        double x = padding;
        double y = padding;

        switch (size) {
            case 4 -> drawCarrier(gc, x, y, shipW, shipH);
            case 3 -> drawSubmarine(gc, x, y, shipW, shipH);
            case 2 -> drawDestroyer(gc, x, y, shipW, shipH);
            default -> drawFrigate(gc, x, y, shipW, shipH);
        }

        gc.setEffect(null);
    }

    private void drawCarrier(GraphicsContext gc, double x, double y, double w, double h) {
        // Base: Gris metálico oscuro, forma robusta
        LinearGradient hullGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#434343")),
                new Stop(1, Color.web("#1C1C1C"))); // Casi negro abajo

        gc.setFill(hullGradient);

        // Forma: Rectángulo con la proa (frente) ligeramente en punta, pero menos que un destructor
        double[] xHull = {x, x + w - 20, x + w, x + w - 20, x};
        double[] yHull = {y, y, y + h / 2, y + h, y + h};
        gc.fillPolygon(xHull, yHull, 5);

        // 2. Cubierta de Vuelo (La pista): Gris asfalto
        gc.setFill(Color.web("#333333")); // Gris muy oscuro
        // La pista es un poco más estrecha que el casco
        gc.fillRect(x + 5, y + 5, w - 25, h - 10);

        // 3. Líneas de la Pista (Detalles blancos)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);

        // Línea central punteada (Eje de aterrizaje)
        gc.setLineDashes(10, 8);
        gc.strokeLine(x + 15, y + h / 2, x + w - 40, y + h / 2);
        gc.setLineDashes(null); // ¡Resetear siempre!

        // Líneas sólidas en los bordes de la pista (Seguridad)
        gc.setLineWidth(1);
        gc.setStroke(Color.WHITE); // Amarillo para resaltar
        gc.strokeLine(x + 15, y + 10, x + w - 35, y + 10); // Borde superior
        gc.strokeLine(x + 15, y + h - 10, x + w - 35, y + h - 10); // Borde inferior

        // La "Isla" (Torre de Control)
        gc.setFill(Color.web("#505050")); // Gris medio
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        // Un rectángulo vertical pequeño en la parte trasera-lateral
        double towerW = 25;
        double towerH = 12;
        double towerX = x + w * 0.6; // A mitad del barco hacia atrás
        double towerY = y - 4; // Sobresaliendo un poco por arriba

        gc.fillRect(towerX, towerY, towerW, towerH);
        gc.strokeRect(towerX, towerY, towerW, towerH);

        // Pequeñas ventanas de la torre (Radar)
        gc.setFill(Color.CYAN);
        gc.fillRect(towerX + 5, towerY + 2, 5, 3);

        // Elevadores de aviones (Cuadritos a los lados)
        gc.setFill(Color.web("#222222"));
        gc.fillRect(x + w * 0.3, y + h - 3, 15, 6); // Elevador abajo
    }

    private void drawSubmarine(GraphicsContext gc, double x, double y, double w, double h) {
        LinearGradient subGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#5C6B7F")), new Stop(1, Color.web("#3B4A5A")));
        gc.setFill(subGradient);
        gc.fillRoundRect(x, y + h * 0.1, w, h * 0.8, h, h);

        // Torre y detalles
        gc.setFill(Color.web("#2C3E50"));
        gc.fillRoundRect(x + w / 2 - 15, y - 2, 30, h * 0.6, 5, 5);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(x + w / 2, y, x + w / 2, y - 5);
        gc.setFill(Color.BLACK);
        gc.fillOval(x + w * 0.2, y + h * 0.4, 6, 6);
        gc.fillOval(x + w * 0.7, y + h * 0.4, 6, 6);
    }

    private void drawDestroyer(GraphicsContext gc, double x, double y, double w, double h) {
        LinearGradient camoGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#7F8C8D")), new Stop(1, Color.web("#BDC3C7")));
        gc.setFill(camoGradient);
        double[] xPoints = {x, x + w, x + w - 15, x};
        double[] yPoints = {y + 5, y + 5, y + h - 5, y + h - 5};
        gc.fillPolygon(xPoints, yPoints, 4);

        drawTurret(gc, x + w * 0.25, y + h / 2);
        drawTurret(gc, x + w * 0.75, y + h / 2);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillRect(x + w * 0.4, y + h * 0.2, w * 0.2, h * 0.6);
    }

    private void drawFrigate(GraphicsContext gc, double x, double y, double w, double h) {
        // 1. Casco: Color gris azulado oscuro (Look militar)
        LinearGradient hullGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#708090")), // SlateGrey
                new Stop(1, Color.web("#2F4F4F"))); // DarkSlateGrey
        gc.setFill(hullGrad);

        double[] xHull = {x, x + w * 0.7, x + w, x + w * 0.7, x};
        double[] yHull = {y + h * 0.15, y + h * 0.15, y + h/2, y + h * 0.85, y + h * 0.85};
        gc.fillPolygon(xHull, yHull, 5);

        // Cabina de mando (Cuadrada en la parte trasera)
        gc.setFill(Color.web("#B0C4DE")); // LightSteelBlue para contraste
        double cabinX = x + w * 0.1;
        double cabinW = w * 0.35;
        gc.fillRect(cabinX, y + h * 0.25, cabinW, h * 0.5);

        // Ventana de la cabina (Detalle pequeño)
        gc.setFill(Color.BLACK);
        gc.fillRect(cabinX + cabinW - 3, y + h * 0.3, 2, h * 0.4);

        // Cañón delantero (Para que se vea agresiva)
        drawTurret(gc, x + w * 0.65, y + h / 2);
    }

    private void drawTurret(GraphicsContext gc, double cx, double cy) {
        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillOval(cx - 5, cy - 5, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(cx, cy, cx + 8, cy);
    }
}