package com.example.onlineauctionsystem.utils;

import com.example.onlineauctionsystem.models.*;

public class UserFactory {
    public static User createUser(String role, String username, String password, String email, String phone, String accountType) {
        if (role == null) {
            return null;
        }

        switch (role.toUpperCase()) {
            case "BIDDER":
                return new Bidder(username, password, email, phone, accountType);
            case "SELLER":
                return new Seller(username, password, email, phone, accountType);
            case "ADMIN":
                return new Admin(username, password, email, phone, accountType, 1);
            default:
                throw new IllegalArgumentException("Lỗi: Không tìm thấy vai trò (role) phù hợp: " + role);
        }
    }
}
