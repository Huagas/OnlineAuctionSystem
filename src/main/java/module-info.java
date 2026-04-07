module com.example.onlineauctionsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.onlineauctionsystem to javafx.fxml;
    exports com.example.onlineauctionsystem;
    exports com.example.onlineauctionsystem.controllers;
    opens com.example.onlineauctionsystem.controllers to javafx.fxml;
    opens com.example.onlineauctionsystem.models to javafx.base;
}