package com.example.batallanaval.controllers;

import com.example.batallanaval.models.*;
import com.example.batallanaval.models.ai.*;
import com.example.batallanaval.observer.*;
import com.example.batallanaval.views.Ship2D;
import com.example.batallanaval.views.ShipAdapter;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameController implements Observer {

    @FXML private GridPane playerBoardView;
    @FXML private GridPane machineBoardView;
    @FXML private StackPane playerArea;
    @FXML private Pane shipLayer;

    @FXML private Button btnStart;

    @FXML private Button btnReveal;
    @FXML private Label playerShipsLabel;
    @FXML private Label machineShipsLabel;
    @FXML private TextArea logArea;

    private final int CELL = 40;

    private Board playerBoardModel = new Board();
    private Board machineBoardModel = new Board();
    private MachineAI ai = new MachineAI(new HuntTargetStrategy());

    private boolean placementPhase = true;

    private Ship2D dragging = null;
    private Ship draggingLogical = null;
    private double offsetX, offsetY;
    private boolean didDrag = false;

    @FXML
    public void initialize() {

        playerBoardModel.addObserver(this);
        machineBoardModel.addObserver(this);

        Fleet playerFleet = new Fleet();
        for (Ship ship : playerFleet.getShips()) {
            Ship2D s2d = ShipAdapter.toGraphic(ship);
            shipLayer.getChildren().add(s2d);
            registerDragEvents(s2d, ship);
        }

        Fleet machineFleet = new Fleet();
        machineBoardModel.randomize(machineFleet);

        btnStart.setDisable(true);
        btnStart.setOnAction(e -> startBattle());
        btnReveal.setOnAction(e->revealEnemyFleet());

        updateHUD();
    }

    private void registerDragEvents(Ship2D ship2D, Ship logicalShip) {

        ship2D.setOnMousePressed(e -> {
            if (!placementPhase) return;

            dragging = ship2D;
            draggingLogical = logicalShip;
            didDrag = false;

            Bounds b = ship2D.localToScene(ship2D.getBoundsInLocal());
            Point2D pos = shipLayer.sceneToLocal(b.getMinX(), b.getMinY());
            ship2D.snap(pos.getX(), pos.getY());

            Point2D pressPos = shipLayer.sceneToLocal(e.getSceneX(), e.getSceneY());
            offsetX = pressPos.getX() - ship2D.getLayoutX();
            offsetY = pressPos.getY() - ship2D.getLayoutY();
        });

        ship2D.setOnMouseDragged(e -> {
            if (!placementPhase || dragging == null) return;

            didDrag = true;

            Point2D p = shipLayer.sceneToLocal(e.getSceneX(), e.getSceneY());
            ship2D.snap(p.getX() - offsetX, p.getY() - offsetY);
        });

        ship2D.setOnMouseReleased(e -> {
            if (!placementPhase || dragging == null) return;

            if (!didDrag) {
                dragging = null;
                draggingLogical = null;
                return;
            }

            Bounds b = ship2D.localToScene(ship2D.getBoundsInLocal());
            Point2D scene = new Point2D(b.getMinX(), b.getMinY());
            Point2D boardPos = playerBoardView.sceneToLocal(scene);

            int col = (int)(boardPos.getX() / CELL);
            int row = (int)(boardPos.getY() / CELL);

            if (!playerBoardModel.canPlace(draggingLogical, row, col)) {
                log("❌ Posición inválida");
                dragging = null;
                draggingLogical = null;
                return;
            }

            playerBoardModel.place(draggingLogical, row, col);

            Point2D snapScene = playerBoardView.localToScene(col * CELL, row * CELL);
            Point2D snapLayer = shipLayer.sceneToLocal(snapScene);

            ship2D.snap(snapLayer.getX(), snapLayer.getY());
            ship2D.setColor(Color.WHITE);

            dragging = null;
            draggingLogical = null;

            if (playerBoardModel.remainingShips() == new Fleet().getShips().size()) {
                // all placed
                btnStart.setDisable(false);
            }
            updateHUD();
        });
    }

    private void startBattle() {
        placementPhase = false;

        for (Node n : shipLayer.getChildren()) {
            n.setOnMousePressed(null);
            n.setOnMouseDragged(null);
            n.setOnMouseReleased(null);
        }

        shipLayer.setMouseTransparent(true);

        machineBoardView.setOnMouseClicked(this::handlePlayerShot);
        btnReveal.setDisable(true);
        log("⚔ ¡Comienza la batalla!");
    }

    private void handlePlayerShot(MouseEvent e) {

        int col = (int)(e.getX() / CELL);
        int row = (int)(e.getY() / CELL);

        ShotResult r = machineBoardModel.shoot(row, col);
        if (r == null) return;

        paint(machineBoardView, row, col, r);
        log("🎯 Atacaste [" + row + "," + col + "] → " + r);

        updateHUD();

        if (machineBoardModel.isGameOver()) {
            log("🏆 ¡Ganaste! La máquina no tiene barcos.");
            machineBoardView.setOnMouseClicked(null);
            return;
        }

        machineTurn();
    }

    private void machineTurn() {

        int[] shot = ai.shoot(playerBoardModel);
        int r = shot[0];
        int c = shot[1];

        ShotResult result = playerBoardModel.shoot(r, c);
        if (result == null) return;

        paint(playerBoardView, r, c, result);
        log("💥 La máquina atacó [" + r + "," + c + "] → " + result);

        updateHUD();

        if (playerBoardModel.isGameOver()) {
            log("☠ Has sido derrotado.");
            machineBoardView.setOnMouseClicked(null);
        }
    }

    private void paint(GridPane pane, int row, int col, ShotResult r) {
        Rectangle rect = new Rectangle(CELL, CELL);

        switch (r) {
            case MISS -> rect.setFill(Color.LIGHTBLUE);
            case HIT -> rect.setFill(Color.ORANGE);
            case SUNK -> rect.setFill(Color.RED);
        }
        pane.add(rect, col, row);
    }

    private void updateHUD() {
        playerShipsLabel.setText(String.valueOf(playerBoardModel.remainingShips()));
        machineShipsLabel.setText(String.valueOf(machineBoardModel.remainingShips()));
    }

    private void log(String text) {
        logArea.appendText(text + "\n");
    }

    @Override
    public void update(Event event) {
        // For now, we mainly use Observer to refresh HUD and detect game over.
        if (event.getType() == EventType.FLEET_UPDATED) {
            updateHUD();
        } else if (event.getType() == EventType.GAME_OVER) {
            if (event.getSource() == machineBoardModel) {
                log("🏆 ¡Ganaste (Observer)!");
            } else if (event.getSource() == playerBoardModel) {
                log("☠ Has perdido (Observer).");
            }
        }
    }
    private void revealEnemyFleet() {

        // Evitar revelar durante la batalla real
        if (!placementPhase) {
            log("⚠ No puedes revelar la flota durante la batalla.");
            return;
        }

        // Limpiar marcas previas (si se presiona varias veces)
        machineBoardView.getChildren().removeIf(n -> n.getUserData() != null);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {

                Cell cell = machineBoardModel.getCell(row, col);

                if (cell.hasShip()) {
                    Rectangle r = new Rectangle(CELL, CELL);
                    r.setFill(Color.rgb(0, 0, 0, 0.25)); // sombra oscura
                    r.setStroke(Color.DARKRED);
                    r.setUserData("reveal"); // marca para poder borrar luego

                    machineBoardView.add(r, col, row);
                }
            }
        }

        log("👁 Flota enemiga revelada (modo observación)");
    }

}
