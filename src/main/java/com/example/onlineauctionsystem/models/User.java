package com.example.onlineauctionsystem.models;

public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String email;
    protected String phone;

    protected String idNumber = "";
    protected String taxCode = "";
    protected String address = "";
    protected String province = "";
    protected String district = "";
    protected String idIssueDate = "";
    protected String idIssuePlace = "";
    protected String frontIdPath = "";
    protected String backIdPath = "";
    protected String bankAccountName = "";
    protected String bankAccountNumber = "";
    protected String bankName = "";

    protected boolean profileComplete = false;

    public User(String username, String password, String email, String phone) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
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

    public String getIdIssueDate() { return idIssueDate; }
    public String getIdIssuePlace() { return idIssuePlace; }

    public String getBankAccountName() { return bankAccountName; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public String getBankName() { return bankName; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public void setAddress(String address) { this.address = address; }

    public void setProvince(String province) { this.province = province; }
    public void setDistrict(String district) { this.district = district; }

    public void setIdIssueDate(String idIssueDate) { this.idIssueDate = idIssueDate; }
    public void setIdIssuePlace(String idIssuePlace) { this.idIssuePlace = idIssuePlace; }
    public void setBankAccountName(String bankAccountName) { this.bankAccountName = bankAccountName; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public abstract String getRole();
    public abstract void printInfo();
}
