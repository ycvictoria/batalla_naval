module com.example.batallanaval {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.batallanaval.controllers to javafx.fxml;
    exports com.example.batallanaval;
}