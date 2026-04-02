package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.Item;
import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.ItemService;
import com.example.onlineauctionsystem.utils.AuctionObserver;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class SellerDashboardController implements AuctionObserver {
    @FXML private Label welcomeLabel;

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> colId;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colCategory;
    @FXML private TableColumn<Item, Double> colStartPrice;
    @FXML private TableColumn<Item, Double> colCurrentBid;
    @FXML private TableColumn<Item, String> colStartTime;
    @FXML private TableColumn<Item, String> colEndTime;
    @FXML private TableColumn<Item, String> colStatus;
    @FXML private Timeline tableRefreshTimeline;
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Xin chào Seller: " + user.getUsername());
        setupTableColumns();
        loadMyItems();
        ItemService.addObserver(this);
        startTableAutoRefresh();
        setupCloseRequest();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colCurrentBid.setCellValueFactory(new PropertyValueFactory<>("currentHighestBid"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        colStartTime.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartTime().format(formatter)));

        colEndTime.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEndTime().format(formatter)));

        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
    }

    @Override
    public void onAuctionUpdate() {
        Platform.runLater(() -> {
            loadMyItems();
        });
    }

    private void loadMyItems() {
        List<Item> allItems = ItemService.getItemsBySeller(currentUser.getId());
        ObservableList<Item> observableList = FXCollections.observableArrayList(allItems);
        itemTable.setItems(observableList);
    }

    private void startTableAutoRefresh() {
        tableRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> itemTable.refresh()));
        tableRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        tableRefreshTimeline.play();
    }

    @FXML
    protected void handleAddNewItem(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/add-item-form.fxml"));
            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(loader.load()));
            popupStage.setTitle("Đăng bán tài sản mới");

            popupStage.initModality(Modality.APPLICATION_MODAL);
            AddItemController controller = loader.getController();
            controller.setSeller(currentUser);

            popupStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi không thể mở cửa sổ: " + e.getMessage());
        }
    }

    @FXML
    protected void handleDeleteItem(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn tài sản", "Vui lòng click chọn một tài sản trong bảng để xóa!");
            return ;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa tài sản '" + selectedItem.getName() + "' không?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            boolean success = ItemService.deleteItem(selectedItem.getId());
            if (!success) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài sản này.");
            }
        }
    }

    @FXML
    protected void handleEditItem(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn tài sản", "Vui lòng click chọn một tài sản trong bảng để sửa!");
            return ;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/add-item-form.fxml"));
            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(loader.load()));
            popupStage.setTitle("Chỉnh sửa tài sản đấu giá");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            AddItemController controller = loader.getController();
            controller.setSeller(currentUser);
            controller.setEditMode(selectedItem);

            popupStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleCancelAuction(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn tài sản", "Vui lòng click chọn một tài sản trong bảng để thao tác!");
            return ;
        }

        String currentStatus = selectedItem.getStatus();
        if (!currentStatus.equals("Đã kết thúc")) {
            showAlert(Alert.AlertType.WARNING, "Không hợp lệ",
                    "Chỉ có thể hủy kết quả những phiên đấu giá ĐÃ KẾT THÚC nhưng người mua chưa thanh toán!");
            return ;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc chắn muốn HỦY kết quả của sản phẩm '" + selectedItem.getName() + "' không?\n"
                        + "Hành động này không thể hoàn tác!",
                javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
        confirm.setTitle("Xác nhận Hủy giao dịch");
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            boolean success = ItemService.updatePaymentStatus(selectedItem.getId(), "CANCELED");
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy kết quả đấu giá!");
                loadMyItems();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi cập nhật hệ thống.");
            }
        }
    }

    @FXML
    protected void handleConfirmPayment(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn tài sản", "Vui lòng click chọn một tài sản trong bảng để thao tác!");
            return ;
        }

        String currentStatus = selectedItem.getStatus();
        if (!currentStatus.equals("Đã kết thúc")) {
            showAlert(Alert.AlertType.WARNING, "Không hợp lệ",
                    "Chỉ có thể xác nhận thanh toán cho những phiên đấu giá ĐÃ KẾT THÚC!");
            return ;
        }

        if (selectedItem.getCurrentWinnerId().equals("NONE")) {
            showAlert(Alert.AlertType.WARNING, "Không có người mua",
                    "Phiên đấu giá này kết thúc mà không có ai trả giá, không thể xác nhận thanh toán!");
            return ;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận bạn đã nhận ĐỦ TIỀN cho sản phẩm '" + selectedItem.getName() + "'?\n" +
                        "Trạng thái sản phẩm sẽ vĩnh viễn chuyển thành ĐÃ THANH TOÁN.",
                javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
        confirm.setTitle("Xác nhận đã thu tiền");
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            boolean success = ItemService.updatePaymentStatus(selectedItem.getId(), "PAID");
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Giao dịch hoàn tất! Đã cập nhật trạng thái ĐÃ THANH TOÁN.");
                loadMyItems();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi cập nhật hệ thống.");
            }
        }
    }

    private void cleanup() {
        if (tableRefreshTimeline != null) tableRefreshTimeline.stop();
        ItemService.removeObserver(this);
    }

    @FXML
    protected void handleLogout(ActionEvent event) throws IOException {
        cleanup();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/login-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.centerOnScreen();
    }

    private void setupCloseRequest() {
        Platform.runLater(() -> {
            if (welcomeLabel.getScene() != null) {
                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                stage.setOnCloseRequest(event -> cleanup());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
