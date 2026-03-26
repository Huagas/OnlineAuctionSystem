package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.Bidder;
import com.example.onlineauctionsystem.models.Item;
import com.example.onlineauctionsystem.models.User;
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
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BidderDashboardController implements AuctionObserver {
    @FXML private Label welcomeLabel;
    @FXML private Label walletLabel;
    @FXML private Label itemCountLabel;

    @FXML private FlowPane itemGrid;
    @FXML private Timeline tableRefreshTimeline;
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Xin chào, " + user.getUsername());

        if (user instanceof Bidder) {
            walletLabel.setText(String.format("Ví: $%,.0f", ((Bidder) user).getWalletBalance()));
        }

        loadItemGrid();
        ItemService.addObserver(this);
        startTableAutoRefresh();
    }

    @Override
    public void onAuctionUpdate() {
        Platform.runLater(() -> {
            loadItemGrid();
        });
    }

    private void startTableAutoRefresh() {
        tableRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            loadItemGrid();
        }));
        tableRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        tableRefreshTimeline.play();
    }

    private void loadItemGrid() {
        itemGrid.getChildren().clear();
        List<Item> allItems = ItemService.getAllItems();
        itemCountLabel.setText("Hiển thị " + allItems.size() + " sản phẩm");
        for (Item item: allItems) {
            VBox card = createItemCard(item);
            itemGrid.getChildren().add(card);
        }
    }

    private VBox createItemCard(Item item) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
        HBox timeBox = createRow("Thời gian:", item.getStartTime().format(formatter));

        HBox statusBox = createRow("Trạng thái:", item.getStatus());
        Label lblStatusValue = (Label) statusBox.getChildren().get(2);

        String status = item.getStatus();
        if (status.equals("Đang diễn ra")) {
            lblStatusValue.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else if (status.equals("Đã kết thúc") || status.equals("Đã thanh toán") || status.equals("Đã hủy")) {
            lblStatusValue.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            lblStatusValue.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        }

        infoBox.getChildren().addAll(nameLabel, new Region(), priceBox, depositBox, timeBox, statusBox);
        card.getChildren().addAll(imageBox, infoBox);

        card.setOnMouseClicked(event -> openItemDetails(item));
        return card;
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

    @FXML
    protected void handleLogout(ActionEvent event) throws IOException {
        if (tableRefreshTimeline != null) tableRefreshTimeline.stop();
        ItemService.removeObserver(this);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/login-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load(), 450, 550));
        stage.centerOnScreen();
    }
}
