package com.example.batallanaval;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setMinWidth(350);
        stage.setMinHeight(350);
        stage.setResizable(true);
        stage.setTitle("Batalla Naval");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
