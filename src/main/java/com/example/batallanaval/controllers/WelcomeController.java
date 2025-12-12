package com.example.batallanaval.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
//import com.example.batallanaval.save.SaveManager;

public class WelcomeController {

    @FXML private Button btnContinue;
    @FXML private Button btnNew;
    @FXML private Button btnExit;

    @FXML
    public void initialize() {

        //btnContinue.setDisable(!SaveManager.getInstance().hasSave());

        btnContinue.setOnAction(e -> loadGame());
        btnNew.setOnAction(e -> newGame());
        btnExit.setOnAction(e -> System.exit(0));
    }

    private void newGame() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo juego");
        dialog.setHeaderText("Ingresa tu nickname");
        dialog.setContentText("Nombre:");

        dialog.showAndWait().ifPresent(nick -> {
            // Guardas nickname inicial
           // SaveManager.getInstance().savePlayerInfo(nick, 0, 0);
            openGame(false);
        });
    }

    private void loadGame() {
        openGame(true);
    }

    private void openGame(boolean load) {
        // aquí carga el Game.fxml (tu juego actual)
        // y  si debe cargar o iniciar

    }
}
