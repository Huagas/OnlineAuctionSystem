package com.example.onlineauctionsystem.controllers;

import com.example.onlineauctionsystem.models.BidTransaction;
import com.example.onlineauctionsystem.models.Item;
import com.example.onlineauctionsystem.models.User;
import com.example.onlineauctionsystem.services.AuctionRegistrationService;
import com.example.onlineauctionsystem.services.ItemService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class AuctionHistoryController {

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TextField searchField;

    @FXML private TableView<Item> historyTable;
    @FXML private TableColumn<Item, String> colItemName;
    @FXML private TableColumn<Item, String> colWinningBid;
    @FXML private TableColumn<Item, String> colMyHighestBid;
    @FXML private TableColumn<Item, String> colBidTime;
    @FXML private TableColumn<Item, String> colStatus;
    @FXML private TableColumn<Item, Void> colAction;

    private User currentUser;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,### đ");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public void initData(User user) {
        currentUser = user;
        loadUserHistory();
    }

    @FXML
    public void initialize() {
        colItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        colWinningBid.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getCurrentHighestBid();
            return new SimpleStringProperty(currencyFormat.format(price));
        });

        colMyHighestBid.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            double myMaxBid = 0;
            String userId = (currentUser != null) ? currentUser.getId() : "";
            for (BidTransaction tx: item.getBidHistory()) {
                if (tx.getBidderId().equals(userId) && tx.getBidAmount() > myMaxBid) {
                    myMaxBid = tx.getBidAmount();
                }
            }
            return new SimpleStringProperty(myMaxBid > 0 ? currencyFormat.format(myMaxBid) : "Chưa trả giá");
        });

        colBidTime.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            String userId = (currentUser != null) ? currentUser.getId() : "";
            LocalDateTime myLastBidTime = null;
            for (BidTransaction tx: item.getBidHistory()) {
                if (tx.getBidderId().equals(userId)) {
                    if (myLastBidTime == null || tx.getCreatedAt().isAfter(myLastBidTime)) {
                        myLastBidTime = tx.getCreatedAt();
                    }
                }
            }

            if (myLastBidTime != null) {
                return new SimpleStringProperty(myLastBidTime.format(timeFormat));
            } else {
                return new SimpleStringProperty("-");
            }
        });

        colStatus.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            String itemGlobalStatus = item.getStatus();
            String myUserId = (currentUser != null) ? currentUser.getId() : "";
            if (itemGlobalStatus.equals("Đã kết thúc") | itemGlobalStatus.equals("Đã thanh toán")) {
                double maxBid = 0;
                String winnerId = "";
                for (BidTransaction tx : item.getBidHistory()) {
                    if (tx.getBidAmount() > maxBid) {
                        maxBid = tx.getBidAmount();
                        winnerId = tx.getBidderId();
                    }
                }

                if (winnerId.equals(myUserId)) {
                    return new SimpleStringProperty("Đã trúng");
                } else {
                    return new SimpleStringProperty("Trượt");
                }
            }

            return new SimpleStringProperty(itemGlobalStatus);
        });

        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("Xem chi tiết");
            {
                btn.getStyleClass().add("action-link");
                btn.setOnAction(event -> {
                    Item selectedItem = getTableView().getItems().get(getIndex());
                    viewAllTransactions(selectedItem);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    public void loadUserHistory() {
        if (currentUser == null) return;
        List<String> allItemIds = AuctionRegistrationService.getRegisteredItemIdsForUser(currentUser.getId());
        List<Item> allItems = new ArrayList<>();
        for (String id : allItemIds) {
            Item item = ItemService.getItemById(id);
            if (item != null) {
                allItems.add(item);
            }
        }
        ObservableList<Item> data = FXCollections.observableArrayList(allItems);
        historyTable.setItems(data);
    }

    private void viewAllTransactions(Item currentItem) {
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
}
