package com.example.onlineauctionsystem.services;

import com.example.onlineauctionsystem.models.*;
import com.example.onlineauctionsystem.utils.UserFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static List<User> userList = new ArrayList<>();
    private static final String DATA_DIR = System.getProperty("user.dir") + File.separator + "data";
    private static final String FILE_PATH = DATA_DIR + File.separator + "users.txt";

    static {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        loadUsersFromFile();

        if (userList.isEmpty()) {
            userList.add(new Admin("admin", "123", "admin@gmail.com", "0853315868"));
            userList.add(new Bidder("nguyenvana", "123", "vana@gmail.com", "0901234567"));
            userList.add(new Seller("cty_thanhly", "123", "contact@thanhly.vn", "0243123456"));
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

    public static boolean register(String username, String password, String email, String phone, String role) {
        for (User user: userList) {
            if (user.getEmail().equals(email) || user.getPhone().equals(phone)) {
                return false;
            }
        }

        User newUser = UserFactory.createUser(role, username, password, email, phone);
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

    public static boolean updateProfile(User updateUser) {
        for (int i = 0; i < userList.size(); i ++) {
            if (userList.get(i).getId().equals(updateUser.getId())) {
                userList.set(i, updateUser);
                saveUsersFromFile();
                return true;
            }
        }
        return false;
    }

    private static void saveUsersFromFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user: userList) {
                writer.write(user.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadUsersFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return ;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String role = parts[0];
                    String id = parts[1];
                    String createdAt = parts[2];
                    String username = parts[3];
                    String password = parts[4];
                    String email = parts[5];
                    String phone = parts[6];
                    User loadedUser = null;
                    if (role.equals("Admin")) {
                        loadedUser = new Admin(username, password, email, phone);
                    } else if (role.equals("Bidder")) {
                        loadedUser = new Bidder(username, password, email, phone);
                        if (parts.length > 15) ((Bidder) loadedUser).setWalletBalance(Double.parseDouble(parts[15]));
                    } else if (role.equals("Seller")) {
                        loadedUser = new Seller(username, password, email, phone);
                        if (parts.length > 15) ((Seller) loadedUser).setReputationScore(Double.parseDouble(parts[15]));
                    }

                    if (loadedUser != null) {
                        loadedUser.setId(id);
                        loadedUser.setCreatedAt(LocalDateTime.parse(createdAt));
                        if (parts.length >= 15) {
                            loadedUser.setIdNumber(parts[7]);
                            loadedUser.setTaxCode(parts[8]);
                            loadedUser.setAddress(parts[9]);
                            loadedUser.setProvince(parts[10]);
                            loadedUser.setDistrict(parts[11]);
                            loadedUser.setProfileComplete(Boolean.parseBoolean(parts[12]));
                            loadedUser.setFrontIdPath(parts[13]);
                            loadedUser.setBackIdPath(parts[14]);
                        }
                        userList.add(loadedUser);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static List<User> getUserList() {
        return userList;
    }
}
