package com.example.batallanaval;

import com.example.batallanaval.views.WelcomeView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La clase principal de la aplicación.
 * Extiende Application y contiene el método 'start' para inicializar la interfaz.
 */
public class MainApp extends Application {

    /**
     * El método start es el punto de entrada principal para todas las aplicaciones JavaFX.
     * Se llama después de que el sistema JavaFX está listo.
     * @param stage El Stage principal (ventana) proporcionado por el sistema JavaFX.
     * @throws IOException Si ocurre un error al cargar la WelcomeView (ej. el FXML no se encuentra).
     */
    @Override
    public void start(Stage stage) throws IOException {
        WelcomeView welcomeView = new WelcomeView();
        welcomeView.show();
    }
}
