package com.example.batallanaval.controllers;

import com.example.batallanaval.models.Board;
import com.example.batallanaval.models.PlayerData;
import com.example.batallanaval.persistence.SaveManager;
import com.example.batallanaval.views.GameView;
import javafx.fxml.FXML;
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
}
