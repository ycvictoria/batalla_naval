package com.example.batallanaval.controllers;

import com.example.batallanaval.models.*;
import com.example.batallanaval.persistence.SaveManager;
import com.example.batallanaval.views.BoardVisualizer;
import com.example.batallanaval.views.CanvasMarkerRenderer;
import com.example.batallanaval.views.CanvasShipRenderer;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Controlador principal del juego Batalla Naval.
 * Gestiona la lógica de la interfaz de usuario, la fase de colocación de barcos,
 * la fase de batalla contra la IA y la persistencia del estado del juego.
 */
public class GameController {

    // ========= FXML ELEMENTS =========
    @FXML private VBox fleetPanel;
    @FXML private Label lblPlayerName;
    @FXML private VBox carrierContainer;
    @FXML private VBox submarineContainer;
    @FXML private VBox destroyerContainer;
    @FXML private VBox frigateContainer;
    @FXML private Label lblPlayerShips;
    @FXML private Label lblMachineShips;
    @FXML private VBox placementBox;

    @FXML private StackPane playerArea;
    @FXML private Pane shipLayer;
    @FXML private GridPane playerBoard;
    @FXML private GridPane machineBoard;

    @FXML private Pane enemyLayer;
    @FXML private Pane revealLayer;
    private boolean isEnemyFleetRevealed = false;

    @FXML private Button btnRotate;
    @FXML private Button btnReveal;
    @FXML private Button btnRandom;
    @FXML private Button btnStart;


    // ========= GAME LOGIC =========
    private final int CELL = 50;
    private boolean placementPhase = true;
    private boolean isGameFinished = false;
    private String playerNickname;
    private Board playerLogical = new Board();
    private Board machineLogical = new Board();
    private MachineAI ai = new MachineAI();
    private int numSunkShips;
    private ShipPlacementManager placementManager;
    private BoardVisualizer boardVisualizer;
    // Renderer para pintar los barcos en el menú lateral antes de arrastrarlos
    private final CanvasShipRenderer renderer = new CanvasShipRenderer();
    private CanvasMarkerRenderer markerRenderer;
    private Rectangle targetHighlight;

    /**
     * Método de inicialización
     * Configura el tablero, la flota arrastable y los manejadores de eventos.
     */
    @FXML
    public void initialize() {
        // Inicializar el Visualizador (Dibuja la grilla y el cuadrado de selección)
        // Le pasamos el 'shipLayer' que es el Pane transparente encima del Grid
        boardVisualizer = new BoardVisualizer(shipLayer, CELL);
        // Configuración visual del tablero enemigo
        if (enemyLayer != null) {
            boardVisualizer.drawGrid(enemyLayer); // Dibuja líneas en el enemigo
            boardVisualizer.prepareEnemyBoard(enemyLayer); // Prepara la mira naranja
        }
        boardVisualizer.drawGrid();

        markerRenderer = new CanvasMarkerRenderer(CELL);

        placementManager = new ShipPlacementManager(this, boardVisualizer, shipLayer, CELL);

        // Crear la flota en el panel lateral
        initDraggableFleet();

        addTargetHighlight();
        numSunkShips = 0;

        updateStatsLabels();

        //Configurar botones
        btnStart.setDisable(true);
        btnStart.setOnAction(e -> startBattlePhase());
        btnReveal.setOnAction(e -> revealEnemyFleet());

        // Deshabilitar disparos hasta que empiece el juego
        enableMachineShotEvents(false);

        updateRotateButtonText();
        btnRotate.setOnAction(e -> onRotateClick());
    }

    /**
     * Crea y añade el rectángulo de resaltado que sigue el cursor del jugador
     * sobre el tablero de la máquina.
     */
    public void addTargetHighlight() {
        //Crear el cuadrado
        targetHighlight = new Rectangle(CELL, CELL); // Tamaño 50x50
        targetHighlight.setFill(Color.rgb(255, 165, 0, 0.3)); // Naranja transparente
        targetHighlight.setStroke(Color.ORANGE);
        targetHighlight.setStrokeWidth(2);
        targetHighlight.setVisible(false); // Oculto al principio
        targetHighlight.setMouseTransparent(true); // ¡Vital! Para que no bloquee tus clics

        //Agregarlo al tablero de la máquina
        machineBoard.getChildren().add(targetHighlight);

    }

    /**
     * Maneja el evento del botón de rotación.
     * Cambia la orientación del barco selecciónado o la orientación predeterminada
     */
    @FXML
    private void onRotateClick() {
        // Le pedimos al manager que cambie la orientación
        placementManager.toggleOrientation();

        // Actualizamos el texto del botón para que el usuario sepa qué va a pasar
        updateRotateButtonText();
    }

    /**
     * Actualiza el texto del botón de rotación, para retroalimentar al jugador.
     */
    private void updateRotateButtonText() {
        if (placementManager.isHorizontal()) {
            btnRotate.setText("Rotación: Horizontal ➡");
        } else {
            btnRotate.setText("Rotación: Vertical ⬇");
        }
    }

    /**
     * Devuelve un barco visualmente al panel de flota después de ser arrastrado fuera del tablero.
     * @param size El tamaño del barco a devolver (ej; 4, 3, 2, 1).
     */
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

            // Si completamos la flota, desactivamos el botón de inicio.
            btnStart.setDisable(true);
            btnStart.setText("🚀 Iniciar Batalla");
            btnStart.setStyle("");
        }
    }

    /**
     * Inicializa visualmente la flota de barcos arrastrables en el panel lateral.
     */
    private void initDraggableFleet() {
        //Limpiar contenedores antes de inicializar
        if (carrierContainer != null) carrierContainer.getChildren().clear();
        if (submarineContainer != null) submarineContainer.getChildren().clear();
        if (destroyerContainer != null) destroyerContainer.getChildren().clear();
        if (frigateContainer != null) frigateContainer.getChildren().clear();
        //Crea y añade los barcos según su tamaño
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

    /**
     * Crea el Canvas visual de un barco y lo hace arrastrable, luego lo añade al panel.
     * @param size Tamaño del barco.
     * @param targetPane El contenedor donde se añadirá el Canvas.
     */
    private void createShipInPanel(int size, Pane targetPane) {
        if (targetPane == null) return;
        // 1. Definimos un tamaño de celda
        int MENU_CELL = 35;
        // 2. Creamos el Canvas con el tamaño real final (Sin setScale)
        Canvas canvas = new Canvas(size * MENU_CELL, MENU_CELL);
        // 3. El Renderer es inteligente y se adapta al tamaño del Canvas automáticamente
        renderer.render(canvas, size);
        // 4. Configurar el arrastre
        placementManager.createDraggableShip(canvas, size);
        // 5. Agregar al panel
        targetPane.getChildren().add(canvas);
    }

    /**
     * Coloca aleatoriamente los barcos del jugador en el tablero.
     * Limpia los barcos existentes y vuelve a dibujar el tablero.
     */
    @FXML
    private void onRandomBoard() {
        playerLogical.clear();
        playerLogical.randomizeShips();
        shipLayer.getChildren().clear();
        boardVisualizer.drawGrid();
        boardVisualizer.recreateHighlight();

        // Limpiar el Panel de la Izquierda (Ya no hay barcos para arrastrar)
        if (carrierContainer != null) carrierContainer.getChildren().clear();
        if (submarineContainer != null) submarineContainer.getChildren().clear();
        if (destroyerContainer != null) destroyerContainer.getChildren().clear();
        if (frigateContainer != null) frigateContainer.getChildren().clear();
        // Dibujar los barcos en el tablero.
        drawPlayerBoardFromModel();
        // Activar botón de inicio (porque la flota ya está completa)
        checkFleetComplete();
    }

    /**
     * Dibuja los barcos del modelo lógico del jugador.
     * Configura las propiedades de arrastre y rotación.
     */
    private void drawPlayerBoardFromModel() {
        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Ship ship = playerLogical.peek(r, c).getShip();

                // Si encontramos un barco y no lo hemos dibujado aún.
                if (ship != null && !drawnShips.contains(ship)) {
                    boolean isHorizontal = false;

                    // Determinar orientación.
                    if (c + 1 < 10 && playerLogical.peek(r, c + 1).getShip() == ship) {
                        isHorizontal = true;
                    }
                    if (ship.getSize() == 1) isHorizontal = true;
                    Canvas canvas = new Canvas(ship.getSize() * CELL, CELL);
                    renderer.render(canvas, ship.getSize());
                    // Aplicar rotación y ajustes de posición para barcos verticales.
                    if (!isHorizontal) {
                        canvas.setRotate(90);
                        double offset = CELL * (1 - ship.getSize()) / 2.0;
                        canvas.setLayoutX((c * CELL) + offset);
                        canvas.setLayoutY((r * CELL) - offset);
                    } else {
                        canvas.setLayoutX(c * CELL);
                        canvas.setLayoutY(r * CELL);
                    }
                    placementManager.setupDragForPlacedShip(canvas, ship, ship.getSize());
                    shipLayer.getChildren().add(canvas);
                    drawnShips.add(ship);
                }
            }
        }
    }

    /**
     * Muestra u oculta visualmente la flota enemiga en el tablero de la máquina.
     */
    private void revealEnemyFleet() {
        if (isEnemyFleetRevealed) {
            revealLayer.getChildren().clear(); // Borra los dibujos
            isEnemyFleetRevealed = false;
            btnReveal.setText("👁 Revelar Máquina");
            return;
        }
        drawMachineBoardRealShips();
        isEnemyFleetRevealed = true;
        btnReveal.setText("🚫 Ocultar Máquina");
    }

    /**
     * Dibuja los barcos de la máquina en la capa de revelado.
     */
    private void drawMachineBoardRealShips() {
        java.util.Set<Ship> drawnShips = new java.util.HashSet<>();
        int size = machineLogical.getSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Ship ship = machineLogical.peek(r, c).getShip();
                if (ship != null && !drawnShips.contains(ship)) {
                    boolean isHorizontal = false;
                    // Determinar orientación
                    if (c + 1 < size && machineLogical.peek(r, c + 1).getShip() == ship) {
                        isHorizontal = true;
                    }
                    if (ship.getSize() == 1) isHorizontal = true;
                    Canvas canvas = new Canvas(ship.getSize() * CELL, CELL);
                    renderer.render(canvas, ship.getSize());
                    canvas.setOpacity(0.5); // Efecto "fantasma"
                    // Aplicar rotación y ajustes de posición.
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
                    autoSave();
                }
            }
        }
    }

    /**
     * Obtiene el modelo lógico del tablero del jugador.
     * @return El objeto Board del jugador.
     */
    public Board getPlayerLogical() {
        return playerLogical;
    }

    /**
     * Verifica si la flota del jugador está completa y habilita el botón de inicio de la batalla.
     */
    public void checkFleetComplete() {
        if (playerLogical.isFleetComplete()) {
            btnStart.setDisable(false);
            updateStatsLabels();
        }
    }

    /**
     * Cuenta el número de barcos que aún no están hundidos.
     * @param board El tablero lógico a inspeccionar.
     * @return El número de barcos restantes.
     */
    private int countRemainingShips(Board board) {
        Set<Ship> uniqueShips = new HashSet<>();
        int unsunkCount = 0;
        int size = board.getSize();

        // 1. Recopila todos los barcos únicos
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = board.peek(r, c);
                if (cell != null && cell.hasShip()) {
                    uniqueShips.add(cell.getShip());
                }
            }
        }
        // 2. Contar los barcos que no están hundidos.
        for (Ship ship : uniqueShips) {
            if (!ship.isSunk()) {
                unsunkCount++;
            }
        }
        return unsunkCount;
    }

    /**
     * Actualiza las etiquetas de estadísticas con el conteo actual.
     */
    private void updateStatsLabels() {
        int playerRemaining = countRemainingShips(playerLogical);
        int machineRemaining = countRemainingShips(machineLogical);

        if (lblPlayerShips != null) {
            lblPlayerShips.setText("Barcos restantes (Tú): " + playerRemaining);
        }
        if (lblMachineShips != null) {
            lblMachineShips.setText("Barcos restantes (IA): " + machineRemaining);
        }
    }

    /**
     * Transiciona el juego de la fase de colocación a la fase de batalla.
     * Deshabilita los controles de colocación y habilita los eventos de disparo.
     */
    private void startBattlePhase() {
        // Ocultar flota enemiga si estaba visible.
        if (isEnemyFleetRevealed) {
            revealEnemyFleet();
            System.out.println("⚠️ La flota enemiga se ocultó automáticamente para iniciar el juego.");
        }
        placementPhase = false;
        shipLayer.setMouseTransparent(true);

        // 1. Deshabilitar botones de edición
        btnRotate.setDisable(true);
        btnRandom.setDisable(true);
        btnReveal.setDisable(true);

        // 2. Deshabilitar los contenedores de barcos
        carrierContainer.setDisable(true);
        submarineContainer.setDisable(true);
        destroyerContainer.setDisable(true);
        frigateContainer.setDisable(true);

        btnStart.setText("¡EN COMBATE!");
        btnStart.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
        autoSave();

        // Habilitar disparos del jugador.
        enableMachineShotEvents(true);

        updateStatsLabels();
        System.out.println("⚔ ¡Comienza la batalla!");
    }

    /**
     * Habílita o deshabilita los manejadores de eventos.
     * Controla el turno del jugador.
     * @param enable True para habilitar el disparo del jugador.
     */
    private void enableMachineShotEvents(boolean enable) {
        if (!enable) {
            machineBoard.setOnMouseClicked(null);
            machineBoard.setOnMouseMoved(null);
            machineBoard.setOnMouseExited(null);
            return;
        }
        // 1. EVENTO DE MOVIMIENTO
        machineBoard.setOnMouseMoved(e -> {
            int col = (int)(e.getX() / CELL);
            int row = (int)(e.getY() / CELL);
            boardVisualizer.updateTargetHighlight(col,row);
            // Validar que esté dentro del tablero (0-9)
            if (col >= 0 && col < 10 && row >= 0 && row < 10) {
                targetHighlight.setVisible(true);
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
        // 3. EVENTO DE CLIC (Disparo del jugador)
        machineBoard.setOnMouseClicked(e -> {
            int col = (int)(e.getX() / CELL);
            int row = (int)(e.getY() / CELL);

            targetHighlight.setVisible(false);

            ShotResult result = machineLogical.shoot(row, col);
            if (result == null) return;
            autoSave();
            if (result == ShotResult.SUNK) {
                Ship sunkShip = machineLogical.peek(row, col).getShip();
                drawSunkShipGhost(sunkShip, row, col);
                markShipAsSunk(enemyLayer, machineLogical, sunkShip);
                updateStatsLabels();
                System.out.println("¡HUNDIDO! Barco destruido.");
            } else {
                paintOnPane(enemyLayer, row, col, result);
            }
            //actualizar estado  barcos

            if (machineLogical.isGameOver()) {
                System.out.println("¡VICTORIA! Has ganado.");
                handleGameOver(true);
                autoSave();
                return;
            }

                // Si fallas, turno de la Máquina
            if (result == ShotResult.MISS) {
                playMachineTurn();
            } else {
                System.out.println("¡Impacto! Sigues disparando.");
            }
        });   autoSave();
    }

    /**
     * Ejecuta el turno de la máquina.
     */
    private void playMachineTurn() {

        int[] aiShot = ai.shoot(playerLogical);
        int r = aiShot[0];
        int c = aiShot[1];

        ShotResult machineResult = playerLogical.shoot(r, c);
        if (machineResult == null) {
            playMachineTurn();
            return;
        }
        autoSave();
        if (machineResult == ShotResult.SUNK) {
            Ship sunkShip = playerLogical.peek(r, c).getShip();
            markPlayerShipAsSunk(sunkShip);
            numSunkShips++;
            updateStatsLabels();
        } else {
            paintOnPane(shipLayer, r, c, machineResult);
        }

        if (playerLogical.isGameOver()) {
            handleGameOver(false);
        } else if (machineResult != ShotResult.MISS) {
            // La IA vuelve a disparar si acierta
            playMachineTurn();
        }
    }

    /**
     * Marca visualemnte un barco de jugador como hundido.
     * @param sunkShip El objeto Ship que acaba de ser hundido.
     */
    private void markPlayerShipAsSunk(Ship sunkShip) {
        // 1. Aplicar efecto visual (Transparencia)
        applyGhostEffectToPlayerShip(sunkShip);
        // 2. Pintar el fuego encima
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (playerLogical.peek(r, c).getShip() == sunkShip) {
                    paintOnPane(shipLayer, r, c, ShotResult.SUNK);
                }
            }
        }
    }

    /**
     * Dibuja un marcador visual, en una celda específica.
     * @param layer La capa donde se dibujará.
     * @param row La fila del disparo
     * @param col La columna del disparo
     * @param result El resultado del disparo.
     */
    private void paintOnPane(Pane layer, int row, int col, ShotResult result) {
        Canvas marker = null;

        switch (result) {
            case MISS -> marker = markerRenderer.drawMiss(); // O markerRenderer si cambiaste el nombre
            case HIT  -> marker = markerRenderer.drawHit();
            case SUNK -> marker = markerRenderer.drawSunk();
        }

        if (marker != null) {
            marker.setMouseTransparent(true);
            marker.setLayoutX(col * CELL);
            marker.setLayoutY(row * CELL);
            layer.getChildren().add(marker);
            marker.toFront();
        }
    }

    /**
     * Marca todas las celdas ocupadas por un barco hundido con el marcador SUNK (fuego).
     * @param layer La capa visual (Pane) donde pintar.
     * @param board El tablero lógico donde buscar el barco.
     * @param sunkShip El barco hundido.
     */
    private void markShipAsSunk(Pane layer, Board board, Ship sunkShip) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (board.peek(r, c).getShip() == sunkShip) {
                    paintOnPane(layer, r, c, ShotResult.SUNK);
                }
            }
        }
    }

    /**
     * Dibuja una representación visual de un barco de la máquina que ha sido hundido
     * en la capa de revelación.
     * @param ship El barco de la máquina hundido.
     * @param r Fila de una de las celdas del barco (se usa para encontrar el inicio).
     * @param c Columna de una de las celdas del barco (se usa para encontrar el inicio).
     */
    private void drawSunkShipGhost(Ship ship, int r, int c) {
        boolean isHorizontal = false;
        if (ship.getSize() == 1) {
            isHorizontal = true;
        } else {
            // Miramos si la celda de la derecha o izquierda tiene el mismo barco
            // Ojo: hay que validar límites del tablero
            if (c + 1 < 10 && machineLogical.peek(r, c + 1).getShip() == ship) isHorizontal = true;
            if (c - 1 >= 0 && machineLogical.peek(r, c - 1).getShip() == ship) isHorizontal = true;
        }

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

        revealLayer.getChildren().add(canvas);
    }

    /**
     * Muestra el mensaje de fin de juego y deshabilita la interacción.
     * @param playerWon true si el jugador ganó, false si perdió.
     */
    private void handleGameOver(boolean playerWon) {

        isGameFinished = true;
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

            // Si pierde, muestra dónde estaban los barcos enemigos que faltaron
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

        SaveManager.deleteSaves();
        alert.show();
        updateStatsLabels();
    }

    /**
     * Establece el apodo del jugador.
     * @param playerNickname El apodo a establecer.
     */
    public void setPlayerNickname(String playerNickname) {
        this.playerNickname = playerNickname;
    }

    /**
     * Guarda automáticamente el estado actual de ambos tableros.
     */
    private void autoSave() {
        // Si el juego terminó, no guardes nada.
        if (isGameFinished) return;

        SaveManager.saveBoard(playerLogical, "player_board.ser");
        SaveManager.saveBoard(machineLogical, "machine_board.ser");
        SaveManager.savePlayerInfo(playerNickname, numSunkShips,placementPhase);
    }

    /**
     * Establece el estado de la fase actual del juego.
     * @param placementPhase true si es la fase de colocación, false si es la fase de batalla.
     */
    public void setPlacementPhase(boolean placementPhase){
        this.placementPhase= placementPhase;
    }

    /**
     * Carga un estado de juego guardado y configura la interfaz de usuario en consecuencia.
     * @param player El modelo lógico del tablero del jugador cargado.
     * @param machine El modelo lógico del tablero de la máquina cargado.
     * @param data Los datos del jugador cargados (nickname, barcos hundidos, fase).
     */
    public void loadGame(Board player, Board machine, PlayerData data) {
        this.playerLogical = player;
        this.machineLogical = machine;
        this.playerNickname = data.getNickname();
        this.numSunkShips = data.getSunkShips();
        this.placementPhase = data.isPlacementPhase();

        lblPlayerName.setText("ALMIRANTE " + playerNickname.toUpperCase());
        redrawBoards();

        // CONFIGURAR ESTADO DEL JUEGO
        if (placementPhase) {
            initDraggableFleet();
            shipLayer.setMouseTransparent(false);

            // Habilitar controles
            btnRotate.setDisable(false);
            btnRandom.setDisable(false);
            btnReveal.setDisable(false);

            // Habilitar contenedores
            carrierContainer.setDisable(false);
            submarineContainer.setDisable(false);
            destroyerContainer.setDisable(false);
            frigateContainer.setDisable(false);

            btnStart.setDisable(!playerLogical.isFleetComplete());
            btnStart.setText("🚀 Iniciar Batalla");
            btnStart.setStyle(""); // Estilo original

            // Desactivar disparos (aún no empieza)
            enableMachineShotEvents(false);

        } else {

            // 1. Limpiar panel izquierdo (ya no hay barcos para poner)
            carrierContainer.getChildren().clear();
            submarineContainer.getChildren().clear();
            destroyerContainer.getChildren().clear();
            frigateContainer.getChildren().clear();

            // 2. Bloquear interacciones de colocación
            shipLayer.setMouseTransparent(true);

            // 3. BLOQUEAR BOTONES (Incluido el de Revelar)
            btnRotate.setDisable(true);
            btnRandom.setDisable(true);
            btnReveal.setDisable(true); // <--- ¡ESTO FALTABA!

            // 4. Bloquear contenedores (estilo visual opaco)
            carrierContainer.setDisable(true);
            submarineContainer.setDisable(true);
            destroyerContainer.setDisable(true);
            frigateContainer.setDisable(true);

            // 5. Configurar botón de inicio como "En Combate"
            btnStart.setDisable(true);
            btnStart.setText("¡EN COMBATE!");
            btnStart.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");

            // 6. ACTIVAR DISPAROS
            enableMachineShotEvents(true);
        }

        updateStatsLabels();
    }

    /**
     * Redibuja completamente ambos tableros, incluyendo barcos y marcadores de disparo,
     * basado en el estado lógico cargado.
     */
    private void redrawBoards() {
        playerBoard.getChildren().clear();
        machineBoard.getChildren().clear();
        shipLayer.getChildren().clear();
        enemyLayer.getChildren().clear();
        revealLayer.getChildren().clear();

        // RESTAURAR TABLERO JUGADOR
        drawPlayerBoardFromModel();
        boardVisualizer.drawGrid();
        boardVisualizer.recreateHighlight();

        // RESTAURAR TABLERO ENEMIGO
        boardVisualizer.drawGrid(enemyLayer);

        // Volver a crear y agregar la mira naranja
        addTargetHighlight();

        // RESTAURAR DISPAROS (X, Bombas, Fuego)
        restoreShotsVisuals(playerLogical, shipLayer);
        restoreShotsVisuals(machineLogical, enemyLayer);

        // 5. LÓGICA DE REVELADO
        if (isEnemyFleetRevealed) {
            drawMachineBoardRealShips();
            btnReveal.setText("🚫 Ocultar Máquina");
        } else {
            btnReveal.setText("👁 Revelar Máquina");
        }
    }

    /**
     * Aplica un efecto visual de semitransparencia (fantasma) al Canvas
     * que representa al barco del jugador hundido.
     * @param ship El barco del jugador hundido.
     */
    private void applyGhostEffectToPlayerShip(Ship ship) {
        int startR = -1, startC = -1;
        boolean found = false;

        // 1. Encontrar la cabeza del barco lógico
        for (int r = 0; r < 10 && !found; r++) {
            for (int c = 0; c < 10 && !found; c++) {
                if (playerLogical.peek(r, c).getShip() == ship) {
                    startR = r;
                    startC = c;
                    found = true;
                }
            }
        }

        if (found) {
            // 2. Calcular posición visual esperada
            boolean isHorizontal = false;
            if (ship.getSize() == 1) isHorizontal = true;
            else if (startC + 1 < 10 && playerLogical.peek(startR, startC + 1).getShip() == ship) isHorizontal = true;

            double expectedX, expectedY;
            if (isHorizontal) {
                expectedX = startC * CELL;
                expectedY = startR * CELL;
            } else {
                double offset = CELL * (1 - ship.getSize()) / 2.0;
                expectedX = (startC * CELL) + offset;
                expectedY = (startR * CELL) - offset;
            }

            // 3. Buscar el Canvas y bajarle la opacidad
            for (javafx.scene.Node node : shipLayer.getChildren()) {
                if (node instanceof Canvas canvas &&
                        Math.abs(canvas.getLayoutX() - expectedX) < 1.0 &&
                        Math.abs(canvas.getLayoutY() - expectedY) < 1.0) {

                    canvas.setOpacity(0.5); // ¡EFECTO FANTASMA!
                    canvas.setMouseTransparent(true);
                    break;
                }
            }
        }
    }

    /**
     * Restaura los marcadores de disparo.
     * @param logicalBoard El tablero lógico
     * @param targetLayer La capa visual
     */
    private void restoreShotsVisuals(Board logicalBoard, Pane targetLayer) {
        Set<Ship> restoredShips = new HashSet<>();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Cell cell = logicalBoard.peek(r, c);

                if (cell.isShot()) {
                    ShotResult result;
                    if (cell.isEmpty()) result = ShotResult.MISS;
                    else if (cell.getShip().isSunk()) result = ShotResult.SUNK;
                    else result = ShotResult.HIT;

                    // 1. Pintar fuego/bomba/agua
                    paintOnPane(targetLayer, r, c, result);

                    // 2. RESTAURAR EFECTO FANTASMA (Solo una vez por barco)
                    if (result == ShotResult.SUNK) {
                        Ship ship = cell.getShip();

                        if (!restoredShips.contains(ship)) {
                            // CASO A: Tablero Enemigo (Dibujamos nuevo fantasma)
                            if (targetLayer == enemyLayer) {
                                drawSunkShipGhost(ship, r, c);
                            }
                            // CASO B: Tablero Jugador (Volvemos transparente el existente)
                            else if (targetLayer == shipLayer) {
                                applyGhostEffectToPlayerShip(ship);
                            }

                            restoredShips.add(ship);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adjunta un manejador al evento de cierre de la ventana (Stage).
     * Garantiza que el estado actual del juego se guarde automáticamente
     * antes de que la ventana principal sea cerrada por el usuario (ej. clic en 'X').
     * @param stage El objeto Stage principal de la aplicación.
     */
    public void attachCloseHandler(Stage stage) {
        stage.setOnCloseRequest(e -> {
            autoSave();
            System.out.println("💾 Juego guardado al cerrar.");
        });
    }

    /**
     * Maneja la acción de volver al menú principal.
     * Muestra una alerta de confirmación, guarda automáticamente la partida si no ha terminado,
     * cierra la ventana actual del juego y abre la vista del menú principal.
     * @throws IOException Si ocurre un error al cargar la vista del menú principal.
     */
    @FXML
    private void onBackToMenu() throws IOException {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Volver al Menú");
        alert.setHeaderText("¿Estás seguro de que deseas salir?");
        alert.setContentText("No te preocupes, los datos de la partida se guardarán automáticamente.");
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            // Guardamos antes de salir (si el juego no ha terminado)
            if (!isGameFinished) {
                autoSave();
            }
            // Cerramos la ventana actual
            Stage stage = (Stage) btnStart.getScene().getWindow();
            stage.close();
            // Abrimos el menú principal
            new com.example.batallanaval.views.WelcomeView().show();
        }
    }


}
