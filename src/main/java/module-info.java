module com.example.batallanaval {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;



    opens com.example.batallanaval.controllers to javafx.fxml;
    opens com.example.batallanaval to javafx.graphics;
    exports com.example.batallanaval;
    exports com.example.batallanaval.controllers;
}