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
    @FXML private Pane revealLayer;
    private boolean isEnemyFleetRevealed = false;

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

        // CONFIGURACIÓN INICIAL DEL BOTÓN ROTAR
        updateRotateButtonText(); // Pone el texto correcto al iniciar
        btnRotate.setOnAction(e -> onRotateClick()); // Vincula la acción (o hazlo en el FXML)
    }

    // =====================================================================
    // LÓGICA DE ROTACIÓN
    // =====================================================================
    @FXML
    private void onRotateClick() {
        // Le pedimos al manager que cambie la orientación
        placementManager.toggleOrientation();

        // Actualizamos el texto del botón para que el usuario sepa qué va a pasar
        updateRotateButtonText();
    }

    private void updateRotateButtonText() {
        if (placementManager.isHorizontal()) {
            btnRotate.setText("Rotación: Horizontal ➡");
        } else {
            btnRotate.setText("Rotación: Vertical ⬇");
        }
    }

    // Método público para devolver un barco al menú si se cancela el movimiento
    public void returnShipToPanel(int size) {
        Pane targetContainer = switch (size) {
            case 4 -> carrierContainer;
            case 3 -> submarineContainer;
            case 2 -> destroyerContainer;
            case 1 -> frigateContainer;
            default -> null;
        };

        if (targetContainer != null) {
            createShipInPanel(size, targetContainer);

            // Si habíamos completado la flota, desactivamos el botón de inicio
            // porque acabamos de sacar un barco del tablero.
            btnStart.setDisable(true);
            btnStart.setText("🚀 Iniciar Batalla");
            btnStart.setStyle(""); // Reset estilo
        }
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

        // RESTAURAR ELEMENTOS VISUALES
        boardVisualizer.drawGrid();        // Restauramos la cuadrícula
        boardVisualizer.recreateHighlight(); // ¡RESTAURAMOS EL CUADRO VERDE!

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

    // =====================================================================
    // MÉTODO CORREGIDO: DIBUJAR BARCOS CON PODER DE ARRASTRE
    // =====================================================================
    private void drawPlayerBoardFromModel() {
        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Ship ship = playerLogical.peek(r, c).getShip();

                // Si encontramos un barco y no lo hemos dibujado aún...
                if (ship != null && !drawnShips.contains(ship)) {
                    boolean isHorizontal = false;

                    // Detectar orientación
                    if (c + 1 < 10 && playerLogical.peek(r, c + 1).getShip() == ship) {
                        isHorizontal = true;
                    }
                    if (ship.getSize() == 1) isHorizontal = true;

                    // --- DIBUJAR ---
                    Canvas canvas = new Canvas(ship.getSize() * CELL, CELL);
                    renderer.render(canvas, ship.getSize());

                    if (!isHorizontal) {
                        canvas.setRotate(90);
                        double offset = CELL * (1 - ship.getSize()) / 2.0;
                        canvas.setLayoutX((c * CELL) + offset);
                        canvas.setLayoutY((r * CELL) - offset);
                    } else {
                        canvas.setLayoutX(c * CELL);
                        canvas.setLayoutY(r * CELL);
                    }

                    // AHORA: Les damos vida para que puedas moverlos
                    placementManager.setupDragForPlacedShip(canvas, ship, ship.getSize());

                    shipLayer.getChildren().add(canvas);
                    drawnShips.add(ship);
                }
            }
        }
    }

    // =====================================================================
    // 1. EL INTERRUPTOR: REVELAR / OCULTAR MÁQUINA
    // =====================================================================
    private void revealEnemyFleet() {
        // SI YA ESTÁN VISIBLES -> LOS OCULTAMOS
        if (isEnemyFleetRevealed) {
            revealLayer.getChildren().clear(); // Borra los dibujos
            isEnemyFleetRevealed = false;
            btnReveal.setText("👁 Revelar Máquina");
            return;
        }

        // SI ESTÁN OCULTOS -> LOS DIBUJAMOS (¡CON IMÁGENES, NO CUADROS!)
        drawMachineBoardRealShips();

        isEnemyFleetRevealed = true;
        btnReveal.setText("🚫 Ocultar Máquina");
    }

    // =====================================================================
    // 2. MÉTODO AUXILIAR: DIBUJAR BARCOS REALES (IGUAL QUE EL JUGADOR)
    // =====================================================================
    private void drawMachineBoardRealShips() {
        // Usamos un Set para no dibujar el mismo barco 2 veces
        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();
        int size = machineLogical.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Ship ship = machineLogical.peek(r, c).getShip();

                // Si encontramos un barco y no lo hemos dibujado aún...
                if (ship != null && !drawnShips.contains(ship)) {
                    boolean isHorizontal = false;

                    // Detectar orientación mirando a la derecha
                    if (c + 1 < size && machineLogical.peek(r, c + 1).getShip() == ship) {
                        isHorizontal = true;
                    }
                    if (ship.getSize() == 1) isHorizontal = true; // Fragata siempre igual

                    // --- DIBUJAR ---
                    Canvas canvas = new Canvas(ship.getSize() * CELL, CELL);

                    // ¡USAMOS EL MISMO RENDERER QUE TUS BARCOS!
                    // (Aparecerán grises, igual que los tuyos)
                    renderer.render(canvas, ship.getSize());

                    // Efecto visual: Los hacemos un poco transparentes para indicar que son "espía"
                    canvas.setOpacity(0.5);

                    if (!isHorizontal) {
                        canvas.setRotate(90);
                        double offset = CELL * (1 - ship.getSize()) / 2.0;
                        canvas.setLayoutX((c * CELL) + offset);
                        canvas.setLayoutY((r * CELL) - offset);
                    } else {
                        canvas.setLayoutX(c * CELL);
                        canvas.setLayoutY(r * CELL);
                    }

                    canvas.setMouseTransparent(true);
                    revealLayer.getChildren().add(canvas); // Añadir a la capa de revelación

                    drawnShips.add(ship);
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
        // --- NUEVO: SI ESTABAN REVELADOS, OCÚLTALOS AUTOMÁTICAMENTE ---
        if (isEnemyFleetRevealed) {
            revealEnemyFleet(); // Esto llama al método de arriba y los borra
            System.out.println("⚠️ La flota enemiga se ocultó automáticamente para iniciar el juego.");
        }

        placementPhase = false;
        shipLayer.setMouseTransparent(true);
        fleetPanel.setDisable(true);

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

            ShotResult result = machineLogical.shoot(row, col);
            if (result == null) return;

            if (result == ShotResult.SUNK) {
                Ship sunkShip = machineLogical.peek(row, col).getShip();

                // 1. DIBUJAR EL BARCO CADÁVER (En la capa media 'revealLayer')
                drawSunkShipGhost(sunkShip, row, col);

                // 2. DIBUJAR EL FUEGO (En la capa superior 'enemyLayer')
                // Cambiamos 'machineBoard' por 'enemyLayer'
                markShipAsSunk(enemyLayer, machineLogical, sunkShip);

                System.out.println("¡HUNDIDO! Barco destruido.");
            } else {
                // 3. DIBUJAR HIT O MISS (En la capa superior 'enemyLayer')
                // Usamos paintOnPane en lugar de paint
                paintOnPane(enemyLayer, row, col, result);
            }

            if (machineLogical.isGameOver()) {
                System.out.println("¡VICTORIA! Has ganado.");
                handleGameOver(true);
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
            handleGameOver(false);
        } else if (machineResult != ShotResult.MISS) {
            // Si la IA acierta, vuelve a disparar (Recursivo simple)
            playMachineTurn();
        }
    }

    // =====================================================================
    // HUNDIR BARCO DEL JUGADOR (FANTASMA + FUEGO)
    // =====================================================================
    private void markPlayerShipAsSunk(Ship sunkShip) {
        // ENCONTRAR EL BARCO VISUAL Y VOLVERLO TRANSPARENTE ---

        // Encontrar la posición lógica inicial (esquina superior izquierda) del barco
        int startR = -1, startC = -1;
        boolean found = false;
        for (int r = 0; r < 10 && !found; r++) {
            for (int c = 0; c < 10 && !found; c++) {
                if (playerLogical.peek(r, c).getShip() == sunkShip) {
                    startR = r;
                    startC = c;
                    found = true;
                }
            }
        }

        if (found) {
            // 1.2. Determinar orientación para calcular la posición visual exacta
            boolean isHorizontal = false;
            if (sunkShip.getSize() == 1) isHorizontal = true;
            else if (startC + 1 < 10 && playerLogical.peek(startR, startC + 1).getShip() == sunkShip) isHorizontal = true;

            // Calcular dónde debería estar el Canvas visualmente (layoutX, layoutY)
            double expectedX, expectedY;
            if (isHorizontal) {
                expectedX = startC * CELL;
                expectedY = startR * CELL;
            } else {
                // Si es vertical, recuerda que usamos un offset al rotarlo
                double offset = CELL * (1 - sunkShip.getSize()) / 2.0;
                expectedX = (startC * CELL) + offset;
                expectedY = (startR * CELL) - offset;
            }

            // Buscar ese Canvas específico en la capa y cambiar su opacidad
            for (javafx.scene.Node node : shipLayer.getChildren()) {
                // Verificamos si es un Canvas y si coincide con la posición calculada
                if (node instanceof Canvas canvas &&
                        Math.abs(canvas.getLayoutX() - expectedX) < 1.0 && // Usamos pequeña tolerancia
                        Math.abs(canvas.getLayoutY() - expectedY) < 1.0) {

                    // ¡ENCONTRADO! Lo convertimos en fantasma
                    canvas.setOpacity(0.5);
                    canvas.setMouseTransparent(true); // Aseguramos que no estorbe
                    break; // Ya lo encontramos, dejamos de buscar
                }
            }
        }

        // PINTAR EL FUEGO ENCIMA
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (playerLogical.peek(r, c).getShip() == sunkShip) {
                    // El fuego se añade al final, así que quedará ENCIMA del barco transparente
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
    // MÉTODO AUXILIAR: PINTAR TODO EL BARCO DE FUEGO
    // =====================================================================
    private void markShipAsSunk(Pane layer, Board board, Ship sunkShip) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (board.peek(r, c).getShip() == sunkShip) {
                    // Ahora usamos paintOnPane para dibujar en la capa superior
                    paintOnPane(layer, r, c, ShotResult.SUNK);
                }
            }
        }
    }

    // =====================================================================
    // PINTAR UN BARCO HUNDIDO ESPECÍFICO
    // =====================================================================
    private void drawSunkShipGhost(Ship ship, int r, int c) {
        // 1. Calcular orientación
        boolean isHorizontal = false;
        if (ship.getSize() == 1) {
            isHorizontal = true;
        } else {
            // Miramos si la celda de la derecha o izquierda tiene el mismo barco
            // Ojo: hay que validar límites del tablero
            if (c + 1 < 10 && machineLogical.peek(r, c + 1).getShip() == ship) isHorizontal = true;
            if (c - 1 >= 0 && machineLogical.peek(r, c - 1).getShip() == ship) isHorizontal = true;
        }

        // A veces la coordenada (r,c) que recibimos es la del último disparo, no la cabeza del barco.
        // Necesitamos encontrar la esquina superior izquierda del barco para dibujarlo bien.
        int startR = r;
        int startC = c;

        if (isHorizontal) {
            // Buscar hacia la izquierda dónde empieza
            while (startC > 0 && machineLogical.peek(r, startC - 1).getShip() == ship) {
                startC--;
            }
        } else {
            // Buscar hacia arriba dónde empieza
            while (startR > 0 && machineLogical.peek(startR - 1, c).getShip() == ship) {
                startR--;
            }
        }

        // 2. Dibujar el barco fantasma
        Canvas canvas = new Canvas(ship.getSize() * CELL, CELL);
        renderer.render(canvas, ship.getSize());
        canvas.setOpacity(0.5); // Semitransparente
        canvas.setMouseTransparent(true);

        // 3. Posicionar y Rotar
        if (!isHorizontal) {
            canvas.setRotate(90);
            double offset = CELL * (1 - ship.getSize()) / 2.0;
            canvas.setLayoutX((startC * CELL) + offset);
            canvas.setLayoutY((startR * CELL) - offset);
        } else {
            canvas.setLayoutX(startC * CELL);
            canvas.setLayoutY(startR * CELL);
        }

        // 4. Agregar a la capa 'revealLayer' (que está DEBAJO del fuego pero ENCIMA del agua)
        revealLayer.getChildren().add(canvas);
    }

    // =====================================================================
    // MANEJO DE FIN DEL JUEGO (VICTORIA / DERROTA)
    // =====================================================================
    private void handleGameOver(boolean playerWon) {
        // 1. Desactivar interacciones
        machineBoard.setOnMouseClicked(null);
        machineBoard.setOnMouseMoved(null);

        // 2. Cambiar el botón de estado
        if (playerWon) {
            btnStart.setText("🏆 ¡VICTORIA! 🏆");
            btnStart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            btnStart.setDisable(true);
        } else {
            btnStart.setText("☠ DERROTA ☠");
            btnStart.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: red; -fx-font-weight: bold;");
            btnStart.setDisable(true);

            // Si perdiste, mostramos dónde estaban los barcos enemigos que faltaron
            if (!isEnemyFleetRevealed) {
                revealEnemyFleet();
            }
        }

        // 3. Mostrar Alerta Visual
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Fin del Juego");
        alert.setHeaderText(null);

        if (playerWon) {
            alert.setContentText("¡FELICIDADES ALMIRANTE!\nHas hundido toda la flota enemiga.");
        } else {
            alert.setContentText("¡HAN HUNDIDO TU FLOTA!\nMás suerte para la próxima, cadete.");
        }

        alert.show(); // Usamos show() en lugar de showAndWait() para no congelar la UI
    }
}
