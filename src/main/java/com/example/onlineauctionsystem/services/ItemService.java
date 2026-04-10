package com.example.onlineauctionsystem.services;

import com.example.onlineauctionsystem.models.*;
import com.example.onlineauctionsystem.utils.AuctionObserver;
import com.example.onlineauctionsystem.utils.DatabaseConnection;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service quản lý các nghiệp vụ liên quan đến Sản phẩm (Item) và Đấu giá (Bidding).
 * Đóng vai trò là cầu nối giữa Giao diện (UI) và Cơ sở dữ liệu (Database).
 */
public class ItemService {

    // =========================================================================================
    // 1. CÁC BIẾN TOÀN CỤC (GLOBAL VARIABLES) & KHỞI TẠO (INITIALIZATION)
    // =========================================================================================

    private static List<Item> itemList = new ArrayList<>();
    private static List<AuctionObserver> observers = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Quản lý khóa (Lock) cho từng sản phẩm để đảm bảo an toàn khi xử lý đồng thời (Concurrency)
    private static final ConcurrentHashMap<String, ReentrantLock> itemLocks = new ConcurrentHashMap<>();

    // Cấu hình luật chống bắn tỉa (Anti-Sniping)
    private static final int X_SECONDS = 30; // Ngưỡng thời gian cuối cùng (giây)
    private static final int Y_SECONDS = 60; // Thời gian cộng thêm (giây)

    static {
        initializeDatabase();
        loadItemsFromDatabase();
    }

    /**
     * Khởi tạo cấu trúc các bảng trong Database (nếu chưa có).
     */
    public static void initializeDatabase() {
        String createItemsSQL = "CREATE TABLE IF NOT EXISTS items ("
                + "id VARCHAR(50) PRIMARY KEY,"
                + "item_type VARCHAR(20),"
                + "name VARCHAR(100),"
                + "description TEXT,"
                + "starting_price REAL,"
                + "current_highest_bid REAL,"
                + "start_time VARCHAR(50),"
                + "end_time VARCHAR(50),"
                + "seller_id VARCHAR(50),"
                + "current_winner_id VARCHAR(50),"
                + "payment_status VARCHAR(20),"
                + "brand VARCHAR(50),"
                + "warranty_months INTEGER,"
                + "artist VARCHAR(50),"
                + "material VARCHAR(50)"
                + ");";

        String createBidHistorySQL = "CREATE TABLE IF NOT EXISTS bid_transactions ("
                + "id VARCHAR(50) PRIMARY KEY,"
                + "bid_time VARCHAR(50),"
                + "item_id VARCHAR(50),"
                + "bidder_id VARCHAR(50),"
                + "bid_amount REAL,"
                + "bid_type VARCHAR(20)"
                + ");";

        String createAutoBidsSQL = "CREATE TABLE IF NOT EXISTS auto_bids ("
                + "id VARCHAR(50) PRIMARY KEY,"
                + "item_id VARCHAR(50),"
                + "user_id VARCHAR(50),"
                + "max_bid REAL,"
                + "timestamp VARCHAR(50)"
                + ");";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createItemsSQL);
            stmt.execute(createBidHistorySQL);
            stmt.execute(createAutoBidsSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================================================================
    // 2. MÔ HÌNH OBSERVER (LẮNG NGHE & CẬP NHẬT GIAO DIỆN)
    // =========================================================================================

    /**
     * Đăng ký một Giao diện (Observer) để nhận thông báo khi có thay đổi dữ liệu.
     */
    public static void addObserver(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Hủy đăng ký một Giao diện (Observer).
     */
    public static void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    /**
     * Phát tín hiệu thông báo cho tất cả các Observer đã đăng ký để chúng làm mới dữ liệu.
     */
    private static void notifyObservers() {
        for (AuctionObserver observer : observers) {
            observer.onAuctionUpdate();
        }
    }


    // =========================================================================================
    // 3. TRUY VẤN DỮ LIỆU TỪ BỘ NHỚ RAM (QUERIES FROM CACHE)
    // =========================================================================================

    /**
     * Lấy toàn bộ danh sách sản phẩm.
     */
    public static List<Item> getAllItems() {
        return itemList;
    }

    /**
     * Lấy danh sách sản phẩm của một người bán cụ thể.
     */
    public static List<Item> getItemsBySeller(String sellerId) {
        List<Item> sellerItems = new ArrayList<>();
        for (Item item : itemList) {
            if (item.getSellerId().equals(sellerId)) {
                sellerItems.add(item);
            }
        }
        return sellerItems;
    }

    /**
     * Tìm kiếm một sản phẩm theo ID.
     */
    public static Item getItemById(String id) {
        for (Item item : itemList) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Tiện ích: Lấy khóa (Lock) tương ứng với ID sản phẩm.
     */
    private static ReentrantLock getLockFromItem(String itemId) {
        return itemLocks.computeIfAbsent(itemId, k -> new ReentrantLock(true));
    }


    // =========================================================================================
    // 4. LOGIC NGHIỆP VỤ CHÍNH (CORE BUSINESS LOGIC)
    // =========================================================================================

    /**
     * NGHIỆP VỤ: Đặt giá thủ công (Manual Bid).
     * @return Thông báo kết quả thao tác.
     */
    public static String placeBid(String itemId, String bidderId, double newBidAmount) {
        ReentrantLock lock = getLockFromItem(itemId);
        lock.lock(); // Khóa sản phẩm, đảm bảo chỉ 1 người đặt giá tại 1 thời điểm
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

            // Ghi nhận giao dịch thủ công
            BidTransaction newTx = new BidTransaction(itemId, bidderId, newBidAmount, BidTransaction.BidType.MANUAL);
            item.addTransaction(newTx);
            saveTransaction(newTx);

            // Cập nhật trạng thái người thắng hiện tại
            item.setCurrentHighestBid(newBidAmount);
            item.setCurrentWinnerId(bidderId);

            // Chạy logic các tính năng tự động
            processAutoBiddingWar(item);
            applyAntiSniping(item);

            // Chốt thay đổi cuối cùng xuống DB
            boolean isDbSuccess = updateItemInDatabase(item);
            if (isDbSuccess) {
                notifyObservers();
                return "SUCCESS";
            } else {
                return "Lỗi hệ thống: Không thể lưu kết quả đấu giá!";
            }
        } finally {
            lock.unlock(); // Giải phóng khóa
        }
    }

    /**
     * NGHIỆP VỤ: Đăng ký đặt giá tự động (Auto-Bid).
     */
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

            // Ghi nhận cấu hình Auto-Bid mới
            AutoBid newAutoBid = new AutoBid(itemId, userId, maxBidAmount);
            item.getAutoBids().add(newAutoBid);
            saveAutoBidToDatabase(newAutoBid);

            // Kiểm tra xem cấu hình mới này có tác động ngay lập tức không
            processAutoBiddingWar(item);
            applyAntiSniping(item);

            updateItemInDatabase(item);
            notifyObservers();
            return "SUCCESS";
        } finally {
            lock.unlock();
        }
    }

    /**
     * TÍNH NĂNG TỰ ĐỘNG: Xử lý cuộc chiến giữa các luồng Auto-Bid.
     * Liên tục kiểm tra xem có AutoBid nào có thể vượt mức giá yêu cầu hiện tại hay không.
     */
    private static void processAutoBiddingWar(Item item) {
        boolean priceChange = true;
        while (priceChange) {
            priceChange = false;
            double requirePrice = item.getCurrentWinnerId().equals("NONE")
                    ? item.getStartingPrice()
                    : item.getCurrentHighestBid() + item.getBidIncrement();

            AutoBid bestAutoBid = null;

            for (AutoBid ab : item.getAutoBids()) {
                if (ab.getUserId().equals(item.getCurrentWinnerId())) continue; // Bỏ qua người đang giữ giá cao nhất

                if (ab.getMaxBid() >= requirePrice) {
                    if (bestAutoBid == null) {
                        bestAutoBid = ab;
                    } else {
                        // Ưu tiên người có MaxBid cao hơn, nếu bằng nhau thì ưu tiên người cài đặt sớm hơn
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

            // Nếu tìm thấy AutoBid phù hợp, thực hiện đặt giá tự động
            if (bestAutoBid != null) {
                BidTransaction autoTx = new BidTransaction(
                        item.getId(),
                        bestAutoBid.getUserId(),
                        requirePrice,
                        BidTransaction.BidType.AUTO);

                item.addTransaction(autoTx);
                saveTransaction(autoTx);
                priceChange = true; // Tiếp tục vòng lặp để xem có AutoBid khác chặn lại không
            }
        }
    }

    /**
     * TÍNH NĂNG TỰ ĐỘNG: Luật chống bắn tỉa (Anti-Sniping).
     * Tự động cộng thêm thời gian nếu có lượt đặt giá ở những giây cuối cùng.
     */
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


    // =========================================================================================
    // 5. THAO TÁC CƠ SỞ DỮ LIỆU & ĐỒNG BỘ RAM (DATABASE CRUD OPERATIONS)
    // =========================================================================================

    /**
     * NGHIỆP VỤ: Đăng bán sản phẩm mới.
     */
    public static void addItem(Item item) {
        itemList.add(item);
        String sql = "INSERT INTO items (id, item_type, name, description, " +
                "starting_price, current_highest_bid, start_time, end_time, " +
                "seller_id, current_winner_id, payment_status, " +
                "brand, warranty_months, artist, material) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getClass().getSimpleName()); // Lấy tên Class làm type
            pstmt.setString(3, item.getName());
            pstmt.setString(4, item.getDescription());
            pstmt.setDouble(5, item.getStartingPrice());
            pstmt.setDouble(6, item.getCurrentHighestBid());
            pstmt.setString(7, item.getStartTime().format(FORMATTER));
            pstmt.setString(8, item.getEndTime().format(FORMATTER));
            pstmt.setString(9, item.getSellerId());
            pstmt.setString(10, item.getCurrentWinnerId());
            pstmt.setString(11, item.getPaymentStatus());

            pstmt.setNull(12, Types.VARCHAR);
            pstmt.setNull(13, Types.INTEGER);
            pstmt.setNull(14, Types.VARCHAR);
            pstmt.setNull(15, Types.VARCHAR);

            if (item instanceof Electronics) {
                pstmt.setString(12, ((Electronics) item).getBrand());
                pstmt.setInt(13, ((Electronics) item).getWarrantyMonths());
            } else if (item instanceof Art) {
                pstmt.setString(14, ((Art) item).getArtist());
                pstmt.setString(15, ((Art) item).getMaterial());
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        notifyObservers();
    }

    /**
     * NGHIỆP VỤ: Admin chỉnh sửa thông tin chi tiết của Sản phẩm.
     */
    public static boolean updateItemDetails(Item updateItem) {
        boolean isDbSuccess = updateItemInDatabase(updateItem);
        if (isDbSuccess) {
            for (int i = 0; i < itemList.size(); i++) {
                if (itemList.get(i).getId().equals(updateItem.getId())) {
                    itemList.set(i, updateItem);
                    break;
                }
            }
            notifyObservers();
            return true;
        }
        return false;
    }

    /**
     * NGHIỆP VỤ: Cập nhật trạng thái thanh toán của Sản phẩm (sau khi đã kết thúc).
     */
    public static boolean updatePaymentStatus(String itemId, String newStatus) {
        Item item = getItemById(itemId);
        if (item != null && item.getStatus().equals("Đã kết thúc")) {
            String oldStatus = item.getPaymentStatus(); // Backup trạng thái cũ
            item.setPaymentStatus(newStatus); // Set tạm trên RAM

            boolean isDbSuccess = updateItemInDatabase(item);
            if (isDbSuccess) {
                notifyObservers();
                return true;
            } else {
                item.setPaymentStatus(oldStatus); // Rollback nếu DB lỗi
                return false;
            }
        }
        return false;
    }

    /**
     * NGHIỆP VỤ: Admin xóa sản phẩm và toàn bộ dữ liệu liên quan.
     */
    public static boolean deleteItem(String id) {
        String deleteAutoBids = "DELETE FROM auto_bids WHERE item_id = ?";
        String deleteBidHistory = "DELETE FROM bid_transactions WHERE item_id = ?";
        String deleteItem = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction để đảm bảo tính toàn vẹn

            try (PreparedStatement pstmtAuto = conn.prepareStatement(deleteAutoBids);
                 PreparedStatement pstmtHistory = conn.prepareStatement(deleteBidHistory);
                 PreparedStatement pstmtItem = conn.prepareStatement(deleteItem)) {

                pstmtAuto.setString(1, id); pstmtAuto.executeUpdate();
                pstmtHistory.setString(1, id); pstmtHistory.executeUpdate();
                pstmtItem.setString(1, id); pstmtItem.executeUpdate();

                conn.commit(); // Hoàn tất Transaction

                itemList.removeIf(item -> item.getId().equals(id)); // Cập nhật RAM
                notifyObservers();

                System.out.println("Đã xóa hoàn toàn sản phẩm và các giao dịch liên quan: " + id);
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Hủy bỏ Transaction nếu có lỗi
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HỆ THỐNG: Kéo toàn bộ dữ liệu từ Database lên RAM khi khởi động ứng dụng.
     */
    private static void loadItemsFromDatabase() {
        itemList.clear();
        String sql = "SELECT * FROM items";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String type = rs.getString("item_type");
                String id = rs.getString("id");
                String name = rs.getString("name");
                String desc = rs.getString("description");
                double startPrice = rs.getDouble("starting_price");
                double currentBid = rs.getDouble("current_highest_bid");
                LocalDateTime startTime = LocalDateTime.parse(rs.getString("start_time"), FORMATTER);
                LocalDateTime endTime = LocalDateTime.parse(rs.getString("end_time"), FORMATTER);
                String sellerId = rs.getString("seller_id");
                String winnerId = rs.getString("current_winner_id");
                String paymentStatus = rs.getString("payment_status");

                Item loadedItem = null;
                if ("Electronics".equals(type)) {
                    loadedItem = new Electronics(name, desc, startPrice, startTime, endTime, sellerId, rs.getString("brand"), rs.getInt("warranty_months"));
                } else if ("Art".equals(type)) {
                    loadedItem = new Art(name, desc, startPrice, startTime, endTime, sellerId, rs.getString("artist"), rs.getString("material"));
                }

                if (loadedItem != null) {
                    loadedItem.setId(id);
                    loadedItem.setCurrentHighestBid(currentBid);
                    loadedItem.setCurrentWinnerId(winnerId);
                    loadedItem.setPaymentStatus(paymentStatus);

                    // Tải danh sách AutoBids
                    String sqlAutoBids = "SELECT * FROM auto_bids WHERE item_id = ?";
                    try (PreparedStatement pstmtAuto = conn.prepareStatement(sqlAutoBids)) {
                        pstmtAuto.setString(1, id);
                        ResultSet rsAuto = pstmtAuto.executeQuery();
                        while (rsAuto.next()) {
                            loadedItem.getAutoBids().add(new AutoBid(id,
                                    rsAuto.getString("user_id"),
                                    rsAuto.getDouble("max_bid")
                            ));
                        }
                    }

                    // Tải Lịch sử giao dịch
                    loadedItem.setBidHistory(loadHistoryForItem(id));
                    itemList.add(loadedItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * HỆ THỐNG: Tải lịch sử giao dịch của một sản phẩm từ DB.
     */
    public static List<BidTransaction> loadHistoryForItem(String itemId) {
        List<BidTransaction> history = new ArrayList<>();
        String sql = "SELECT * FROM bid_transactions WHERE item_id = ? ORDER BY bid_time";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                history.add(new BidTransaction(
                        rs.getString("id"),
                        LocalDateTime.parse(rs.getString("bid_time"), FORMATTER),
                        rs.getString("item_id"),
                        rs.getString("bidder_id"),
                        rs.getDouble("bid_amount"),
                        BidTransaction.BidType.valueOf(rs.getString("bid_type"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    /**
     * HỆ THỐNG: Cập nhật thông số giá và thời gian của Sản phẩm xuống DB.
     */
    private static boolean updateItemInDatabase(Item item) {
        String sql = "UPDATE items SET "
                + "current_highest_bid = ?, current_winner_id = ?, end_time = ?, "
                + "payment_status = ? "
                + "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, item.getCurrentHighestBid());
            pstmt.setString(2, item.getCurrentWinnerId());
            pstmt.setString(3, item.getEndTime().format(FORMATTER));
            pstmt.setString(4, item.getPaymentStatus());
            pstmt.setString(5, item.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HỆ THỐNG: Lưu một Giao dịch (Transaction) mới xuống DB.
     */
    public static void saveTransaction(BidTransaction tx) {
        String sql = "INSERT INTO bid_transactions (id, bid_time, item_id, bidder_id, bid_amount, bid_type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tx.getId());
            pstmt.setString(2, tx.getCreatedAt().format(FORMATTER)); // Chỉnh sửa để format thời gian thống nhất
            pstmt.setString(3, tx.getItemId());
            pstmt.setString(4, tx.getBidderId());
            pstmt.setDouble(5, tx.getBidAmount());
            pstmt.setString(6, tx.getBidType().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * HỆ THỐNG: Lưu cấu hình Auto-Bid mới xuống DB.
     */
    public static void saveAutoBidToDatabase(AutoBid autoBid) {
        String sql = "INSERT INTO auto_bids (item_id, user_id, max_bid, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, autoBid.getItemId());
            pstmt.setString(2, autoBid.getUserId());
            pstmt.setDouble(3, autoBid.getMaxBid());
            pstmt.setString(4, autoBid.getTimestamp().format(FORMATTER));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}