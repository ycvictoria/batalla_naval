package com.example.batallanaval.controllers;

import com.example.batallanaval.models.*;
import com.example.batallanaval.views.BoardVisualizer;
import com.example.batallanaval.views.CanvasMarkerRenderer;
import com.example.batallanaval.views.CanvasShipRenderer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameController {

    // ========= FXML ELEMENTS =========
    @FXML private VBox fleetPanel;
    @FXML private VBox carrierContainer;
    @FXML private VBox submarineContainer;
    @FXML private VBox destroyerContainer;
    @FXML private VBox frigateContainer;

    @FXML private StackPane playerArea;
    @FXML private Pane shipLayer;
    @FXML private GridPane playerBoard;
    @FXML private GridPane machineBoard;
    @FXML private Pane enemyLayer;

    @FXML private Button btnRotate;
    @FXML private Button btnReveal;
    @FXML private Button btnStart;

    // ========= GAME LOGIC =========
    private final int CELL = 50;
    private boolean placementPhase = true;

    private Board playerLogical = new Board();
    private Board machineLogical = new Board();
    private MachineAI ai = new MachineAI();

    // ========= NUEVOS GESTORES VISUALES =========
    private ShipPlacementManager placementManager;
    private BoardVisualizer boardVisualizer;

    // Renderer para pintar los barcos en el menú lateral antes de arrastrarlos
    private final CanvasShipRenderer renderer = new CanvasShipRenderer();

    private CanvasMarkerRenderer markerRenderer;
    private Rectangle targetHighlight;

    // =====================================================================
    // INITIALIZATION
    // =====================================================================
    @FXML
    public void initialize() {

        // 1. Inicializar tableros lógicos
        machineLogical.randomizeShips();

        // 2. Inicializar el Visualizador (Dibuja la grilla y el cuadrado de selección)
        // Le pasamos el 'shipLayer' que es el Pane transparente encima del Grid
        boardVisualizer = new BoardVisualizer(shipLayer, CELL);

        if (enemyLayer != null) {
            boardVisualizer.drawGrid(enemyLayer); // Dibuja líneas en el enemigo
            boardVisualizer.prepareEnemyBoard(enemyLayer); // Prepara la mira naranja
        }
        boardVisualizer.drawGrid();

        markerRenderer = new CanvasMarkerRenderer(CELL);

        // 3. Inicializar el Gestor de Arrastre
        // Este se encargará de toda la magia de Drag & Drop
        placementManager = new ShipPlacementManager(this, boardVisualizer, shipLayer, CELL);

        // 4. Crear la flota en el panel lateral (Canvas bonitos)
        initDraggableFleet();

        // --- AGREGA ESTO PARA LA MIRA ---
        // 1. Crear el cuadrado
        targetHighlight = new Rectangle(CELL, CELL); // Tamaño 50x50
        targetHighlight.setFill(Color.rgb(255, 165, 0, 0.3)); // Naranja transparente
        targetHighlight.setStroke(Color.ORANGE);
        targetHighlight.setStrokeWidth(2);
        targetHighlight.setVisible(false); // Oculto al principio
        targetHighlight.setMouseTransparent(true); // ¡Vital! Para que no bloquee tus clics

        // 2. Agregarlo al tablero de la máquina
        machineBoard.getChildren().add(targetHighlight);

        // 5. Configurar botones
        btnStart.setDisable(true);
        btnStart.setOnAction(e -> startBattlePhase());
        btnReveal.setOnAction(e -> revealEnemyFleet());

        // Deshabilitar disparos hasta que empiece el juego
        enableMachineShotEvents(false);
    }

    // =====================================================================
    // CREACIÓN DE FLOTA ARRASTRABLE (NUEVO MÉTODO)
    // =====================================================================
    private void initDraggableFleet() {
        //fleetPanel.getChildren().clear(); // Limpiar por si acaso
        if (carrierContainer != null) carrierContainer.getChildren().clear();
        if (submarineContainer != null) submarineContainer.getChildren().clear();
        if (destroyerContainer != null) destroyerContainer.getChildren().clear();
        if (frigateContainer != null) frigateContainer.getChildren().clear();

        // 1 Portaaviones -> al carrierContainer
        createShipInPanel(4, carrierContainer);

        // 2 Submarinos -> al submarineContainer
        createShipInPanel(3, submarineContainer);
        createShipInPanel(3, submarineContainer);

        // 3 Destructores -> al destroyerContainer
        createShipInPanel(2, destroyerContainer);
        createShipInPanel(2, destroyerContainer);
        createShipInPanel(2, destroyerContainer);

        // 4 Fragatas -> al frigateContainer
        createShipInPanel(1, frigateContainer);
        createShipInPanel(1, frigateContainer);
        createShipInPanel(1, frigateContainer);
        createShipInPanel(1, frigateContainer);
    }

    private void createShipInPanel(int size, Pane targetPane) {
        if (targetPane == null) return;

        // 1. Definimos un tamaño de celda
        int MENU_CELL = 35;

        // 2. Creamos el Canvas con el tamaño real final (Sin setScale)
        Canvas canvas = new Canvas(size * MENU_CELL, MENU_CELL);

        // 3. El Renderer es inteligente y se adapta al tamaño del Canvas automáticamente
        // (Tu CanvasShipRenderer usa canvas.getWidth() así que dibujará bien)
        renderer.render(canvas, size);

        // 4. Configurar el arrastre
        // NOTA: Al arrastrar, el manager usará el tamaño lógico (size),
        // así que al soltarlo en el tablero se verá grande (50px) otra vez.
        placementManager.createDraggableShip(canvas, size);

        // 5. Agregar al panel
        targetPane.getChildren().add(canvas);
    }

    // =====================================================================
    // ALEATORIZAR TABLERO JUGADOR
    // =====================================================================
    @FXML
    private void onRandomBoard() {
        // 1. Limpiar Lógica
        playerLogical.clear();
        playerLogical.randomizeShips(); // Usamos la misma lógica que la IA

        // 2. Limpiar Visuales del Tablero
        shipLayer.getChildren().clear();
        boardVisualizer.drawGrid(); // Redibujamos la cuadrícula porque el clear la borró

        // 3. Limpiar el Panel de la Izquierda (Ya no hay barcos para arrastrar)
        if (carrierContainer != null) carrierContainer.getChildren().clear();
        if (submarineContainer != null) submarineContainer.getChildren().clear();
        if (destroyerContainer != null) destroyerContainer.getChildren().clear();
        if (frigateContainer != null) frigateContainer.getChildren().clear();

        // 4. Dibujar los barcos en el tablero basándonos en la nueva lógica
        drawPlayerBoardFromModel();

        // 5. Activar botón de inicio (porque la flota ya está completa)
        checkFleetComplete();
    }

    // Método auxiliar para "escanear" el tablero lógico y pintar los barcos
    private void drawPlayerBoardFromModel() {
        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Ship ship = playerLogical.peek(r, c).getShip();

                // Si encontramos un barco que no hemos dibujado aún...
                if (ship != null && !drawnShips.contains(ship)) {
                    // ...significa que esta es la esquina superior izquierda del barco
                    boolean isHorizontal = false;

                    // Verificamos orientación mirando la celda de la derecha
                    if (c + 1 < 10 && playerLogical.peek(r, c + 1).getShip() == ship) {
                        isHorizontal = true;
                    }
                    // Caso especial: Fragata (Tamaño 1) siempre la tratamos como horizontal o vertical da igual
                    if (ship.getSize() == 1) isHorizontal = true;

                    // Dibujar
                    Canvas canvas = new Canvas(ship.getSize() * CELL, CELL);
                    renderer.render(canvas, ship.getSize());

                    if (!isHorizontal) {
                        canvas.setRotate(90);
                        // Ajuste de pivote para rotación correcta
                        double offset = CELL * (1 - ship.getSize()) / 2.0;
                        canvas.setLayoutX((c * CELL) + offset);
                        canvas.setLayoutY((r * CELL) - offset);
                    } else {
                        canvas.setLayoutX(c * CELL);
                        canvas.setLayoutY(r * CELL);
                    }

                    canvas.setMouseTransparent(true); // Para que no bloquee los clics
                    shipLayer.getChildren().add(canvas);

                    drawnShips.add(ship); // Marcamos como dibujado
                }
            }
        }
    }

    // =====================================================================
    // GETTER PARA EL MANAGER (IMPORTANTE)
    // =====================================================================
    public Board getPlayerLogical() {
        return playerLogical;
    }

    // Método llamado por el Manager para saber si ya están todos los barcos
    public void checkFleetComplete() {
        if (playerLogical.isFleetComplete()) {
            btnStart.setDisable(false);
        }
    }

    // =====================================================================
    // FASE DE BATALLA (DISPAROS)
    // =====================================================================
    private void startBattlePhase() {
        placementPhase = false;
        shipLayer.setMouseTransparent(true); // Ya no se pueden mover barcos
        fleetPanel.setDisable(true);

        // Feedback visual para que sepas que funcionó
        btnStart.setText("¡EN COMBATE!");
        btnStart.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");

        enableMachineShotEvents(true);
        System.out.println("⚔ ¡Comienza la batalla!");
    }

    private void enableMachineShotEvents(boolean enable) {
        if (!enable) {
            machineBoard.setOnMouseClicked(null);
            machineBoard.setOnMouseMoved(null);
            machineBoard.setOnMouseExited(null);
            return;
        }

        // 1. EVENTO DE MOVIMIENTO (El efecto que quieres)
        machineBoard.setOnMouseMoved(e -> {
            int col = (int)(e.getX() / CELL);
            int row = (int)(e.getY() / CELL);
            boardVisualizer.updateTargetHighlight(col,row);

            // Validar que esté dentro del tablero (0-9)
            if (col >= 0 && col < 10 && row >= 0 && row < 10) {
                targetHighlight.setVisible(true);

                // Mover el rectángulo a la columna y fila correcta del GridPane
                GridPane.setColumnIndex(targetHighlight, col);
                GridPane.setRowIndex(targetHighlight, row);
            } else {
                targetHighlight.setVisible(false);
            }
        });

        // 2. EVENTO DE SALIDA (Ocultar si sacas el mouse)
        machineBoard.setOnMouseExited(e -> {
            targetHighlight.setVisible(false);
        });

        machineBoard.setOnMouseClicked(e -> {
            int col = (int)(e.getX() / CELL);
            int row = (int)(e.getY() / CELL);

            targetHighlight.setVisible(false);

            // Turno del Jugador
            ShotResult result = machineLogical.shoot(row, col);
            if (result == null) return; // Ya disparado

            // --- CAMBIO AQUÍ ---
            if (result == ShotResult.SUNK) {
                // 1. Obtener el barco que acabamos de hundir
                Ship sunkShip = machineLogical.peek(row, col).getShip();

                // 2. Llamar al método que pinta fuego en TODO el barco
                markShipAsSunk(machineBoard, machineLogical, sunkShip);
                System.out.println("¡HUNDIDO! Barco destruido.");
            } else {
                // Si es MISS o HIT, pintamos solo esa celda normal
                paint(machineBoard, row, col, result);
            }

            if (machineLogical.isGameOver()) {
                System.out.println("¡VICTORIA! Has ganado.");
                machineBoard.setOnMouseClicked(null);
                return;
            }

            // Si fallas, turno de la Máquina
            if (result == ShotResult.MISS) {
                playMachineTurn();
            } else {
                System.out.println("¡Impacto! Sigues disparando.");
            }
        });
    }

    private void playMachineTurn() {
        // IA Dispara
        int[] aiShot = ai.shoot(playerLogical);
        int r = aiShot[0];
        int c = aiShot[1];

        ShotResult machineResult = playerLogical.shoot(r, c);
        if (machineResult == ShotResult.SUNK) {
            Ship sunkShip = playerLogical.peek(r, c).getShip();
            // Creamos un método especial para quemar el barco en la capa visual
            markPlayerShipAsSunk(sunkShip);
        } else {
            // Pintamos el HIT o MISS sobre la capa de barcos
            paintOnPane(shipLayer, r, c, machineResult);
        }

        if (playerLogical.isGameOver()) {
            System.out.println("DERROTA. La máquina ganó.");
            machineBoard.setOnMouseClicked(null);
        } else if (machineResult != ShotResult.MISS) {
            // Si la IA acierta, vuelve a disparar (Recursivo simple)
            playMachineTurn();
        }
    }

    // Este busca las partes del barco hundido y las pinta de fuego EN LA CAPA DEL JUGADOR
    private void markPlayerShipAsSunk(Ship sunkShip) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (playerLogical.peek(r, c).getShip() == sunkShip) {
                    // Pintamos FUEGO encima del barco
                    paintOnPane(shipLayer, r, c, ShotResult.SUNK);
                }
            }
        }
    }

    // =====================================================================
// NUEVO: PINTAR SOBRE CAPA VISUAL (Para el Tablero del Jugador)
// =====================================================================
    private void paintOnPane(Pane layer, int row, int col, ShotResult result) {
        Canvas marker = null;

        switch (result) {
            case MISS -> marker = markerRenderer.drawMiss(); // O markerRenderer si cambiaste el nombre
            case HIT  -> marker = markerRenderer.drawHit();
            case SUNK -> marker = markerRenderer.drawSunk();
        }

        if (marker != null) {
            marker.setMouseTransparent(true);
            // En un Pane normal, usamos layout X e Y, no columnas/filas
            marker.setLayoutX(col * CELL);
            marker.setLayoutY(row * CELL);

            // ¡TRUCO! Lo agregamos al final para que se dibuje ENCIMA de los barcos
            layer.getChildren().add(marker);
            marker.toFront();
        }
    }

    // =====================================================================
// PINTAR DISPAROS (CON ICONOS)
// =====================================================================
    private void paint(GridPane pane, int row, int col, ShotResult result) {

        // Usamos Canvas en lugar de Rectangle
        Canvas marker = null;

        switch (result) {
            case MISS -> marker = markerRenderer.drawMiss(); // Dibuja la X roja
            case HIT  -> marker = markerRenderer.drawHit();  // Dibuja la Bomba
            case SUNK -> marker = markerRenderer.drawSunk(); // Dibuja el Fuego
        }

        if (marker != null) {
            // Importante: setMouseTransparent(true) para que el icono no bloquee clics futuros
            marker.setMouseTransparent(true);
            pane.add(marker, col, row);
        }
    }

    // =====================================================================
    // DEBUG: REVELAR FLOTA ENEMIGA
    // =====================================================================
    private void revealEnemyFleet() {
        int size = machineLogical.getSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (machineLogical.peek(r, c).hasShip()) {
                    Rectangle rect = new Rectangle(CELL, CELL);
                    rect.setFill(Color.YELLOW);
                    rect.setOpacity(0.5);
                    machineBoard.add(rect, c, r);
                }
            }
        }
        System.out.println("Flota enemiga revelada (Modo Debug)");
    }

    // =====================================================================
    // ROTATE SHIP
    // =====================================================================
    private void rotateSelectedShip() {
    }

    // =====================================================================
// MÉTODO AUXILIAR: PINTAR TODO EL BARCO DE FUEGO
// =====================================================================
    private void markShipAsSunk(GridPane pane, Board board, Ship sunkShip) {
        // Recorremos todo el tablero buscando las partes de ese barco específico
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                // Si la celda tiene UN barco y es EL MISMO que acabamos de hundir
                if (board.peek(r, c).getShip() == sunkShip) {
                    // Forzamos a pintar FUEGO (SUNK) en esa coordenada
                    paint(pane, r, c, ShotResult.SUNK);
                }
            }
        }
    }
}
