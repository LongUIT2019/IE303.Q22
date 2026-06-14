package com.example.taxi;

import java.util.ArrayList;
import java.util.List;

public class AppUser {
    private List<FavoriteLocation> favorites = new ArrayList<>();

    private String id; // document ID trong Firestore (= username)
    private String username;
    private String password;
    private String role; // ROLE_CUSTOMER, ROLE_DRIVER, ROLE_ADMIN
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean active = false;
    private String otpCode;
    private String otpExpiry; // Lưu dạng String ISO để Firestore dễ xử lý
    private boolean online = false;

    public AppUser() {
    }

    public AppUser(String username, String password, String role, String email, String fullName, String phoneNumber) {
        this.id = username; // dùng username làm document ID
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(String otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public List<FavoriteLocation> getFavorites() {
        if (favorites == null) favorites = new ArrayList<>();
        return favorites;
    }

    public void setFavorites(List<FavoriteLocation> favorites) {
        this.favorites = favorites;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
