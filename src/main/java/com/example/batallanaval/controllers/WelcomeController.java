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

public class WelcomeController {

    // ======================
    // FXML
    // ======================
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

        // Ocultar sección de nuevo juego al inicio
        newGameBox.setVisible(false);
        newGameBox.setManaged(false);

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
    // NUEVO JUEGO
    // ======================
    private void startNewGame() {

        String nickname = nameField.getText().trim();
        if (nickname.isEmpty()) {
            nickname = "Jugador";
        }

        Board playerBoard = new Board();
        Board machineBoard = new Board();
        machineBoard.randomizeShips();

        // 🔹 Guardado inicial
        SaveManager.saveBoard(playerBoard, "player_board.ser");
        SaveManager.saveBoard(machineBoard, "machine_board.ser");

        // 🔹 Nuevo juego → 0 hundidos, EN colocación
        SaveManager.savePlayerInfo(nickname, 0, true);
        PlayerData data = new PlayerData(nickname, 0,true);
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
