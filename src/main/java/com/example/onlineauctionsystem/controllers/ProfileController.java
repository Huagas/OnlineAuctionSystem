package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class ProfileController {
    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField idNumberField;
    @FXML private TextField taxCodeField;
    @FXML private TextField addressField;

    @FXML private ComboBox<String> provinceCombo, districtCombo;
    @FXML private DatePicker idIssueDatePicker;
    @FXML private ComboBox<String> idIssuePlaceCombo;
    @FXML private TextField bankAccountNameField;
    @FXML private TextField bankAccountNumberField;
    @FXML private ComboBox<String> bankNameCombo;

    @FXML private Label idNumberErrorLabel;
    @FXML private Label provinceErrorLabel;
    @FXML private Label districtErrorLabel;
    @FXML private Label idIssueDateErrorLabel;
    @FXML private Label idIssuePlaceErrorLabel;
    @FXML private Label frontImageErrorLabel;
    @FXML private Label backImageErrorLabel;
    @FXML private Label bankAccountNameErrorLabel;
    @FXML private Label bankAccountNumberErrorLabel;
    @FXML private Label bankNameErrorLabel;

    @FXML private VBox frontImageBox, backImageBox;
    @FXML private ImageView frontImageView, backImageView;

    private User currentUser;
    private File frontIdFile;
    private File backIdFile;

    private final String IMAGE_STORE_PATH = System.getProperty("user.dir") +
            File.separator + "data" +
            File.separator + "images" +
            File.separator + "cccd";

    @FXML
    public void initialize() {
        provinceCombo.setItems(FXCollections.observableArrayList("Hà Nội", "Hải Phòng", "Đà Nẵng", "TP. Hồ Chí Minh"));
        districtCombo.setItems(FXCollections.observableArrayList("Quận 1", "Quận 3", "Hoàn Kiếm", "Hải An"));
        idIssuePlaceCombo.getItems().addAll("Cục CSQLHC về TTXH", "Công an TP Hà Nội", "Công an TP Hồ Chí Minh");
        bankNameCombo.getItems().addAll("Vietcombank", "Techcombank", "MB Bank", "BIDV", "Agribank", "TPBank");

        setupValidationListeners();
    }

    public void initData(User user) {
        currentUser = user;
        if (currentUser != null) {
            usernameField.setText(user.getUsername());
            phoneField.setText(user.getPhone());
            emailField.setText(user.getEmail());

            idNumberField.setText(user.getIdNumber());
            taxCodeField.setText(user.getTaxCode());
            addressField.setText(user.getAddress());

            provinceCombo.setValue(user.getProvince());
            districtCombo.setValue(user.getDistrict());

            idIssuePlaceCombo.setValue(user.getIdIssuePlace());
            if (user.getIdIssueDate() != null && !user.getIdIssueDate().isEmpty()) {
                idIssueDatePicker.setValue(LocalDate.parse(user.getIdIssueDate()));
            }

            bankAccountNameField.setText(user.getBankAccountName());
            bankAccountNumberField.setText(user.getBankAccountNumber());
            bankNameCombo.setValue(user.getBankName());

            loadSavedImage(currentUser.getFrontIdPath(), frontImageView);
            loadSavedImage(currentUser.getBackIdPath(), backImageView);

            usernameField.setDisable(true);
            phoneField.setDisable(true);
            emailField.setDisable(true);
        }
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không tìm thấy dữ liệu người dùng!");
            return;
        }

        if (validateForm()) {
            currentUser.setUsername(usernameField.getText());
            currentUser.setIdNumber(idNumberField.getText());
            currentUser.setTaxCode(taxCodeField.getText());
            currentUser.setAddress(addressField.getText());
            currentUser.setProvince(provinceCombo.getValue());
            currentUser.setDistrict(districtCombo.getValue());
            currentUser.setIdIssueDate(idIssueDatePicker.getValue() != null ? idIssueDatePicker.getValue().toString() : "");
            currentUser.setIdIssuePlace(idIssuePlaceCombo.getValue());
            currentUser.setBankAccountName(bankAccountNameField.getText());
            currentUser.setBankAccountNumber(bankAccountNumberField.getText());
            currentUser.setBankName(bankNameCombo.getValue());

            if (frontIdFile != null) {
                String frontName = saveImageToLocalFolder(frontIdFile, "front");
                currentUser.setFrontIdPath(frontName);
            }

            if (backIdFile != null) {
                String backName = saveImageToLocalFolder(backIdFile, "back");
                currentUser.setBackIdPath(backName);
            }

            currentUser.setProfileComplete(true);
            boolean isSaved = UserService.updateProfile(currentUser);
            if (isSaved) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin tài khoản thành công!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi lưu vào hệ thống.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng kiểm tra lại các thông tin bắt buộc (*).");
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/change-password-view.fxml"));
            Parent root = loader.load();

            ChangePasswordController changePasswordController = loader.getController();
            changePasswordController.initData(currentUser);

            Stage changeStage = new Stage();
            changeStage.setTitle("Đổi mật khẩu");
            changeStage.setScene(new Scene(root));
            changeStage.initModality(Modality.APPLICATION_MODAL);
            changeStage.centerOnScreen();
            changeStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUploadFront(MouseEvent event) {
        frontIdFile = chooseImage("Tải lên mặt trước CCCD", event);
        if (frontIdFile != null) {
            frontImageView.setImage(new Image(frontIdFile.toURI().toString()));
            frontImageView.setOpacity(1.0);
        }
    }

    @FXML
    private void handleUploadBack(MouseEvent event) {
        backIdFile = chooseImage("Tải lên mặt sau CCCD", event);
        if (backIdFile != null) {
            backImageView.setImage(new Image(backIdFile.toURI().toString()));
            backImageView.setOpacity(1.0);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (idNumberField.getText() == null || idNumberField.getText().trim().isEmpty()) {
            showError(idNumberField, idNumberErrorLabel, "Số CMND/CCCD không được để trống.");
            isValid = false;
        } else {
            clearError(idNumberField, idNumberErrorLabel);
        }

        if (provinceCombo.getValue() == null) {
            showError(provinceCombo, provinceErrorLabel, "Vui lòng chọn tỉnh/thành phố.");
            isValid = false;
        } else {
            clearError(provinceCombo, provinceErrorLabel);
        }

        if (districtCombo.getValue() == null) {
            showError(districtCombo, districtErrorLabel, "Vui lòng chọn phường/xã.");
            isValid = false;
        } else {
            clearError(districtCombo, districtErrorLabel);
        }

        if (idIssueDatePicker.getValue() == null) {
            showError(idIssueDatePicker, idIssueDateErrorLabel, "Vui lòng không để trống trường ngày cấp CMND/CCCD.");
            isValid = false;
        } else {
            clearError(idIssueDatePicker, idIssueDateErrorLabel);
        }

        if (idIssuePlaceCombo.getValue() == null) {
            showError(idIssuePlaceCombo, idIssuePlaceErrorLabel, "Vui lòng không để trống trường nơi cấp CMND/CCCD.");
            isValid = false;
        } else {
            clearError(idIssuePlaceCombo, idIssuePlaceErrorLabel);
        }

        if (frontIdFile == null) {
            showError(frontImageBox, frontImageErrorLabel, "Vui lòng tải lên ảnh mặt trước CMND/CCCD.");
            isValid = false;
        } else {
            clearError(frontImageBox, frontImageErrorLabel);
        }

        if (backIdFile == null) {
            showError(backImageBox, backImageErrorLabel, "Vui lòng tải lên ảnh mặt sau CMND/CCCD.");
            isValid = false;
        } else {
            clearError(backImageBox, backImageErrorLabel);
        }

        if (bankAccountNameField.getText() == null || bankAccountNameField.getText().trim().isEmpty()) {
            showError(bankAccountNameField, bankAccountNameErrorLabel, "Vui lòng không để trống trường Chủ tài khoản.");
            isValid = false;
        } else {
            clearError(bankAccountNameField, bankAccountNameErrorLabel);
        }

        if (bankAccountNumberField.getText() == null || bankAccountNumberField.getText().trim().isEmpty()) {
            showError(bankAccountNumberField, bankAccountNumberErrorLabel, "Vui lòng không để trống trường Số tài khoản ngân hàng.");
            isValid = false;
        } else {
            clearError(bankAccountNumberField, bankAccountNumberErrorLabel);
        }

        if (bankNameCombo.getValue() == null) {
            showError(bankNameCombo, bankNameErrorLabel, "Vui lòng không để trống trường Chọn ngân hàng.");
            isValid = false;
        } else {
            clearError(bankNameCombo, bankNameErrorLabel);
        }

        return isValid;
    }

    private File chooseImage(String title, MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    private String saveImageToLocalFolder(File sourceFile, String suffix) {
        if (sourceFile == null || currentUser == null) return "";
        try {
            File destDir = new File(IMAGE_STORE_PATH);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf("."));
            String newFileName = currentUser.getUsername() + "_" + suffix + extension;
            Path destPath = Paths.get(IMAGE_STORE_PATH, newFileName);
            Files.copy(sourceFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void loadSavedImage(String fileName, ImageView imageView) {
        if (fileName != null && !fileName.isEmpty()) {
            File imgFile = new File(IMAGE_STORE_PATH + File.separator + fileName);
            if (imgFile.exists()) {
                imageView.setImage(new Image(imgFile.toURI().toString()));
                imageView.setOpacity(1.0);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(Node input, Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
        if (!input.getStyleClass().contains("modern-input-error")) {
            input.getStyleClass().add("modern-input-error");
        }
    }

    private void clearError(Node input, Label label) {
        label.setVisible(false);
        label.setManaged(false);
        input.getStyleClass().remove("modern-input-error");
    }

    private void setupValidationListeners() {
        idNumberField.textProperty().addListener((obs, old, newValue) -> clearError(idNumberField, idNumberErrorLabel));
        provinceCombo.valueProperty().addListener((obs, old, newValue) -> clearError(provinceCombo, provinceErrorLabel));
        districtCombo.valueProperty().addListener((obs, old, newValue) -> clearError(districtCombo, districtErrorLabel));

        idIssueDatePicker.valueProperty().addListener((obs, old, newValue) -> clearError(idIssueDatePicker, idIssueDateErrorLabel));
        idIssuePlaceCombo.valueProperty().addListener((obs, old, newValue) -> clearError(idIssuePlaceCombo, idIssuePlaceErrorLabel));
        bankAccountNameField.textProperty().addListener((obs, old, newValue) -> clearError(bankAccountNameField, bankAccountNameErrorLabel));
        bankAccountNumberField.textProperty().addListener((obs, old, newValue) -> clearError(bankAccountNumberField, bankAccountNumberErrorLabel));
        bankNameCombo.valueProperty().addListener((obs, old, newValue) -> clearError(bankNameCombo, bankNameErrorLabel));
    }
}
