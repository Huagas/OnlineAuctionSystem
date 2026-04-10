package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.Bidder;
import com.example.onlineauctionsystem.models.Item;
import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.AuctionRegistrationService;
import com.example.onlineauctionsystem.services.ItemService;
import com.example.onlineauctionsystem.utils.AuctionObserver;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BidderDashboardController implements AuctionObserver {
    @FXML private Label welcomeLabel;
    @FXML private Label walletLabel;
    @FXML private Label itemCountLabel;

    @FXML private FlowPane itemGrid;
    @FXML private Timeline tableRefreshTimeline;
    private User currentUser;

    private final Map<String, VBox> itemCardMap = new HashMap<>();

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Xin chào, " + user.getUsername());

        if (user instanceof Bidder) {
            walletLabel.setText(String.format("Ví: $%,.0f", ((Bidder) user).getWalletBalance()));
        }

        loadItemGrid();
        ItemService.addObserver(this);
        startTableAutoRefresh();
        setupCloseRequest();
    }

    @Override
    public void onAuctionUpdate() {
        Platform.runLater(() -> {
            loadItemGrid();
        });
    }

    private void startTableAutoRefresh() {
        tableRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            loadItemGrid();
        }));
        tableRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        tableRefreshTimeline.play();
    }

    private void loadItemGrid() {
        List<Item> allItems = ItemService.getAllItems();
        itemCountLabel.setText("Hiển thị " + allItems.size() + " sản phẩm");
        for (Item item: allItems) {
            if (itemCardMap.containsKey(item.getId())) {
                VBox existingCard = itemCardMap.get(item.getId());
                updateCardContent(existingCard, item);
            } else {
                VBox newCard = createItemCard(item);
                itemCardMap.put(item.getId(), newCard);
                itemGrid.getChildren().add(newCard);
            }
        }

        itemCardMap.keySet().removeIf(id -> {
            boolean exist = allItems.stream().anyMatch(i -> i.getId().equals(id));
            if (!exist) {
                itemGrid.getChildren().remove(itemCardMap.get(id));
            }
            return !exist;
        });
    }

    private VBox createItemCard(Item item) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.setUserData(item.getId());
        card.getStyleClass().add("item-card");

        StackPane imageBox = new StackPane();
        imageBox.setPrefHeight(150);
        imageBox.getStyleClass().add("card-image-placeholder");

        Label catLabel = new Label(item.getCategory());
        catLabel.getStyleClass().add("text-muted");
        catLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        imageBox.getChildren().add(catLabel);

        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(15));

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setWrapText(true);

        HBox priceBox = createRow("Giá khởi điểm:", String.format("$%,.0f", item.getStartingPrice()));
        HBox depositBox = createRow("Giá cao nhất:", String.format("$%,.0f", item.getCurrentHighestBid()));

        Label lblHighestValue = (Label) depositBox.getChildren().get(2);
        lblHighestValue.setId("highest-bid-" + item.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
        HBox timeBox = createRow("Thời gian:", item.getStartTime().format(formatter));

        HBox statusBox = createRow("Trạng thái:", item.getStatus());
        Label lblStatusValue = (Label) statusBox.getChildren().get(2);
        lblStatusValue.setId("status-" + item.getId());
        updateStatusStyle(lblStatusValue, item.getStatus());

        Button btnRegister = new Button("Đăng ký đấu giá");
        btnRegister.getStyleClass().add("register-btn");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(btnRegister, new Insets(10, 0, 0, 0));
        if (AuctionRegistrationService.isUserRegistered(currentUser.getId(), item.getId())) {
            btnRegister.setText("Chờ đấu giá");
            btnRegister.setDisable(true);
        }
        btnRegister.setOnAction(event -> {
            event.consume();
            boolean success = AuctionRegistrationService.registerUserForItem(currentUser.getId(), item.getId(), 5000);
            if (success) {
                btnRegister.setText("Chờ đấu giá");
                btnRegister.setDisable(true);
            }
        });

        infoBox.getChildren().addAll(nameLabel, new Region(), priceBox, depositBox, timeBox, statusBox, btnRegister);
        card.getChildren().addAll(imageBox, infoBox);
        card.setOnMouseClicked(event -> openItemDetails(item));
        return card;
    }

    private void updateCardContent(VBox card, Item item) {
        Label lblHighestValue = (Label) card.lookup("#highest-bid-" + item.getId());
        if (lblHighestValue != null) {
            String newPrice = String.format("$%,.0f", item.getCurrentHighestBid());
            lblHighestValue.setText(newPrice);
        }

        Label lblStatusValue = (Label) card.lookup("#status-" + item.getId());
        if (lblStatusValue != null) {
            lblStatusValue.setText(item.getStatus());
            updateStatusStyle(lblStatusValue, item.getStatus());
        }
    }

    private void updateStatusStyle(Label lbl, String status) {
        if (status.equals("Đang diễn ra")) {
            lbl.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else if (status.equals("Đã kết thúc") || status.equals("Đã thanh toán") || status.equals("Đã hủy")) {
            lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            lbl.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        }
    }

    private HBox createRow(String title, String value) {
        HBox row = new HBox();
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("text-muted");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("text-dark-bold");
        lblValue.setStyle("-fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(lblTitle, spacer, lblValue);
        return row;
    }

    private void openItemDetails(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/item-details.fxml"));
            Stage detailStage = new Stage();
            detailStage.setScene(new Scene(loader.load()));
            detailStage.setTitle("Chi Tiết Tài Sản - " + item.getName());
            detailStage.initModality(Modality.APPLICATION_MODAL);
            ItemDetailsController controller = loader.getController();
            controller.initData(item, currentUser);
            detailStage.showAndWait();
            loadItemGrid();
        } catch (IOException e) {
            e.printStackTrace();
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
        stage.setScene(new Scene(loader.load(), 450, 550));
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
}
