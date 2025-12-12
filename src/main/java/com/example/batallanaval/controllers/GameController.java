package com.example.batallanaval.controllers;

import com.example.batallanaval.models.*;
import com.example.batallanaval.views.Ship2D;
import com.example.batallanaval.views.ShipAdapter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.Optional;

public class GameController {

    // ========= FXML ELEMENTS =========
    @FXML
    private VBox fleetPanel;
    @FXML
    private StackPane playerArea;
    @FXML
    private Pane shipLayer;
    @FXML
    private GridPane playerBoard;
    @FXML
    private GridPane machineBoard;

    @FXML
    private Button btnRotate;
    @FXML
    private Button btnReveal;
    @FXML
    private Button btnStart;

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
    private String nickName = "Jugador 1";
    private int playerShipsSunk = 0;


    // =====================================================================
    // INITIALIZATION
    // =====================================================================
    @FXML
    public void initialize() {
        // iniciar un juego cargado
        if (loadPendingGame()) {
            System.out.println("Juego pendiente cargado. A continuar con la batalla");
        } else {
            // Generate enemy fleet
            machineLogical.randomizeShips();

            // Generate player fleet and initialize dragging behavior
            initPlayerFleet();

        }
        // Rotate ship button
        btnRotate.setOnAction(e -> rotateSelectedShip());
        // Reveal machine board (debug)
        btnReveal.setOnAction(e -> revealEnemyFleet());
        // Start battle button
        btnStart.setDisable(false);
        btnStart.setOnAction(e -> startBattlePhase());
        // Disable shooting until battle start
        enableMachineShotEvents(false);
    }

    // =====================================================================
    // PLAYER FLEET INITIALIZATION
    // =====================================================================
    private void initPlayerFleet() {

        List<Ship> fleet = List.of(
                new CarrierFactory().createShip(),     // size 4 (1)

                new SubmarineFactory().createShip(),   // size 3 (1)
                new SubmarineFactory().createShip(),   // size 3 (2)

                new DestroyerFactory().createShip(),   // size 2 (1)
                new DestroyerFactory().createShip(),   // size 2 (2)
                new DestroyerFactory().createShip(),   // size 2 (3)

                new FrigateFactory().createShip(),      // size 1 (1)
                new FrigateFactory().createShip(),      // size 1 (2)
                new FrigateFactory().createShip(),      // size 1 (3)
                new FrigateFactory().createShip()       // size 1 (4)
        );

        // Create graphics & add them to shipLayer
        int startY = 20;
        for (Ship ship : fleet) {

            Ship2D s2d = ShipAdapter.toGraphic(ship, true);
            registerShip(s2d, ship);
            fleetPanel.getChildren().add(s2d);

            // Starting position (stacked visually)
            // s2d.setLayoutX(20);
            //s2d.setLayoutY(startY);
            //startY += 70;
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

            // Row and column calculation
            int col = (int)(e.getX() / CELL);
            int row = (int)(e.getY() / CELL);

            ShotResult result = machineLogical.shoot(row, col);
            if (result == null) return;

            paint(machineBoard, row, col, result);

            // Check player's victory

            if (machineLogical.isGameOver()) {
                // Manejar Fin de Juego (Jugador gana)
                System.out.println("¡Victoria! Has hundido toda la flota enemiga.");
                disableAllShooting(); // Detiene el juego
                return;
            }

            playerShipsSunk = (int) machineLogical.getSunkShipCount();

            // Turn Logic: If it's MISS, the AI shoots
            if (result == ShotResult.MISS) {

                // AI shoots back
                int[] aiShot = ai.shoot(playerLogical);
                int r = aiShot[0];
                int c = aiShot[1];

                ShotResult machineResult = playerLogical.shoot(r, c);
                paint(playerBoard, r, c, machineResult);

                //Check machine victory
                if (playerLogical.isGameOver()) {
                    // Game Over
                    System.out.println("Derrota! La Máquina ha hundido tu flota.");
                    disableAllShooting(); // Game stops.
                }
            } else {
                // Si es HIT o SUNK, el turno se mantiene para el jugador.
                System.out.println("Has adivinado! Sigue disparando.");
            }
            //guardar el estado del juego luego del turno
            SaveGame.saveGame(playerLogical,machineLogical, ai, nickName, playerShipsSunk);
        });
    }

    private void disableAllShooting(){
        machineBoard.setOnMouseClicked(null);
        // Agregar lógica para mostrar mensaje de fin de juego.
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

    private void saveGameAct(){
        SaveGame.saveGame(playerLogical, machineLogical, ai,nickName,playerShipsSunk);
    }

    private boolean loadPendingGame() {
        Object[] loadedData = SaveGame.restartGame();

        if (loadedData == null) {
            return false; // No hay juego guardado o hubo error
        }

        // Asignar los objetos cargados a las variables de instancia
        playerLogical = (Board) loadedData[0];
        machineLogical = (Board) loadedData[1];
        ai = (MachineAI) loadedData[2];

        // Determinar la fase del juego
        // Si el tablero del jugador está completo y se han realizado disparos
        boolean hadShots = playerLogical.getShotsTaken() > 0 || machineLogical.getShotsTaken() > 0;

        if (playerLogical.isFleetComplete() && hadShots) {
            placementPhase = false;

            // Regenerar la vista gráfica de los barcos del jugador
            recreatePlayerShipsVisuals();

            // Dibujar los disparos realizados en ambos tableros
            recreateShotsVisuals(playerBoard, playerLogical);
            recreateShotsVisuals(machineBoard, machineLogical);
        } else {
            // Se cargó un juego en fase de colocación.
            placementPhase = true;
            recreatePlayerShipsVisuals();
        }

        return true;
    }

// =====================================================================
// NEW: RECREATE VISUALS AFTER LOAD
// =====================================================================

    // Debe iterar sobre los barcos lógicos y crear sus contrapartes Ship2D y colocarlas
    private void recreatePlayerShipsVisuals() {
        // 1. Limpiar la capa de barcos actual si es necesario
        shipLayer.getChildren().clear();
        fleetPanel.getChildren().clear();

        // 2. Iterar sobre todos los barcos del tablero lógico del jugador
        for (Ship ship : playerLogical.getShips()) {

            Ship2D s2d = ShipAdapter.toGraphic(ship, true);
            registerShip(s2d, ship); // Re-registrar eventos de arrastre/rotación

            if (ship.isPlaced()) {
                // El barco estaba colocado, colocarlo en el lugar correcto en la capa de barcos
                int row = ship.getTopRow();
                int col = ship.getLeftCol();

                s2d.setOrientation(ship.isHorizontal()); // Aplicar orientación guardada
                s2d.setLayoutX(playerBoard.getLayoutX() + col * CELL);
                s2d.setLayoutY(playerBoard.getLayoutY() + row * CELL);
                s2d.setColor(Color.WHITE);
                shipLayer.getChildren().add(s2d);

            } else {
                // El barco no estaba colocado, devolverlo al panel de flota
                fleetPanel.getChildren().add(s2d);
            }
        }
    }

    // Debe dibujar los rectángulos de los disparos guardados en el tablero lógico
    private void recreateShotsVisuals(GridPane pane, Board board) {
        pane.getChildren().clear();

        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                Cell cell = board.peek(r, c);

                if (cell.isShot()) {

                    ShotResult result;

                    if (cell.hasShip()) {
                        // Verificar si el barco en esa celda está hundido
                        if (cell.getShip().isSunk()) { // <-- Requiere getShip().isSunk()
                            result = ShotResult.SUNK;
                        } else {
                            result = ShotResult.HIT;
                        }
                    } else {
                        result = ShotResult.MISS;
                    }

                    // Si el resultado es SUNK, debemos pintar todas las celdas del barco hundido.
                    // Sin embargo, el método 'paint' en tu controlador solo pinta una celda.
                    // Para simplificar, pintaremos cada celda por separado.
                    paint(pane, r, c, result);
                }
            }
        }
        // Nota: Si un barco está SUNK, se pintará cada celda que haya sido disparada
        // con el color SUNK. Esto es suficiente para la visualización.
    }

}



