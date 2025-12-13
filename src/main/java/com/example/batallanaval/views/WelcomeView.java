package com.example.batallanaval.views;

import com.example.batallanaval.controllers.WelcomeController;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeView extends Stage {

    public WelcomeController welcomeController;

    public WelcomeView() throws IOException {

        //System.out.println(getClass().getResource("/com/example/batallanaval/welcome-view.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/com/example/batallanaval/welcome-view.fxml")
        );
        Parent root = fxmlLoader.load();
        welcomeController = fxmlLoader.getController();

        Scene scene = new Scene(root, 1000, 700);

        this.setTitle("Batalla Naval - Menú Principal");
        this.setScene(scene);
        // Tamaño mínimo decente
        this.setMinWidth(1000);
        this.setMinHeight(700);

        // Tamaño normal
        this.setWidth(1000);
        this.setHeight(700);
        // Abre la ventana maximizada
        this.setMaximized(true);

        /*this.maximizedProperty().addListener((observable, wasMaximized, isNowMaximized) -> {
            // Si isNowMaximized es false, significa que el usuario le dio a "Restaurar"
            if (!isNowMaximized) {
                // Usamos runLater para esperar a que la ventana termine de cambiar de tamaño
                javafx.application.Platform.runLater(() -> {
                    this.centerOnScreen();
                });
            }
        });*/
    }

    public WelcomeController getWelcomeController() {
        return welcomeController;
    }
}
