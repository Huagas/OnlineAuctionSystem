package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.AutoBid;
import com.example.onlineauctionsystem.models.BidTransaction;
import com.example.onlineauctionsystem.models.Item;
import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.ItemService;
import com.example.onlineauctionsystem.services.UserService;
import com.example.onlineauctionsystem.utils.AuctionObserver;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemDetailsController implements AuctionObserver {
    @FXML private Label titleLabel;
    @FXML private Label descLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label currentBidLabel;
    @FXML private Label statusLabel;

    @FXML private Label dayLabel;
    @FXML private Label hourLabel;
    @FXML private Label minuteLabel;
    @FXML private Label secondLabel;

    @FXML private TextField bidAmountField;
    @FXML private Button bidButton;
    @FXML private Label modeIndicatorLabel;

    @FXML private VBox bidHistoryContainer;

    private boolean isAutoMode = false;
    private Item currentItem;
    private User currentUser;
    private Timeline timeline;

    public void initData(Item item, User user) {
        this.currentItem = item;
        this.currentUser = user;

        List<BidTransaction> history = ItemService.loadHistoryForItem(item.getId());
        item.setBidHistory(history);
        if (!history.isEmpty()) {
            BidTransaction latest = history.get(history.size() - 1);
            item.setCurrentHighestBid(latest.getBidAmount());
            item.setCurrentWinnerId(latest.getBidderId());
        }

        titleLabel.setText(item.getName());
        descLabel.setText(item.getDescription());
        startPriceLabel.setText(String.format("$%,.0f", item.getStartingPrice()));

        checkExistingAutoMode();
        updateUI();
        startCountdownTimer();
        ItemService.addObserver(this);
        setupCloseRequest();
    }

    private void updateUI() {
        updateCurrentBidDisplay();
        updateBidHistory();
    }

    @Override
    public void onAuctionUpdate() {
        Platform.runLater(() -> {
            updateUI();
        });
    }

    private void updateBidHistory() {
        bidHistoryContainer.getChildren().clear();
        List<BidTransaction> allTransactions = currentItem.getBidHistory();
        if (allTransactions == null || allTransactions.isEmpty()) {
            return;
        }

        List<BidTransaction> top3 = allTransactions.stream()
                .sorted((b1, b2) -> Double.compare(b2.getBidAmount(), b1.getBidAmount()))
                .limit(3)
                .collect(Collectors.toList());

        for (BidTransaction tx: top3) {
            bidHistoryContainer.getChildren().add(createHistoryRow(tx));
        }
    }

    private HBox createHistoryRow(BidTransaction tx) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));

        Label price = new Label(String.format("$%,.0f", tx.getBidAmount()));
        price.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String rawName =  "VN-" + tx.getBidderId().substring(0, 6).toUpperCase();
        boolean isMe = tx.getBidderId().equals(currentUser.getId());
        Label user = new Label(isMe ? "(Bạn) " + rawName : rawName);
        if (isMe) {
            user.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
        } else {
            user.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold");
        }
        Label time = new Label(tx.getFormattedTime());
        time.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10px; -fx-font-weight: bold;");

        row.getChildren().addAll(price, spacer, user, time);
        return row;
    }

    @FXML
    private void handleViewAllTransactions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/onlineauctionsystem/views/all-transactions.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Lịch sử giao dịch - " + currentItem.getName());
            stage.initModality(Modality.APPLICATION_MODAL);

            AllTransactionsController controller = loader.getController();
            controller.setData(currentItem.getBidHistory(), currentUser);

            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handlePlaceBid(ActionEvent event) {
        String inputStr = bidAmountField.getText();
        if (inputStr == null || inputStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng nhập số tiền bạn muốn đấu giá!");
            return ;
        }
        try {
            double bidAmount = Double.parseDouble(inputStr);
            String resultMsg;
            if (isAutoMode) {
                resultMsg = ItemService.registerAutoBid(currentItem.getId(), currentUser.getId(), bidAmount);
            } else {
                resultMsg = ItemService.placeBid(currentItem.getId(), currentUser.getId(), bidAmount);
            }
            if (resultMsg.equals("SUCCESS")) {
                bidAmountField.clear();
                if (isAutoMode) {
                    showAlert(Alert.AlertType.INFORMATION, "Ủy quyền thành công",
                            "Hệ thống đã nhận Max Bid $" + bidAmount + ". Chúng tôi sẽ tự động đấu giá thay bạn!");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            "Bạn đã vươn lên dẫn đầu với mức giá $" + bidAmount + "!");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Không hợp lệ", resultMsg);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Vui lòng chỉ nhập số (Không nhập chữ hoặc ký tự đặc biệt)!");
        }
    }

    private void updateCurrentBidDisplay() {
        currentBidLabel.setText(String.format("$%,.0f", currentItem.getCurrentHighestBid()));
    }

    private void startCountdownTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            String status = currentItem.getStatus();
            if (statusLabel != null) {
                statusLabel.setText(status);
            }
            if (status.equals("Chờ bắt đầu")) {
                java.time.Duration duration = java.time.Duration.between(now, currentItem.getStartTime());
                updateTimerLabels(duration);
                if (statusLabel != null) statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold");
                bidButton.setDisable(true);
                bidAmountField.setDisable(true);
                bidButton.setText("CHƯA MỞ CỬA");
            } else if (status.equals("Đang diễn ra")) {
                java.time.Duration duration = java.time.Duration.between(now, currentItem.getEndTime());
                updateTimerLabels(duration);
                if (statusLabel != null) statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                bidButton.setDisable(false);
                bidAmountField.setDisable(false);
                bidButton.setText("ĐẶT GIÁ");
            } else {
                timeline.stop();
                setTimerZero();
                if (statusLabel != null) statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                bidButton.setDisable(true);
                bidAmountField.setDisable(true);
                bidButton.setText("ĐÃ KẾT THÚC");
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateTimerLabels(java.time.Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.toSeconds() % 60;
        dayLabel.setText(String.format("%02d", days));
        hourLabel.setText(String.format("%02d", hours));
        minuteLabel.setText(String.format("%02d", minutes));
        secondLabel.setText(String.format("%02d", seconds));
    }

    private void setTimerZero() {
        dayLabel.setText("00");
        hourLabel.setText("00");
        minuteLabel.setText("00");
        secondLabel.setText("00");
    }

    @FXML
    protected void handleIncreaseBid() {
        try {
            double currentVal = Double.parseDouble(bidAmountField.getText());
            bidAmountField.setText(String.format("%.0f", currentVal + currentItem.getBidIncrement()));
        } catch (Exception e) {
            double minAllowed = getSuggestedBid();
            bidAmountField.setText(String.format("%.0f", minAllowed));
        }
    }

    @FXML
    protected void handleDecreaseBid() {
        try {
            double currentVal = Double.parseDouble(bidAmountField.getText());
            double minAllowed = getSuggestedBid();
            if (currentVal > minAllowed) {
                bidAmountField.setText(String.format("%.0f", Math.max(minAllowed, currentVal - currentItem.getBidIncrement())));
            }
        } catch (Exception e) {}
    }

    @FXML
    protected void toggleMode() {
        isAutoMode = !isAutoMode;
        updateModeUI();
    }

    private double getSuggestedBid() {
        return currentItem.getCurrentWinnerId().equals("NONE")
                ? currentItem.getStartingPrice()
                : currentItem.getCurrentHighestBid() + currentItem.getBidIncrement();
    }

    private void updateModeUI() {
        if (modeIndicatorLabel == null) return;
        if (isAutoMode) {
            modeIndicatorLabel.setText("Auto");
            modeIndicatorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; " +
                    "-fx-background-color: #e8f8f5; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
        } else {
            modeIndicatorLabel.setText("Manual");
            modeIndicatorLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold; " +
                    "-fx-background-color: #f2f4f4; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
        }
    }

    private void checkExistingAutoMode() {
        boolean found = false;
        for (AutoBid ab: currentItem.getAutoBids()) {
            if (ab.getUserId().equals(currentUser.getId())) {
                found = true;
                break;
            }
        }
        this.isAutoMode = found;
        updateModeUI();
    }

    private void cleanup() {
        if (timeline != null) timeline.stop();
        ItemService.removeObserver(this);
    }

    @FXML
    protected void handleClose(ActionEvent event) {
        cleanup();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void setupCloseRequest() {
        Platform.runLater(() -> {
            if (titleLabel.getScene() != null) {
                Stage stage = (Stage) titleLabel.getScene().getWindow();
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