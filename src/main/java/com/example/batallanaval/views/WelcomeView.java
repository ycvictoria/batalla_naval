package com.example.batallanaval.views;

import com.example.batallanaval.controllers.WelcomeController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeView extends Stage {
    public WelcomeController welcomeController;
    public WelcomeView() throws IOException {
        System.out.println(getClass().getResource("/com/example/batallanaval/welcome-view.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/com/example/batallanaval/welcome-view.fxml")
        );
        welcomeController = fxmlLoader.getController();
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setScene(scene);
        this.setMaximized(true);
        this.setTitle("Batalla Naval - Menú Principal");
        this.centerOnScreen();

    }

    public WelcomeController getWelcomeController() {
        return welcomeController;
    }
}
