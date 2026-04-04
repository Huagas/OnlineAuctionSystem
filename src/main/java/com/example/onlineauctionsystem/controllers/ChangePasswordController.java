package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ChangePasswordController {
    @FXML private PasswordField oldPassField;
    @FXML private PasswordField newPassField;
    @FXML private PasswordField confirmPassField;

    @FXML private Label oldPassErrorLabel;
    @FXML private Label newPassErrorLabel;
    @FXML private Label confirmPassErrorLabel;

    private User currentUser;

    @FXML
    public void initialize() {
        oldPassField.textProperty().addListener((obs, old, newValue) -> clearError(oldPassField, oldPassErrorLabel));
        newPassField.textProperty().addListener((obs, old, newValue) -> clearError(newPassField, newPassErrorLabel));
        confirmPassField.textProperty().addListener((obs, old, newValue) -> clearError(confirmPassField, confirmPassErrorLabel));
    }

    public void initData(User user) {
        currentUser = user;
    }

    @FXML
    private void handNext(ActionEvent event) {
        String oldPass = oldPassField.getText();
        String newPass = newPassField.getText();
        String confirmPass = confirmPassField.getText();
        boolean isValid = true;

        if (oldPass.isEmpty()) {
            showError(oldPassField, oldPassErrorLabel, "Vui lòng nhập mật khẩu cũ.");
            isValid = false;
        } else if (!oldPass.equals(currentUser.getPassword()))  {
            showError(oldPassField, oldPassErrorLabel, "Mật khẩu cũ không chính xác.");
            isValid = false;
        } else {
            clearError(oldPassField, oldPassErrorLabel);
        }

        if (newPass.isEmpty()) {
            showError(newPassField, newPassErrorLabel, "Vui lòng nhập mật khẩu mới.");
            isValid = false;
        } else if (!checkPasswordStrength(newPass)){
            showError(newPassField, newPassErrorLabel, "Mật khẩu phải có độ dài tối thiểu là 8 ký tự và chứa ký tự hoa, ký tự thường, ký tự số, ký tự đặc biệt.");
            isValid = false;
        } else if (newPass.equals(oldPass)) {
            showError(newPassField, newPassErrorLabel, "Không trùng lặp với mật khẩu gần nhất.");
            isValid = false;
        } else {
            clearError(newPassField, newPassErrorLabel);
        }

        if (confirmPass.isEmpty()) {
            showError(confirmPassField, confirmPassErrorLabel, "Vui lòng nhập mật khẩu mới.");
            isValid = false;
        } else if (!confirmPass.equals(newPass)) {
            showError(confirmPassField, confirmPassErrorLabel, "Mật khẩu nhập không khớp, vui lòng nhập lại mật khẩu.");
            isValid = false;
        } else {
            clearError(confirmPassField, confirmPassErrorLabel);
        }

        if (isValid) {
            currentUser.setPassword(newPass);
            boolean isSaved = UserService.updateProfile(currentUser);
            if (isSaved) {
                showAlert("Thành công", "Đổi mật khẩu thành công!");
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                showAlert("Lỗi hệ thống", "Không thể lưu mật khẩu. Vui lòng thử lại!");
            }
        }
    }

    private boolean checkPasswordStrength(String password) {
        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasLength && hasUpper && hasNumber && hasSpecial;
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

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
