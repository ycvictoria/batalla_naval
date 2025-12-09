package com.example.batallanaval;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("game-view2.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

       stage.setWidth(screenBounds.getWidth() * 0.6);  // % del ancho
        stage.setHeight(screenBounds.getHeight() * 0.6); // % del alto
        //stage.setWidth(1000);
        //stage.setHeight(600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
