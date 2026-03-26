package com.example.onlineauctionsystem.services;

import com.example.onlineauctionsystem.models.Art;
import com.example.onlineauctionsystem.models.AutoBid;
import com.example.onlineauctionsystem.models.Electronics;
import com.example.onlineauctionsystem.models.Item;
import com.example.onlineauctionsystem.utils.AuctionObserver;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    private static List<Item> itemList = new ArrayList<>();
    private static final String DIR_PATH = "data";
    private static final String FILE_PATH = DIR_PATH + "/items.txt";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static List<AuctionObserver> observers = new ArrayList<>();

    static {
        loadItemsFromFile();
    }

    public static void addObserver(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public static void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    private static void notifyObservers() {
        for (AuctionObserver observer: observers) {
            observer.onAuctionUpdate();
        }
    }

    public static void addItem(Item item) {
        itemList.add(item);
        saveItemsToFile();
        System.out.println("Đã thêm sản phẩm mới: " + item.getName());
    }

    public static List<Item> getAllItems() {
        return itemList;
    }

    public static List<Item> getItemsBySeller(String sellerId) {
        List<Item> sellerItems = new ArrayList<>();
        for (Item item: itemList) {
            if (item.getSellerId().equals(sellerId)) {
                sellerItems.add(item);
            }
        }
        return sellerItems;
    }

    public static Item getItemById(String id) {
        for (Item item: itemList) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public static boolean updateItemFull(Item updatedItem) {
        for (int i = 0; i < itemList.size(); i ++) {
            if (itemList.get(i).getId().equals(updatedItem.getId())) {
                itemList.set(i, updatedItem);
                saveItemsToFile();
                return true;
            }
        }
        return false;
    }

    public static boolean deleteItem(String id) {
        boolean isRemoved = itemList.removeIf(item -> item.getId().equals(id));
        if (isRemoved) {
            saveItemsToFile();
        }
        return isRemoved;
    }

    private static void saveItemsToFile() {
        File directory = new File(DIR_PATH);
        if (!directory.exists()) directory.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Item item: itemList) {
                String baseInfo = item.getId() + "," + item.getName() + "," + item.getDescription() + "," +
                        item.getStartingPrice() + "," + item.getCurrentHighestBid() + "," + item.getStartTime().format(FORMATTER) + "," +
                        item.getEndTime().format(FORMATTER) + "," + item.getSellerId() + "," + item.getCurrentWinnerId() + "," + item.getPaymentStatus();
                String line = "";
                if (item instanceof Electronics) {
                    Electronics e = (Electronics) item;
                    line = "Electronics," + baseInfo + "," + e.getBrand() + "," + e.getWarrantyMonths();
                } else if (item instanceof Art) {
                    Art a = (Art) item;
                    line = "Art," + baseInfo + "," + a.getArtist() + "," + a.getMaterial();
                }

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadItemsFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return ;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 11) return ;
                String type = data[0];
                String id = data[1];
                String name = data[2];
                String desc = data[3];
                double startPrice = Double.parseDouble(data[4]);
                double currentBid = Double.parseDouble(data[5]);
                LocalDateTime startTime = LocalDateTime.parse(data[6], FORMATTER);
                LocalDateTime endTime = LocalDateTime.parse(data[7], FORMATTER);
                String sellerId = data[8];
                String winnerId = data[9];
                String paymentStatus = data[10];
                Item loadedItem = null;
                if (type.equals("Electronics") && data.length >= 13) {
                    String brand = data[11];
                    int warranty = Integer.parseInt(data[12]);
                    loadedItem = new Electronics(name, desc, startPrice, startTime, endTime, sellerId, brand, warranty);
                } else if (type.equals("Art") && data.length >= 13) {
                    String artist = data[11];
                    String material = data[12];
                    loadedItem = new Art(name, desc, startPrice, startTime, endTime, sellerId, artist, material);
                }

                if (loadedItem != null) {
                    loadedItem.setId(id);
                    loadedItem.setCurrentHighestBid(currentBid);
                    loadedItem.setCurrentWinnerId(winnerId);
                    loadedItem.setPaymentStatus(paymentStatus);
                    itemList.add(loadedItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String placeBid(String itemId, String bidderId, double newBidAmount) {
        Item item = getItemById(itemId);
        if (item == null) return "Tài sản không tồn tại!";
        String status = item.getStatus();
        if (status.equals("Chờ bắt đầu")) return "Phiên đấu giá chưa mở cửa!";
        if (status.equals("Đã kết thúc")) return "Phiên đấu giá đã kết thúc!";

        if (item.getCurrentWinnerId().equals("NONE")) {
            if (newBidAmount < item.getStartingPrice()) {
                return "Lần trả giá đầu tiên không được thấp hơn Giá khởi điểm (" + item.getStartingPrice() + "$)!";
            }
        } else {
            if (newBidAmount < item.getCurrentHighestBid()) {
                return "Giá đặt phải cao hơn giá dẫn đầu hiện tại (" + item.getCurrentHighestBid() + "$)!";
            }
        }

        item.setCurrentHighestBid(newBidAmount);
        item.setCurrentWinnerId(bidderId);
        processAutoBiddingWar(item);
        saveItemsToFile();
        notifyObservers();
        return "SUCCESS";
    }

    public static boolean updatePaymentStatus(String itemId, String newStatus) {
        Item item = getItemById(itemId);
        if (item != null) {
            if (item.getStatus().equals("Đã kết thúc")) {
                item.setPaymentStatus(newStatus);
                saveItemsToFile();
                notifyObservers();
                return true;
            }
        }
        return false;
    }

    public static String registerAutoBid(String itemId, String userId, double maxBidAmount) {
        Item item = getItemById(itemId);
        if (item == null) return "Tài sản không tồn tại!";

        double minRequired = item.getCurrentWinnerId().equals("NONE")
                ? item.getStartingPrice()
                : item.getCurrentHighestBid() + item.getBidIncrement();

        if (maxBidAmount < minRequired) {
            return "Mức giá tối đa không được thấp hơn mức giá tối thiểu yêu cầu ($" + minRequired + ")!";
        }

        item.getAutoBids().add(new AutoBid(userId, maxBidAmount));
        processAutoBiddingWar(item);
        saveItemsToFile();
        notifyObservers();
        return "SUCCESS";
    }

    private static void processAutoBiddingWar(Item item) {
        boolean priceChange = true;
        while (priceChange) {
            priceChange = false;
            double requirePrice = item.getCurrentHighestBid() + item.getBidIncrement();
            AutoBid bestAutoBid = null;
            for (AutoBid ab: item.getAutoBids()) {
                if (ab.getUserId() == item.getCurrentWinnerId()) continue ;
                if (ab.getMaxBid() >= requirePrice) {
                    if (bestAutoBid == null) {
                        bestAutoBid = ab;
                    } else {
                        if (ab.getMaxBid() > bestAutoBid.getMaxBid()) {
                            bestAutoBid = ab;
                        } else if (ab.getMaxBid() == bestAutoBid.getMaxBid()) {
                            if (ab.getTimestamp().isBefore(bestAutoBid.getTimestamp())) {
                                bestAutoBid = ab;
                            }
                        }
                    }
                }
            }

            if (bestAutoBid != null) {
                item.setCurrentHighestBid(requirePrice);
                item.setCurrentWinnerId(bestAutoBid.getUserId());
                priceChange = true;
            }
        }
    }
}
