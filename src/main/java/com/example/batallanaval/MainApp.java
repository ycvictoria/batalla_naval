package com.example.batallanaval;

import com.example.batallanaval.views.WelcomeView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        WelcomeView welcomeView = new WelcomeView();
        welcomeView.show();
    }
}
