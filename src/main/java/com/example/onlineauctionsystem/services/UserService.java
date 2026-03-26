package com.example.onlineauctionsystem.services;

import com.example.onlineauctionsystem.models.*;
import com.example.onlineauctionsystem.utils.UserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static List<User> userList = new ArrayList<>();
    private static final String DIR_PATH = "data";
    private static final String FILE_PATH = DIR_PATH + "/users.txt";

    static {
        loadUsersFromFile();

        if (userList.isEmpty()) {
            userList.add(new Admin("admin", "123", "admin@gmail.com", "0853315868", "Tổ chức", 1));
            userList.add(new Bidder("nguyenvana", "123", "vana@gmail.com", "0901234567", "Cá nhân"));
            userList.add(new Seller("cty_thanhly", "123", "contact@thanhly.vn", "0243123456", "Tổ chức"));
            saveUsersFromFile();
        }
    }

    public static User login(String username, String password) {
        for (User user: userList) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public static boolean register(String username, String password, String email, String phone, String accountType, String role) {
        for (User user: userList) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }

        User newUser = UserFactory.createUser(role, username, password, email, phone, accountType);
        userList.add(newUser);
        saveUsersFromFile();
        newUser.printInfo();
        return true;
    }

    public static User getUserById(String id) {
        for (User user: userList) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    private static void saveUsersFromFile() {
        File directory = new File(DIR_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user: userList) {
                String line = user.getRole() + "," +
                        user.getId() + "," +
                        user.getUsername() + "," +
                        user.getPassword() + "," +
                        user.getEmail() + "," +
                        user.getPhone() + "," +
                        user.getAccountType();
                if (user instanceof Admin) {
                    line += ",1";
                }
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadUsersFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return ;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String role = parts[0];
                    String id = parts[1];
                    String username = parts[2];
                    String password = parts[3];
                    String email = parts[4];
                    String phone = parts[5];
                    String accountType = parts[6];
                    User loadedUser = null;
                    if (role.equals("Admin")) {
                        int level = (parts.length == 8) ? Integer.parseInt(parts[7]) : 1;
                        loadedUser = new Admin(username, password, email, phone, accountType, level);
                    } else if (role.equals("Bidder")) {
                        loadedUser = new Bidder(username, password, email, phone, accountType);
                    } else if (role.equals("Seller")) {
                        loadedUser = new Seller(username, password, email, phone, accountType);
                    }

                    if (loadedUser != null) {
                        loadedUser.setId(id);
                        userList.add(loadedUser);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<User> getUserList() {
        return userList;
    }
}
