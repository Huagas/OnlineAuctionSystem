package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        messageLabel.setText("");
        User loggedInUser = UserService.login(username, password);

        if (loggedInUser != null) {
            String role = loggedInUser.getRole();
            System.out.println("Đăng nhập thành công: " + username + " | Vai trò: " + role);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/profile-view.fxml"));
                Parent root = loader.load();
                ProfileController profileController = loader.getController();
                profileController.initData(loggedInUser);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
                stage.show();
                /*
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader;
                Scene scene;

                if (role.equals("Seller")) {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("Đang chuyển hướng đến Kênh Người Bán...");

                    loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/seller-dashboard.fxml"));
                    scene = new Scene(loader.load(), 1200, 768);

                    SellerDashboardController controller = loader.getController();
                    controller.initData(loggedInUser);

                    stage.setTitle("Kênh Người Bán - Lạc Việt Auction");

                } else if (role.equals("Bidder")) {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("Đang vào phòng Đấu giá...");

                    loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/bidder-dashboard.fxml"));
                    scene = new Scene(loader.load(), 1280, 800);

                    BidderDashboardController controller = loader.getController();
                    controller.initData(loggedInUser);

                    stage.setTitle("Sàn Đấu Giá Trực Tuyến - Lạc Việt Auction");

                } else {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("Admin Dashboard đang được xây dựng...");
                    return;
                }

                stage.setScene(scene);
                stage.centerOnScreen();
                 */
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình tiếp theo: " + e.getMessage());
            }
        } else {
            messageLabel.setText("Sai tên đăng nhập hoặc mật khẩu. Vui lòng thử lại!");
            messageLabel.setStyle("-fx-text-fill: red;");
            passwordField.clear();
        }
    }

    @FXML
    protected void handleGoToRegister(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/register-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene registerScene = new Scene(loader.load(), 850, 800);
        stage.setScene(registerScene);
        stage.setTitle("Hệ thống Đấu giá Trực tuyến - Đăng ký");
        stage.centerOnScreen();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}