package com.example.batallanaval.views;

import com.example.batallanaval.controllers.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameView extends Stage {

    private GameController gameController;

    public GameView() throws IOException {
        this.setTitle("Batalla Naval Game");
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/com/example/batallanaval/game-view.fxml")
        );
        Parent root = fxmlLoader.load();
        gameController = fxmlLoader.getController();
        Scene scene = new Scene(root);
        this.setMaxHeight(600);
        this.setMaxWidth(600);
        this.setScene(scene);

    }

    public GameController getGameController() {
        return gameController;
    }

    public static GameView getInstance() throws IOException {
        if (GameViewHolder.INSTANCE == null) {
            GameViewHolder.INSTANCE = new GameView();
        }
        return GameViewHolder.INSTANCE;
    }


    private static class GameViewHolder {
        private static GameView INSTANCE = null;
    }
}