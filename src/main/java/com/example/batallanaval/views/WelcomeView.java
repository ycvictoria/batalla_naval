package com.example.batallanaval.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeView extends Stage {
    public WelcomeView() throws IOException {
        System.out.println(getClass().getResource("/com/example/batallanaval/welcome-view.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/com/example/batallanaval/welcome-view.fxml")
        );
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setScene(scene);
        this.setResizable(false);
        this.setTitle("Batalla Naval - Menú Principal");
        this.centerOnScreen();


    }
}
