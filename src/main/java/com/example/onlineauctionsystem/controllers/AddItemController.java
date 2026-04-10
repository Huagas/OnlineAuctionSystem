package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.Art;
import com.example.onlineauctionsystem.models.Electronics;
import com.example.onlineauctionsystem.models.Item;
import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.ItemService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AddItemController {
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextField priceField;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    @FXML private TextArea descArea;

    @FXML private Label extraLabel1;
    @FXML private TextField extraField1;
    @FXML private Label extraLabel2;
    @FXML private TextField extraField2;

    private User currentSeller;

    @FXML
    public void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList("Electronics", "Art"));

        categoryBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Electronics".equals(newVal)) {
                extraLabel1.setText("Thương hiệu:");
                extraField1.setPromptText("VD: Apple, Sony...");
                extraLabel2.setText("Bảo hành (tháng):");
                extraField2.setPromptText("VD: 12");
            } else if ("Art".equals(newVal)) {
                extraLabel1.setText("Tác giả:");
                extraField1.setPromptText("VD: Tô Ngọc Vân");
                extraLabel2.setText("Chất liệu:");
                extraField2.setPromptText("VD: Sơn dầu");
            }
        });
    }

    public void setSeller(User seller) {
        this.currentSeller = seller;
    }

    private Item editingItem = null;

    public void setEditMode(Item item) {
        this.editingItem = item;

        nameField.setText(item.getName());
        priceField.setText(String.valueOf(item.getStartingPrice()));
        descArea.setText(item.getDescription());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        startDatePicker.setValue(item.getStartTime().toLocalDate());
        startTimeField.setText(item.getStartTime().toLocalTime().format(timeFormatter));

        endDatePicker.setValue(item.getEndTime().toLocalDate());
        endTimeField.setText(item.getEndTime().toLocalTime().format(timeFormatter));

        if (item instanceof Electronics) {
            categoryBox.setValue("Electronics");
            extraField1.setText(((Electronics) item).getBrand());
            extraField2.setText(String.valueOf(((Electronics) item).getWarrantyMonths()));
        } else if (item instanceof Art) {
            categoryBox.setValue("Art");
            extraField1.setText(((Art) item).getArtist());
            extraField2.setText(((Art) item).getMaterial());
        }

        categoryBox.setDisable(true);
    }

    @FXML
    protected void handleSave(ActionEvent event) {
        try {
            String name = nameField.getText();
            String category = categoryBox.getValue();
            String desc = descArea.getText();
            String priceStr = priceField.getText();
            LocalDate startDate = startDatePicker.getValue();
            String startTimeStr = startTimeField.getText();
            LocalDate endDate = endDatePicker.getValue();
            String endTimeStr = endTimeField.getText();
            String extra1 = extraField1.getText();
            String extra2 = extraField2.getText();

            if (name.isEmpty() || category == null || priceStr.isEmpty() || startDate == null || startTimeStr.isEmpty() ||
                    endDate == null || endTimeStr.isEmpty() || extra1.isEmpty() || extra2.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ các trường!");
                return ;
            }

            double price = Double.parseDouble(priceStr);
            LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.parse(startTimeStr));
            LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.parse(endTimeStr));

            if (endTime.isBefore(startTime)) {
                showAlert(Alert.AlertType.WARNING, "Lỗi thời gian", "Thời gian kết thúc phải lớn hơn hiện tại!");
                return ;
            }

            Item newItem = null;
            if ("Electronics".equals(category)) {
                int warranty = Integer.parseInt(extra2);
                newItem = new Electronics(name, desc, price, startTime, endTime, currentSeller.getId(), extra1, warranty);
            } else if ("Art".equals(category)) {
                newItem = new Art(name, desc, price, startTime, endTime, currentSeller.getId(), extra1, extra2);
            }

            if (newItem != null) {
                if (editingItem != null) {
                    newItem.setId(editingItem.getId());
                    newItem.setCurrentHighestBid(editingItem.getCurrentHighestBid());
                    ItemService.updateItemDetails(newItem);
                } else {
                    ItemService.addItem(newItem);
                }

                closeWindow(event);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Giá khởi điểm và Số tháng bảo hành (nếu là đồ điện tử) phải là con số!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi thời gian", "Vui lòng nhập giờ kết thúc theo đúng định dạng HH:mm (Ví dụ: 14:30)");
        }
    }

    @FXML
    protected void handleCancel(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
