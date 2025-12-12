package com.example.batallanaval.controllers;

import com.example.batallanaval.views.GameView;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
//import com.example.batallanaval.save.SaveManager;

public class WelcomeController {

    @FXML private Button btnContinue;
    @FXML private Button btnNew;
    @FXML private Button btnExit;

    @FXML
    public void initialize() {

        //btnContinue.setDisable(!SaveManager.getInstance().hasSave());

        btnContinue.setOnAction(e -> loadGame(e));
        btnNew.setOnAction(e -> newGame(e));
        btnExit.setOnAction(e -> System.exit(0));
    }

    private void newGame(ActionEvent e) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo juego");
        dialog.setHeaderText("Ingresa tu nickname");
        dialog.setContentText("Nombre:");

        dialog.showAndWait().ifPresent(nick -> {
            // Guardas nickname inicial
           // SaveManager.getInstance().savePlayerInfo(nick, 0, 0);

            openGame(e);
        });
    }

    private void loadGame(ActionEvent e) {
        openGame(e);
    }

    private void openGame(ActionEvent e)  {
        // aquí carga el Game.fxml (tu juego actual)
        // y  si debe cargar o iniciar
        try{

            Node sourceNode = (Node)e.getSource();
            Scene scene = sourceNode.getScene();
            Stage stage = (Stage)scene.getWindow();
            stage.close();
            GameView gameView= new GameView();
            gameView.getGameController().initialize();
            gameView.show();

        }catch(Exception exc){
            //System.out.println("Error abriendo juego");
            exc.printStackTrace();
        }

    }
}
