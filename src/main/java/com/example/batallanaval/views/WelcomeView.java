package com.example.batallanaval.views;

import com.example.batallanaval.controllers.WelcomeController;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Representa la ventana principal (Stage) de la aplicación, que carga
 * la vista de bienvenida desde el archivo FXML y recupera su controlador.
 */
public class WelcomeView extends Stage {

    public WelcomeController welcomeController;

    /**
     * Constructor que inicializa el Stage cargando la vista FXML.
     * @throws IOException Si el archivo FXML no se puede cargar.
     */
    public WelcomeView() throws IOException {
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

    }

    /**
     * Devuelve el controlador asociado a la vista de bienvenida.
     * @return El WelcomeController.
     */
    public WelcomeController getWelcomeController() {
        return welcomeController;
    }
}
