package com.example.batallanaval.controllers;

import com.example.batallanaval.models.Board;
import com.example.batallanaval.models.PlayerData;
import com.example.batallanaval.persistence.SaveManager;
import com.example.batallanaval.views.GameView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;

public class WelcomeController {

    // ======================
    // FXML
    // ======================

    @FXML private StackPane rootStack;
    @FXML private ImageView backgroundView;

    @FXML private Button btnContinue;
    @FXML private Button btnNew;
    @FXML private Button btnExit;
    @FXML private Button buttonHelp;

    @FXML private HBox newGameBox;
    @FXML private TextField nameField;
    @FXML private Button btnAccept;

    // ======================
    // INIT
    // ======================
    @FXML
    public void initialize() {

        // --- 1. LÓGICA DE FONDO RESPONSIVE (EL ARREGLO MÁGICO) ---
        if (rootStack != null && backgroundView != null) {
            // Esto "ata" el ancho/alto de la imagen al de la ventana
            backgroundView.fitWidthProperty().bind(rootStack.widthProperty());
            backgroundView.fitHeightProperty().bind(rootStack.heightProperty());
        }

        // VERIFICAR SI HAY PARTIDA GUARDADA
        // Si no existe el archivo, deshabilitamos el botón de Continuar
        if (SaveManager.loadPlayerInfo() == null) {
            btnContinue.setDisable(true);
        }

        // Ocultar sección de nuevo juego al inicio
        newGameBox.setVisible(false);
        newGameBox.setManaged(false);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            nameField.getStyleClass().remove("error");
        });

        // NUEVO JUEGO → mostrar campo nombre
        btnNew.setOnAction(e -> {
            newGameBox.setVisible(true);
            newGameBox.setManaged(true);
            nameField.requestFocus();
        });

        // ACEPTAR (botón o ENTER)
        btnAccept.setOnAction(e -> startNewGame());
        nameField.setOnAction(e -> startNewGame());

        // CONTINUAR
        btnContinue.setOnAction(e -> continueGame());

        // SALIR
        btnExit.setOnAction(e -> {
            Stage stage = (Stage) btnExit.getScene().getWindow();
            stage.close();
        });
    }

    // ======================
    // NUEVO JUEGO (CON VALIDACIÓN)
    // ======================
    private void startNewGame() {
        String nickname = nameField.getText().trim();

        // --- VALIDACIÓN: Mínimo 3 caracteres ---
        if (nickname.length() < 3) {
            // 1. Poner borde rojo al campo de texto
            if (!nameField.getStyleClass().contains("error")) {
                nameField.getStyleClass().add("error");
            }

            // 2. Mostrar alerta pequeña
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Nombre muy corto");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, ingresa un nombre de al menos 3 caracteres.");
            alert.show();

            // 3. ¡IMPORTANTE! Return para que NO inicie el juego
            return;
        }

        // Si pasa la validación, quitamos el estilo de error (por si acaso)
        nameField.setStyle(null);

        // --- LÓGICA DE CREACIÓN DEL JUEGO ---
        Board playerBoard = new Board();
        Board machineBoard = new Board();
        machineBoard.randomizeShips();

        // Guardado inicial
        SaveManager.saveBoard(playerBoard, "player_board.ser");
        SaveManager.saveBoard(machineBoard, "machine_board.ser");
        SaveManager.savePlayerInfo(nickname, 0, true);

        PlayerData data = new PlayerData(nickname, 0, true);
        openGame(playerBoard, machineBoard, data);
    }

    // ======================
    // CONTINUAR JUEGO
    // ======================
    private void continueGame() {

        Board player = SaveManager.loadBoard("player_board.ser");
        Board machine = SaveManager.loadBoard("machine_board.ser");
        PlayerData data = SaveManager.loadPlayerInfo();

        if (player == null || machine == null || data == null) {
            System.out.println("❌ No hay partida guardada.");
            return;
        }

        openGame(
                player,
                machine,
                data
        );
    }

    // ======================
    // ABRIR JUEGO
    // ======================
    private void openGame(Board player,
                          Board machine,
                       PlayerData data) {

        try {
            GameView gameView = new GameView();
            GameController controller = gameView.getGameController();

            // 🔹 Cargar estado
            controller.loadGame(player, machine, data);
            controller.setPlacementPhase(data.isPlacementPhase());

            // 🔹 Guardar al cerrar
            controller.attachCloseHandler(gameView);

            // 🔹 Mostrar juego
            gameView.show();

            // 🔹 Cerrar Welcome
            Stage welcomeStage = (Stage) btnNew.getScene().getWindow();
            welcomeStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onHelpButtonClick() {
        showInstructionsDialog();
    }

    /**
     * Muestra un diálogo modal con las instrucciones del juego.
     */
    private void showInstructionsDialog() {
        Alert alert = new javafx.scene.control.Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instrucciones de Batalla Naval");
        alert.setHeaderText("¡Bienvenido al campo de batalla, Almirante!");

        String content = """
        🌊 \tFASE 1: COLOCACIÓN DE BARCOS
        
        \t1. Flota: Tienes 10 barcos de diferentes tamaños:
        \t\t• 1 Acorazado (4 celdas)
        \t\t• 2 Cruceros (3 celdas)
        \t\t• 3 Destructores (2 celdas)
        \t\t• 4 Submarinos (1 celda)
        \t2. Posicionamiento: Arrastra los barcos desde el panel izquierdo a tu tablero (el de abajo).
        \t3. Rotación: Usa el botón "Rotación" para cambiar la orientación (Horizontal/Vertical) del barco seleccionado o antes de colocar uno.
        \t4. Opciones: Usa "Flota Aleatoria" para colocar todos los barcos automáticamente.
        \t5. Inicio: El botón "Iniciar Batalla" se habilitará cuando todos los 10 barcos estén colocados.

        💥 \tFASE 2: BATALLA
        
        \t1. Tu Turno: Haz clic en el tablero de la Máquina (el de arriba) para disparar.
        \t\t• 💧 Agua (Miss): Se marca con una 'X' o círculo azul. Turno de la IA.
        \t\t• 💣 Impacto (Hit): Se marca con una bomba (que refleja impacto). ¡Obtienes otro turno!
        \t\t• 🚢 Hundido (Sunk): El barco se marca con fuego. ¡Obtienes otro turno!
        \t2. Turno de la IA: Si fallas, es el turno de la Máquina. La IA disparará a tu tablero.
        \t3. Fin del Juego: El juego termina cuando la flota de un jugador ha sido completamente hundida.
        """;

        alert.setContentText(content);
        alert.setResizable(true);
        alert.showAndWait();
    }
}
