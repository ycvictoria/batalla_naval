package com.example.batallanaval.views;

import com.example.batallanaval.controllers.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * GameView es la ventana principal (Stage) de la aplicación Batalla Naval.
 * Utiliza el patrón Singleton para asegurar una única instancia de la ventana del juego.
 * Se encarga de cargar el layout FXML y obtener el controlador asociado (GameController).
 */
public class GameView extends Stage {

    private GameController gameController;

    /**
     * Constructor privado que inicializa el Stage, carga el FXML y configura la ventana.
     * @throws IOException Si el archivo FXML no puede ser cargado.
     */
    public GameView() throws IOException {
        this.setTitle("Batalla Naval Game");
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/com/example/batallanaval/game-view.fxml")
        );
        Parent root = fxmlLoader.load();
        gameController = fxmlLoader.getController();
        Scene scene = new Scene(root);
        // Tamaño mínimo decente
        this.setMinWidth(1000);
        this.setMinHeight(700);
        // Abre la ventana maximizada
        this.setMaximized(true);
        this.setScene(scene);

    }

    /**
     * Obtiene la instancia del GameController asociado a esta vista.
     * @return El GameController.
     */
    public GameController getGameController() {
        return gameController;
    }

    /**
     * Implementación del patrón Singleton para obtener la única instancia de GameView.
     * @return La única instancia de GameView.
     * @throws IOException Si la instancia debe ser creada y el FXML falla al cargar.
     */
    public static GameView getInstance() throws IOException {
        if (GameViewHolder.INSTANCE == null) {
            GameViewHolder.INSTANCE = new GameView();
        }
        return GameViewHolder.INSTANCE;
    }

    /**
     * Clase interna estática para mantener la instancia Singleton de forma lazy y thread-safe.
     */
    private static class GameViewHolder {
        private static GameView INSTANCE = null;
    }
}