package com.example.onlineauctionsystem.models;

import javafx.fxml.FXML;

import java.time.LocalDateTime;

public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String email;
    protected String phone;

    protected String idNumber;
    protected String taxCode;
    protected String address;

    protected String province;
    protected String district;

    protected String frontIdPath = "";
    protected String backIdPath = "";

    protected boolean profileComplete = false;

    public User(String username, String password, String email, String phone) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;

        idNumber = "";
        taxCode = "";
        address = "";
        province = "";
        district = "";
    }

    public User(String id, LocalDateTime createdAt, String username, String password, String email, String phone, String idNumber, String taxCode,
                String address, String province, String district, boolean profileComplete) {
        super(id, createdAt);
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;

        this.idNumber = idNumber;
        this.taxCode = taxCode;
        this.address = address;
        this.province = province;
        this.district = district;
        this.profileComplete = profileComplete;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }

    public String getFrontIdPath() { return frontIdPath; }
    public void setFrontIdPath(String frontIdPath) { this.frontIdPath = frontIdPath; }

    public String getBackIdPath() { return backIdPath; }
    public void setBackIdPath(String backIdPath) { this.backIdPath = backIdPath; }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public String getIdNumber() { return idNumber; }
    public String getTaxCode() { return taxCode; }
    public String getAddress() { return address; }

    public String getProvince() { return province; }
    public String getDistrict() { return district; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public void setAddress(String address) { this.address = address; }

    public void setProvince(String province) { this.province = province; }
    public void setDistrict(String district) { this.district = district; }

    public abstract String getRole();
    public abstract void printInfo();

    public String toCSV() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%b,%s,%s",
                getRole(),
                getId(),
                getCreatedAt(),
                username,
                password,
                email,
                phone,
                idNumber,
                taxCode,
                address,
                province,
                district,
                profileComplete,
                frontIdPath,
                backIdPath
        );
    }
}
