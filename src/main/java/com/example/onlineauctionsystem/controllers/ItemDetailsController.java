package com.example.onlineauctionsystem.controllers;

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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;

public class ItemDetailsController implements AuctionObserver {
    @FXML private Label titleLabel;
    @FXML private Label descLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label currentBidLabel;
    @FXML private Label currentWinnerLabel;

    @FXML private Label statusLabel;

    @FXML private Label dayLabel;
    @FXML private Label hourLabel;
    @FXML private Label minuteLabel;
    @FXML private Label secondLabel;

    @FXML private TextField bidAmountField;
    @FXML private Button bidButton;

    @FXML private Label modeIndicatorLabel;
    private boolean isAutoMode = true;

    private Item currentItem;
    private User currentUser;
    private Timeline timeline;

    public void initData(Item item, User user) {
        this.currentItem = item;
        this.currentUser = user;

        titleLabel.setText(item.getName());
        descLabel.setText(item.getDescription());
        startPriceLabel.setText(String.format("$%,.0f", item.getStartingPrice()));
        updateCurrentBidDisplay();
        startCountdownTimer();

        ItemService.addObserver(this);
    }

    @Override
    public void onAuctionUpdate() {
        Platform.runLater(() -> {
            updateCurrentBidDisplay();
        });
    }

    private void updateCurrentBidDisplay() {
        currentBidLabel.setText(String.format("$%,.0f", currentItem.getCurrentHighestBid()));

        String winnerId = currentItem.getCurrentWinnerId();
        if (winnerId == null || winnerId.equals("NONE")) {
            if (currentWinnerLabel != null) {
                currentWinnerLabel.setText("Chưa có ai");
            }
        } else {
            User winner = UserService.getUserById(winnerId);
            if (winner != null && currentWinnerLabel != null) {
                String username = winner.getUsername();

                String maskedName;
                if (username.length() <= 3) {
                    maskedName = username.substring(0, 1) + "***";
                } else {
                    maskedName = username.substring(0, 3) + "***";
                }

                if (winnerId.equals(currentUser.getId())) {
                    currentWinnerLabel.setText("Bạn (Đang dẫn đầu)");
                    currentWinnerLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else {
                    currentWinnerLabel.setText(maskedName);
                    currentWinnerLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                }
            }
        }

        if (isAutoMode && !bidAmountField.isFocused()) {
            double suggestedBid;
            if (currentItem.getCurrentWinnerId().equals("NONE")) {
                suggestedBid = currentItem.getStartingPrice();
            } else {
                suggestedBid = currentItem.getCurrentHighestBid() + currentItem.getBidIncrement();
            }
            String suggestedStr = String.format("%.0f", suggestedBid);
            bidAmountField.setText(suggestedStr);
        }
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
                updateCurrentBidDisplay();
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

    @FXML
    protected void handleIncreaseBid() {
        try {
            double currentVal = Double.parseDouble(bidAmountField.getText());
            bidAmountField.setText(String.format("%.0f", currentVal + currentItem.getBidIncrement()));
        } catch (Exception e) {
            double minAllowed = currentItem.getCurrentWinnerId().equals("NONE")
                    ? currentItem.getStartingPrice()
                    : currentItem.getCurrentHighestBid() + currentItem.getBidIncrement();
            bidAmountField.setText(String.format("%.0f", minAllowed));
        }
    }

    @FXML
    protected void handleDecreaseBid() {
        try {
            double currentVal = Double.parseDouble(bidAmountField.getText());
            double minAllowed = currentItem.getCurrentWinnerId().equals("NONE")
                    ? currentItem.getStartingPrice()
                    : currentItem.getCurrentHighestBid() + currentItem.getBidIncrement();
            if (currentVal - currentItem.getBidIncrement() >= minAllowed) {
                bidAmountField.setText(String.format("%.0f", currentVal - currentItem.getBidIncrement()));
            }
        } catch (Exception e) {}
    }

    private void switchToManualMode() {
        isAutoMode = false;
        if (modeIndicatorLabel != null) {
            modeIndicatorLabel.setText("Manual");
            modeIndicatorLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
        }
    }

    @FXML
    protected void toggleMode() {
        isAutoMode = !isAutoMode;
        if (isAutoMode) {
            if (modeIndicatorLabel != null) {
                modeIndicatorLabel.setText("Auto");
                modeIndicatorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            }

            double suggestedBid;
            if (currentItem.getCurrentWinnerId().equals("NONE")) {
                suggestedBid = currentItem.getStartingPrice();
            } else {
                suggestedBid = currentItem.getCurrentHighestBid() + currentItem.getBidIncrement();
            }
            bidAmountField.setText(String.format("%.0f", suggestedBid));
        } else {
            switchToManualMode();
        }
    }

    @FXML
    protected void handleClose(ActionEvent event) {
        if (timeline != null) timeline.stop();
        ItemService.removeObserver(this);
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