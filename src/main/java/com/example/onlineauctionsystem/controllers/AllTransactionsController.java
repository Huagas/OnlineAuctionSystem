package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.BidTransaction;
import com.example.onlineauctionsystem.models.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class AllTransactionsController {
    @FXML private VBox allTransactionsContainer;

    public void setData(List<BidTransaction> history, User currentUser) {
        allTransactionsContainer.getChildren().clear();
        for (int i = history.size() - 1; i >= 0; i --) {
            allTransactionsContainer.getChildren().add(createHistoryRow(history.get(i), currentUser));
        }
    }

    private HBox createHistoryRow(BidTransaction tx, User currentUser) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        Label price = new Label(String.format("$%,.0f", tx.getBidAmount()));
        price.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");

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
}
