package com.example.batallanaval.controllers;

import com.example.batallanaval.models.*;
import com.example.batallanaval.views.Ship2D;
import com.example.batallanaval.views.ShipAdapter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class GameController {

    // ========= FXML ELEMENTS =========
    @FXML private VBox fleetPanel;
    @FXML private StackPane playerArea;
    @FXML private Pane shipLayer;
    @FXML private GridPane playerBoard;
    @FXML private GridPane machineBoard;

    @FXML private Button btnRotate;
    @FXML private Button btnReveal;
    @FXML private Button btnStart;

    // ========= GAME LOGIC =========
    private final int CELL = 40;
    private boolean placementPhase = true;

    private Board playerLogical = new Board();
    private Board machineLogical = new Board();
    private MachineAI ai = new MachineAI();

    // ========= DRAGGING VARIABLES =========
    private Ship2D selectedShip = null;
    private Ship2D dragging = null;
    private double offsetX, offsetY;


    // =====================================================================
    // INITIALIZATION
    // =====================================================================
    @FXML
    public void initialize() {

        // Generate enemy fleet
        machineLogical.randomizeShips();

        // Generate player fleet and initialize dragging behavior
        initPlayerFleet();

        // Rotate ship button
        btnRotate.setOnAction(e -> rotateSelectedShip());

        // Reveal machine board (debug)
        btnReveal.setOnAction(e -> revealEnemyFleet());

        // Start battle button
        btnStart.setDisable(true); // initially disabled
        btnStart.setOnAction(e -> startBattlePhase());

        // Disable shooting until battle start
        enableMachineShotEvents(false);
    }


    // =====================================================================
    // PLAYER FLEET INITIALIZATION
    // =====================================================================
    private void initPlayerFleet() {

        // Create ships through factories
        List<Ship> fleet = List.of(
                new CarrierFactory().createShip(),     // size 4
                new SubmarineFactory().createShip(),   // size 3
                new DestroyerFactory().createShip(),   // size 2
                new DestroyerFactory().createShip(),   // size 2
                new FrigateFactory().createShip()      // size 1
        );

        // Create graphics & add them to shipLayer
        int startY = 20;
        for (Ship ship : fleet) {

            Ship2D s2d = ShipAdapter.toGraphic(ship, true);

            // Starting position (stacked visually)
           // s2d.setLayoutX(20);
            //s2d.setLayoutY(startY);

            //startY += 70;

            registerShip(s2d, ship);
            fleetPanel.getChildren().add(s2d);
            //shipLayer.getChildren().add(s2d);
        }
    }


    // =====================================================================
    // ROTATE SHIP
    // =====================================================================
    private void rotateSelectedShip() {
        if (selectedShip != null) {
            selectedShip.toggleOrientation();
        }
    }


    // =====================================================================
    // REGISTER SHIP FOR DRAGGING + PLACEMENT
    // =====================================================================
    public void registerShip(Ship2D ship2D, Ship logicalShip) {

        // Select ship (necessary for rotate)
        ship2D.setOnMouseClicked(e -> {
            if (!placementPhase) return;

            selectedShip = ship2D;

            if (e.getButton() == MouseButton.SECONDARY)
                ship2D.toggleOrientation();
        });

        // Start dragging
        ship2D.setOnMousePressed(e -> {
            if (ship2D.getParent() != shipLayer) {
                ((Pane) ship2D.getParent()).getChildren().remove(ship2D);
                shipLayer.getChildren().add(ship2D);
            }

            if (!placementPhase) return;

            dragging = ship2D;
            offsetX = e.getSceneX() - ship2D.getLayoutX();
            offsetY = e.getSceneY() - ship2D.getLayoutY();
        });

        // Dragging movement
        ship2D.setOnMouseDragged(e -> {
            if (!placementPhase) return;

            ship2D.setLayoutX(e.getSceneX() - offsetX);
            ship2D.setLayoutY(e.getSceneY() - offsetY);
        });

        // Release → Attempt to place ship into grid
        ship2D.setOnMouseReleased(e -> {
            if (!placementPhase) return;

            // 1. Scene coordinates of ship
            double sceneX = ship2D.localToScene(0, 0).getX();
            double sceneY = ship2D.localToScene(0, 0).getY();

            // 2. Convert to playerBoard coordinates
            double gridX = playerBoard.sceneToLocal(sceneX, sceneY).getX();
            double gridY = playerBoard.sceneToLocal(sceneX, sceneY).getY();

            int col = (int)(gridX / CELL);
            int row = (int)(gridY / CELL);

            boolean horiz = ship2D.isHorizontal();

            // Validate placement
            if (playerLogical.canPlaceShip(logicalShip, row, col, horiz)) {

                playerLogical.placeShip(logicalShip, row, col, horiz);

                // Snap ship to grid
                ship2D.setLayoutX(playerBoard.getLayoutX() + col * CELL);
                ship2D.setLayoutY(playerBoard.getLayoutY() + row * CELL);

                ship2D.setColor(Color.WHITE);

                // Check if fleet is complete → enable start button
                if (playerLogical.isFleetComplete()) {
                    btnStart.setDisable(false);
                }

            } else {
                // Invalid → reset
                ship2D.setLayoutX(20);
                ship2D.setLayoutY(20);
                ship2D.setColor(Color.PINK);
            }
        });
    }


    // =====================================================================
    // ENABLE MACHINE BOARD SHOOTING
    // =====================================================================
    private void enableMachineShotEvents(boolean enable) {

        if (!enable) {
            machineBoard.setOnMouseClicked(null);
            return;
        }

        machineBoard.setOnMouseClicked(e -> {

            int col = (int)(e.getX() / CELL);
            int row = (int)(e.getY() / CELL);

            ShotResult result = machineLogical.shoot(row, col);
            if (result == null) return;

            paint(machineBoard, row, col, result);

            // AI shoots back
            int[] aiShot = ai.shoot(playerLogical);
            int r = aiShot[0];
            int c = aiShot[1];

            ShotResult machineResult = playerLogical.shoot(r, c);
            paint(playerBoard, r, c, machineResult);
        });
    }


    // =====================================================================
    // START BATTLE PHASE
    // =====================================================================
    private void startBattlePhase() {

        placementPhase = false;

        shipLayer.setDisable(true);
        fleetPanel.setDisable(true);

        enableMachineShotEvents(true);

        System.out.println("⚔ ¡Comienza la batalla!");
    }


    // =====================================================================
    // PAINT SHOT RESULT
    // =====================================================================
    private void paint(GridPane pane, int row, int col, ShotResult result) {

        Rectangle rect = new Rectangle(CELL, CELL);

        switch (result) {
            case MISS -> rect.setFill(Color.LIGHTBLUE);
            case HIT  -> rect.setFill(Color.ORANGE);
            case SUNK -> rect.setFill(Color.RED);
        }

        pane.add(rect, col, row);
    }


    // =====================================================================
    // DEBUG: REVEAL ENEMY FLEET
    // =====================================================================
    private void revealEnemyFleet() {

        int size = machineLogical.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {

                if (machineLogical.peek(r, c).hasShip()) {

                    Rectangle rect = new Rectangle(CELL, CELL);
                    rect.setFill(Color.YELLOW);
                    machineBoard.add(rect, c, r);
                }
            }
        }
    }
}
