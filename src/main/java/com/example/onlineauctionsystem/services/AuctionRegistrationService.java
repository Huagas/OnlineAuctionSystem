package com.example.onlineauctionsystem.services;

import com.example.onlineauctionsystem.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service quản lý việc Đăng ký tham gia đấu giá (Junction Table).
 * Cầu nối giữa User (Người dùng) và Item (Sản phẩm).
 */
public class AuctionRegistrationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        initializeDatabase();
    }

    /**
     * Tự động tạo bảng trung gian nếu chưa tồn tại.
     */
    public static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS auction_registrations ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id VARCHAR(50), "
                + "item_id VARCHAR(50), "
                + "deposit_amount REAL, "
                + "registration_time VARCHAR(50), "
                + "UNIQUE(user_id, item_id)" // Đảm bảo 1 user chỉ đăng ký 1 item 1 lần
                + ");";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * NGHIỆP VỤ 1: Đăng ký tham gia một phiên đấu giá (Thêm bản ghi vào DB).
     */
    public static boolean registerUserForItem(String userId, String itemId, double depositAmount) {
        String sql = "INSERT INTO auction_registrations (user_id, item_id, deposit_amount, registration_time) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, itemId);
            pstmt.setDouble(3, depositAmount);
            pstmt.setString(4, LocalDateTime.now().format(FORMATTER));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Lỗi có thể do User đã đăng ký rồi (Vi phạm UNIQUE constraint)
            System.out.println("Lỗi đăng ký: Người dùng đã đăng ký sản phẩm này rồi, hoặc lỗi DB.");
            return false;
        }
    }

    /**
     * NGHIỆP VỤ 2: Kiểm tra xem User đã đăng ký tham gia Item này chưa?
     * (Dùng để ẩn/hiện nút "Đặt giá" trên giao diện).
     */
    public static boolean isUserRegistered(String userId, String itemId) {
        String sql = "SELECT count(*) FROM auction_registrations WHERE user_id = ? AND item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * NGHIỆP VỤ 3: Lấy danh sách ID của tất cả các sản phẩm mà User ĐÃ ĐĂNG KÝ.
     * (Dùng cho tab "Sản phẩm đang theo dõi" của người dùng).
     */
    public static List<String> getRegisteredItemIdsForUser(String userId) {
        List<String> registeredItemIds = new ArrayList<>();
        String sql = "SELECT item_id FROM auction_registrations WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                registeredItemIds.add(rs.getString("item_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registeredItemIds;
    }
}