package com.example.onlineauctionsystem.services;

import com.example.onlineauctionsystem.models.*;
import com.example.onlineauctionsystem.utils.DatabaseConnection;
import com.example.onlineauctionsystem.utils.UserFactory;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id VARCHAR(20) PRIMARY KEY,"
                + "role VARCHAR(20),"
                + "createdAt VARCHAR(50),"
                + "username VARCHAR(50) UNIQUE,"
                + "password VARCHAR(255),"
                + "email VARCHAR(100),"
                + "phone VARCHAR(20),"
                + "idNumber VARCHAR(20),"
                + "taxCode VARCHAR(50),"
                + "address TEXT,"
                + "province VARCHAR(50),"
                + "district VARCHAR(50),"
                + "profileComplete BOOLEAN,"
                + "idIssueDate VARCHAR(50),"
                + "idIssuePlace VARCHAR(100),"
                + "frontIdPath TEXT,"
                + "backIdPath TEXT,"
                + "bankAccountName VARCHAR(100),"
                + "bankAccountNumber VARCHAR(50),"
                + "bankName VARCHAR(50),"
                + "walletBalance REAL DEFAULT 0.0,"
                + "reputationScore REAL DEFAULT 0.0"
                + ");";
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        insertDefaultAdmin();
    }

    private static void insertDefaultAdmin() {
        String checkEmptySQL = "SELECT count(*) FROM users";
        boolean isEmpty = false;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkEmptySQL)) {
            if (rs.next() && rs.getInt(1) == 0) {
                isEmpty = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (isEmpty) {
            System.out.println("Database trống. Đang tạo dữ liệu mẫu...");
            register("admin", "123", "admin@gmail.com", "0853315868", "Admin");
            register("nguyenvana", "123", "vana@gmail.com", "0901234567", "Bidder");
            register("cty_thanhly", "123", "contact@thanhly.vn", "0243123456", "Seller");
        }
    }

    public static User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean register(String username, String password, String email, String phone, String role) {
        String checkSql = "SELECT id FROM users WHERE email = ? OR phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, email);
            checkStmt.setString(2, phone);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        User newUser = UserFactory.createUser(role, username, password, email, phone);
        String insertSql = "INSERT INTO users (id, role, createdAt, username, password, email, phone, profileComplete)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            pstmt.setString(1, newUser.getId());
            pstmt.setString(2, newUser.getRole());
            pstmt.setString(3, newUser.getCreatedAt().toString());
            pstmt.setString(4, newUser.getUsername());
            pstmt.setString(5, newUser.getPassword());
            pstmt.setString(6, newUser.getEmail());
            pstmt.setString(7, newUser.getPhone());
            pstmt.setBoolean(8, newUser.isProfileComplete());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateProfile(User user) {
        String sql = "UPDATE users SET "
                + "username = ?, password = ?, email = ?, phone = ?, "
                + "idNumber = ?, taxCode = ?, address = ?, province = ?, district = ?, "
                + "profileComplete = ?, "
                + "idIssueDate = ?, idIssuePlace = ?, frontIdPath = ?, backIdPath = ?, "
                + "backAccountName = ?, bankAccountNumber = ?, bankName = ? "
                + "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getIdNumber());
            pstmt.setString(6, user.getTaxCode());
            pstmt.setString(7, user.getAddress());
            pstmt.setString(8, user.getProvince());
            pstmt.setString(9, user.getDistrict());
            pstmt.setBoolean(10, user.isProfileComplete());
            pstmt.setString(11, user.getIdIssueDate());
            pstmt.setString(12, user.getIdIssuePlace());
            pstmt.setString(13, user.getFrontIdPath());
            pstmt.setString(14, user.getBackIdPath());
            pstmt.setString(15, user.getBankAccountName());
            pstmt.setString(16, user.getBankAccountNumber());
            pstmt.setString(17, user.getBankName());

            pstmt.setString(18, user.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user = null;

        if ("Admin".equals(role)) {
            user = new Admin(rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getString("phone"));
        } else if ("Bidder".equals(role)) {
            user = new Bidder(rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getString("phone"));
            ((Bidder) user).setWalletBalance(rs.getDouble("walletBalance"));
        } else if ("Seller".equals(role)) {
            user = new Seller(rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getString("phone"));
            ((Seller) user).setReputationScore(rs.getDouble("reputationScore"));
        }

        if (user != null) {
            user.setId(rs.getString("id"));
            user.setCreatedAt(LocalDateTime.parse(rs.getString("createdAt")));
            user.setIdNumber(rs.getString("idNumber"));
            user.setTaxCode(rs.getString("taxCode"));
            user.setAddress(rs.getString("address"));
            user.setProvince(rs.getString("province"));
            user.setDistrict(rs.getString("district"));
            user.setProfileComplete(rs.getBoolean("profileComplete"));
            user.setIdIssueDate(rs.getString("idIssueDate"));
            user.setIdIssuePlace(rs.getString("idIssuePlace"));
            user.setFrontIdPath(rs.getString("frontIdPath"));
            user.setBackIdPath(rs.getString("backIdPath"));
            user.setBankAccountName(rs.getString("bankAccountName"));
            user.setBankAccountName(rs.getString("bankAccountNumber"));
            user.setBankName(rs.getString("bankName"));
        }
        return user;
    }
}
