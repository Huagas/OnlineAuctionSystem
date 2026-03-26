package com.example.onlineauctionsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/example/onlineauctionsystem/views/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 550);
        stage.setTitle("Hệ thống Đấu giá Trực tuyến - Đăng nhập");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
