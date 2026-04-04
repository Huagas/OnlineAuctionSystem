package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {
    @FXML private Button btnBidder;
    @FXML private Button btnSeller;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML private Label nameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;

    @FXML private CheckBox termsCheckBox;
    private String selectedRole = "Bidder";

    @FXML
    public void initialize() {
        usernameField.textProperty().addListener((obs, old, newValue) -> clearError(usernameField, nameErrorLabel));
        passwordField.textProperty().addListener((obs, old, newValue) -> clearError(passwordField, passwordErrorLabel));
        confirmPasswordField.textProperty().addListener((obs, old, newValue) -> clearError(confirmPasswordField, confirmPasswordErrorLabel));
        emailField.textProperty().addListener((obs, old, newValue) -> clearError(emailField, emailErrorLabel));
        phoneField.textProperty().addListener((obs, old, newValue) -> clearError(phoneField, phoneErrorLabel));
    }

    private boolean checkPasswordStrength(String password) {
        boolean hasLength = false;
        boolean hasUpper = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        if (password.length() >= 8) {
            hasLength = true;
        }

        if (password.matches(".*[A-Z].*")) {
            hasUpper = true;
        }

        if (password.matches(".*\\d.*")) {
            hasNumber = true;
        }

        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            hasSpecial = true;
        }

        return hasLength && hasUpper && hasNumber && hasSpecial;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void handleToggleRole(ActionEvent event) {
        Button clickButton = (Button) event.getSource();
        if (clickButton.getStyleClass().contains("toggle-btn-active")) {
            return;
        }

        if (clickButton == btnBidder) {
            selectedRole = "Bidder";
            updateToggleStyle(btnBidder, btnSeller);
        } else {
            selectedRole = "Seller";
            updateToggleStyle(btnSeller, btnBidder);
        }
    }

    private void updateToggleStyle(Button activeBtn, Button inactiveBtn) {
        activeBtn.getStyleClass().remove("toggle-btn-inactive");
        if (!activeBtn.getStyleClass().contains("toggle-btn-active")) {
            activeBtn.getStyleClass().add("toggle-btn-active");
        }

        inactiveBtn.getStyleClass().remove("toggle-btn-active");
        if (!inactiveBtn.getStyleClass().contains("toggle-btn-inactive")) {
            inactiveBtn.getStyleClass().add("toggle-btn-inactive");
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (usernameField.getText().isEmpty()) {
            showError(usernameField, nameErrorLabel, "Họ và tên không được để trống.");
            isValid = false;
        } else {
            clearError(usernameField, nameErrorLabel);
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError(emailField, emailErrorLabel, "Email không hợp lệ.");
            isValid = false;
        } else {
            clearError(emailField, emailErrorLabel);
        }

        if (!phoneField.getText().matches("^0[35789][0-9]{8}$")) {
            showError(phoneField, phoneErrorLabel, "Số điện thoại không hợp lệ.");
            isValid = false;
        } else {
            clearError(phoneField, phoneErrorLabel);
        }

        if (!checkPasswordStrength(passwordField.getText())) {
            showError(passwordField, passwordErrorLabel, "Mật khẩu chưa đạt yêu cầu.");
            isValid = false;
        } else {
            clearError(passwordField, passwordErrorLabel);
        }

        if (!confirmPasswordField.getText().equals(passwordField.getText())) {
            showError(confirmPasswordField, confirmPasswordErrorLabel, "Mật khẩu xác nhận không khớp.");
            isValid = false;
        } else {
            clearError(confirmPasswordField, confirmPasswordErrorLabel);
        }

        if (!termsCheckBox.isSelected()) {
            termsCheckBox.getStyleClass().add("terms-checkbox-error");
            isValid = false;
        } else {
            termsCheckBox.getStyleClass().remove("terms-checkbox-error");
        }

        return isValid;
    }

    private void showError(Control input, Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!input.getStyleClass().contains("modern-input-error")) {
            input.getStyleClass().add("modern-input-error");
        }
    }

    private void clearError(Control input, Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        input.getStyleClass().remove("modern-input-error");
    }

    @FXML
    protected void handleRegister(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        boolean isRegistered = UserService.register(
                usernameField.getText(),
                passwordField.getText(),
                emailField.getText(),
                phoneField.getText(),
                selectedRole
        );

        if (isRegistered) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký thành công! Đang quay lại trang đăng nhập...");
            try {
                handleGoToLogin(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email hoặc số điện thoại đã tồn tại!");
        }
    }

    @FXML
    protected void handleGoToLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/login-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Scene loginScene = new Scene(loader.load(), 450, 550);
        stage.setScene(loginScene);
        stage.setTitle("Hệ thống Đấu giá Trực tuyến - Đăng nhập");
        stage.centerOnScreen();
    }
}