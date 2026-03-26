package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class RegisterController {
    @FXML private ScrollPane mainScrollPane;

    @FXML private RadioButton individualRadio;
    @FXML private RadioButton organizationRadio;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label lengthHint;
    @FXML private Label upperHint;
    @FXML private Label numberHint;
    @FXML private Label specialHint;

    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML private VBox frontIdBox;
    @FXML private VBox backIdBox;
    @FXML private ImageView frontImageView;
    @FXML private ImageView backImageView;
    @FXML private Label frontIdLabel;
    @FXML private Label backIdLabel;

    @FXML private ComboBox<String> dayComboBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private ComboBox<String> provinceComboBox;
    @FXML private ComboBox<String> districtComboBox;
    @FXML private ComboBox<String> wardComboBox;
    @FXML private ComboBox<String> bankComboBox;

    @FXML private CheckBox termsCheckBox;

    private File frontIdFile;
    private File backIdFile;

    @FXML
    public void initialize() {
        mainScrollPane.getContent().setOnScroll((ScrollEvent event) -> {
            double scrollSpeed = 0.005;
            double deltaY = event.getDeltaY();
            mainScrollPane.setVvalue(mainScrollPane.getVvalue() - deltaY * scrollSpeed);
            event.consume();
        });

        setupImageUpload(frontIdBox, frontIdLabel, "Chọn ảnh mặt trước CMND/CCCD", true);
        setupImageUpload(backIdBox, backIdLabel, "Chọn ảnh mặt sau CMND/CCCD", false);

        roleComboBox.getItems().addAll("Người mua (Bidder)", "Người bán (Seller)");
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordStrength(newValue);
        });

        for (int i = 1; i <= 31; i++) {
            dayComboBox.getItems().add(String.format("%02d", i));
        }

        for (int i = 1; i <= 12; i++) {
            monthComboBox.getItems().add(String.format("%02d", i));
        }

        int currentYear = java.time.LocalDate.now().getYear();
        for (int i = currentYear; i >= 1920; i--) {
            yearComboBox.getItems().add(String.valueOf(i));
        }

        String[] banks = {
                "Vietcombank (Ngân hàng Ngoại thương VN)",
                "Techcombank (Ngân hàng Kỹ thương VN)",
                "BIDV (Ngân hàng Đầu tư và Phát triển VN)",
                "Agribank (Ngân hàng Nông nghiệp VN)",
                "VietinBank (Ngân hàng Công thương VN)",
                "MBBank (Ngân hàng Quân đội)",
                "ACB (Ngân hàng Á Châu)",
                "VPBank (Ngân hàng Việt Nam Thịnh Vượng)",
                "TPBank (Ngân hàng Tiên Phong)",
                "Sacombank (Ngân hàng Sài Gòn Thương Tín)"
        };
        bankComboBox.getItems().addAll(banks);
    }

    private boolean checkPasswordStrength(String password) {
        String greenStyle = "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
        String grayStyle = "-fx-text-fill: #9e9e9e;";

        boolean hasLength = false;
        boolean hasUpper = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        if (password.length() >= 8) {
            lengthHint.setStyle(greenStyle);
            hasLength = true;
        } else {
            lengthHint.setStyle(grayStyle);
        }

        if (password.matches(".*[A-Z].*")) {
            upperHint.setStyle(greenStyle);
            hasUpper = true;
        } else {
            upperHint.setStyle(grayStyle);
        }

        if (password.matches(".*\\d.*")) {
            numberHint.setStyle(greenStyle);
            hasNumber = true;
        } else {
            numberHint.setStyle(grayStyle);
        }

        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            specialHint.setStyle(greenStyle);
            hasSpecial = true;
        } else {
            specialHint.setStyle(grayStyle);
        }

        return hasLength && hasUpper && hasNumber && hasSpecial;
    }

    private void setupImageUpload(VBox uploadBox, Label statusLabel, String dialogTitle, boolean isFront) {
        uploadBox.setOnMouseClicked(event -> {
            File selectedFile = selectImageFromComputer(dialogTitle);

            if (selectedFile != null) {
                // Đã Việt hóa thông báo chọn ảnh thành công
                statusLabel.setText("Đã chọn: " + selectedFile.getName());
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

                Image newImage = new Image(selectedFile.toURI().toString());
                if (isFront) {
                    frontIdFile = selectedFile;
                    frontImageView.setImage(newImage);
                } else {
                    backIdFile = selectedFile;
                    backImageView.setImage(newImage);
                }
            }
        });
    }

    private File selectImageFromComputer(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Hình ảnh (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg")
        );

        Stage currentStage = (Stage) frontIdBox.getScene().getWindow();
        return fileChooser.showOpenDialog(currentStage);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        String selectedRoleItem = roleComboBox.getValue();
        if (selectedRoleItem == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Mục đích tham gia (Vai trò)!");
            return;
        }

        String role = selectedRoleItem.contains("Bidder") ? "Bidder" : "Seller";
        String accountType = individualRadio.isSelected() ? "Cá nhân" : "Tổ chức";

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Vui lòng điền đầy đủ các thông tin bắt buộc!");
            return;
        }

        if (!checkPasswordStrength(password)) {
            showAlert(Alert.AlertType.WARNING, "Mật khẩu yếu", "Mật khẩu chưa đạt yêu cầu. Vui lòng kiểm tra lại các điều kiện bên dưới!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Lỗi xác nhận mật khẩu", "Mật khẩu nhập lại không khớp. Vui lòng thử lại!");
            return;
        }

        if (frontIdFile == null || backIdFile == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu tài liệu", "Vui lòng tải lên đầy đủ ảnh mặt trước và mặt sau của CMND/CCCD!");
            return;
        }

        if (!termsCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Chưa đồng ý điều khoản", "Bạn phải đồng ý với Điều khoản và Điều kiện để có thể đăng ký!");
            return;
        }

        System.out.println("Đang xử lý đăng ký cho tài khoản: " + username);
        boolean isRegistered = UserService.register(username, password, email, phone, accountType, role);

        if (isRegistered) {
            showAlert(Alert.AlertType.INFORMATION, "Đăng ký thành công", "Chào mừng " + username + "! Bạn đã đăng ký thành công.\nHệ thống sẽ chuyển về trang đăng nhập.");

            try {
                handleGoToLogin(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Đăng ký thất bại", "Tên đăng nhập đã tồn tại. Vui lòng chọn một tên khác!");
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