package com.example.onlineauctionsystem.services;

import com.example.onlineauctionsystem.models.*;
import com.example.onlineauctionsystem.utils.AuctionObserver;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ItemService {
    private static List<Item> itemList = new ArrayList<>();
    private static final String DIR_PATH = "data";
    private static final String FILE_PATH = DIR_PATH + "/items.txt";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static List<AuctionObserver> observers = new ArrayList<>();

    private static final ConcurrentHashMap<String, ReentrantLock> itemLocks = new ConcurrentHashMap<>();
    private static final Object FILE_LOCK = new Object();

    private static final int X_SECONDS = 30;
    private static final int Y_SECONDS = 60;

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
        notifyObservers();
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
                notifyObservers();
                return true;
            }
        }
        return false;
    }

    public static boolean deleteItem(String id) {
        boolean isRemoved = itemList.removeIf(item -> item.getId().equals(id));
        if (isRemoved) {
            saveItemsToFile();
            notifyObservers();
        }
        return isRemoved;
    }

    private static boolean saveItemsToFile() {
        synchronized (FILE_LOCK) {
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

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static void loadItemsFromFile() {
        synchronized (FILE_LOCK) {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length < 11) return;
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
    }

    private static void endureDirectoryExists(String DATA_DIR) {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static List<BidTransaction> loadHistoryForItem(String itemId) {
        ReentrantLock lock = getLockFromItem(itemId);
        lock.lock();

        try {
            endureDirectoryExists("data/history/");
            List<BidTransaction> history = new ArrayList<>();
            File file = new File("data/history/history_" + itemId + ".txt");
            if (!file.exists()) return history;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        history.add(new BidTransaction(parts[0],
                                LocalDateTime.parse(parts[1]),
                                parts[2],
                                parts[3],
                                Double.parseDouble(parts[4]),
                                BidTransaction.BidType.valueOf(parts[5])
                        ));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return history;
        } finally {
            lock.unlock();
        }
    }

    public static void saveTransaction(BidTransaction tx) {
        endureDirectoryExists("data/history/");
        String fileName = "data/history/history_" + tx.getItemId() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(tx.toCSV());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static void processAutoBiddingWar(Item item) {
        boolean priceChange = true;
        while (priceChange) {
            priceChange = false;
            double requirePrice = item.getCurrentWinnerId().equals("NONE")
                    ? item.getStartingPrice()
                    : item.getCurrentHighestBid() + item.getBidIncrement();
            AutoBid bestAutoBid = null;
            for (AutoBid ab: item.getAutoBids()) {
                if (ab.getUserId().equals(item.getCurrentWinnerId())) continue ;
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
                BidTransaction autoTx = new BidTransaction(
                        item.getId(),
                        bestAutoBid.getUserId(),
                        requirePrice,
                        BidTransaction.BidType.AUTO);

                item.addTransaction(autoTx);
                saveTransaction(autoTx);
                priceChange = true;
            }
        }
    }

    private static ReentrantLock getLockFromItem(String itemId) {
        return itemLocks.computeIfAbsent(itemId, k -> new ReentrantLock(true));
    }

    public static String placeBid(String itemId, String bidderId, double newBidAmount) {
        ReentrantLock lock = getLockFromItem(itemId);
        lock.lock();
        try {
            Item item = getItemById(itemId);
            if (item == null) return "Sản phẩm không tồn tại!";

            String status = item.getStatus();
            if (status.equals("Chờ bắt đầu")) return "Phiên đấu giá chưa mở cửa!";
            if (status.equals("Đã kết thúc")) return "Phiên đấu giá đã kết thúc!";

            double minRequired = item.getCurrentWinnerId().equals("NONE")
                    ? item.getStartingPrice()
                    : item.getCurrentHighestBid() + item.getBidIncrement();
            if (newBidAmount < minRequired) {
                return "Giá đặt tối thiểu phải là " + minRequired + "$!";
            }

            BidTransaction newTx = new BidTransaction(itemId, bidderId, newBidAmount, BidTransaction.BidType.MANUAL);
            item.addTransaction(newTx);
            saveTransaction(newTx);

            item.setCurrentHighestBid(newBidAmount);
            item.setCurrentWinnerId(bidderId);
            processAutoBiddingWar(item);
            applyAntiSniping(item);

            boolean success = saveItemsToFile();
            if (!success) return "Lỗi hệ thống: Không thể lưu kết quả đấu giá!";
            notifyObservers();
            return "SUCCESS";
        } finally {
            lock.unlock();
        }
    }

    public static String registerAutoBid(String itemId, String userId, double maxBidAmount) {
        ReentrantLock lock = getLockFromItem(itemId);
        lock.lock();
        try {
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
            applyAntiSniping(item);

            boolean success = saveItemsToFile();
            if (!success) return "Lỗi hệ thống: Không thể lưu cấu hình AutoBid!";
            notifyObservers();
            return "SUCCESS";
        } finally {
            lock.unlock();
        }
    }

    private static void applyAntiSniping(Item item) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = item.getEndTime();
        Duration duration = Duration.between(now, endTime);
        long secondsRemaining = duration.getSeconds();
        if (secondsRemaining > 0 && secondsRemaining <= X_SECONDS) {
            LocalDateTime newEndTime = endTime.plusSeconds(Y_SECONDS);
            item.setEndTime(newEndTime);
        }
    }
}
