package com.soarclient.management.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.soarclient.Soar;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class AuthManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path USER_DATA_FILE = FabricLoader.getInstance().getConfigDir();
    private static AuthManager instance;

    private HashMap<Object, Object> users = new HashMap<>();
    private UserData currentUser;

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    private AuthManager() {
        loadUserData();
    }

    public static class UserData {
        private String username;
        private String passwordHash;
        private long createdAt;
        private long lastLogin;

        public UserData() {}

        public UserData(String username, String password) {
            this.username = username;
            this.passwordHash = simpleHash(password);
            this.createdAt = System.currentTimeMillis();
            this.lastLogin = System.currentTimeMillis();
        }

        private String simpleHash(String input) {
            return Integer.toHexString(input.hashCode());
        }

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
        public long getLastLogin() { return lastLogin; }
        public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
    }

    private void loadUserData() {
        try {
            if (Files.exists(USER_DATA_FILE)) {
                String json = new String(Files.readAllBytes(USER_DATA_FILE));
                users = GSON.fromJson(json, HashMap.class);
                Soar.LOGGER.info("Loaded {} users from disk", users.size());
            } else {
                Soar.LOGGER.info("No existing user data found, starting fresh");
                users = new HashMap<>();
            }
        } catch (Exception e) {
            Soar.LOGGER.error("Failed to load user data", e);
            users = new HashMap<>();
        }
    }

    private void saveUserData() {
        try {
            Files.createDirectories(USER_DATA_FILE.getParent());
            String json = GSON.toJson(users);
            Files.write(USER_DATA_FILE, json.getBytes());
            Soar.LOGGER.info("Saved {} users to disk", users.size());
        } catch (Exception e) {
            Soar.LOGGER.error("Failed to save user data", e);
        }
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false; // 用户已存在
        }

        UserData userData = new UserData(username, password);
        users.put(username, userData);
        saveUserData();
        return true;
    }

    public boolean login(String username, String password) {
        UserData userData = (UserData) users.get(username);
        if (userData == null) {
            return false;
        }

        // 验证密码
        String inputHash = new UserData(username, password).getPasswordHash();
        if (userData.getPasswordHash().equals(inputHash)) {
            userData.setLastLogin(System.currentTimeMillis());
            currentUser = userData;
            saveUserData();
            return true;
        }

        return false; // 密码错误
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public UserData getCurrentUser() {
        return currentUser;
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}
