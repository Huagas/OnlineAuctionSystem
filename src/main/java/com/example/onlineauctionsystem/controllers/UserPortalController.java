package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class UserPortalController {

    @FXML private StackPane contentArea;

    @FXML private Button btnProfile;
    @FXML private Button btnCart;
    @FXML private Button btnHistory;

    private User currentUser;

    public void initData(User user) {
        currentUser = user;
    }

    @FXML
    public void initialize() {
        showProfile();
    }

    @FXML
    public void showProfile() {
        loadViewWithUser("profile-view.fxml", btnProfile);
    }

    @FXML
    public void showHistory() {
        loadViewWithUser("auction-history.fxml", btnHistory);
    }

    // --- CÁC HÀM TIỆN ÍCH DƯỚI ĐÂY ---

    /**
     * Hàm dùng để tải file FXML và đắp lên màn hình giữa
     */
    private void loadViewWithUser(String fxmlFileName, Button activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/" + fxmlFileName));
            Node view = loader.load();

            // 1. Tóm lấy Controller của cái View con (Profile hoặc History)
            Object controller = loader.getController();

            // 2. Kiểm tra xem nó là loại Controller nào để bơm dữ liệu User sang cho nó
            if (controller instanceof ProfileController) {
                ((ProfileController) controller).initData(this.currentUser);
            } else if (controller instanceof AuctionHistoryController) {
                ((AuctionHistoryController) controller).initData(this.currentUser);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            setActiveButton(activeButton);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi không tìm thấy file: " + fxmlFileName);
        }
    }

    /**
     * Hàm đổi màu nút để biết người dùng đang ở trang nào (Đỏ là đang chọn)
     */
    private void setActiveButton(Button activeButton) {
        Button[] allButtons = {btnProfile, btnCart, btnHistory};

        for (Button btn : allButtons) {
            btn.getStyleClass().remove("menu-item-active");
        }

        if (!activeButton.getStyleClass().contains("menu-item-active")) {
            activeButton.getStyleClass().add("menu-item-active");
        }
    }
}