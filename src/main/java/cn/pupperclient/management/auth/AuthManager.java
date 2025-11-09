package cn.pupperclient.management.auth;

import cn.pupperclient.PupperClient;
import cn.pupperclient.utils.file.FileLocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SECRET_KEY = "pupper";
    private static AuthManager instance;

    private Map<String, UserData> users = new HashMap<>();
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
            this.passwordHash = hashPassword(password);
            this.createdAt = System.currentTimeMillis();
            this.lastLogin = System.currentTimeMillis();
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

    private static String hashPassword(String password) {
        try {
            String data = password + SECRET_KEY;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            PupperClient.LOGGER.error("Failed to hash password", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * 加载用户数据
     */
    private void loadUserData() {
        try {
            File userDataFile = FileLocation.USER_DATA_DIR;
            Path userDataPath = userDataFile.toPath();

            PupperClient.LOGGER.info("Attempting to load user data from: {}", userDataPath);

            // 如果文件不存在，创建空的数据文件
            if (!userDataFile.exists()) {
                PupperClient.LOGGER.info("No existing user data found, creating new file");
                users = new HashMap<>();

                // 确保目录存在
                File parentDir = userDataFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean dirsCreated = parentDir.mkdirs();
                    if (!dirsCreated) {
                        PupperClient.LOGGER.warn("Failed to create parent directories: {}", parentDir.getAbsolutePath());
                    }
                }

                // 创建空文件
                boolean fileCreated = userDataFile.createNewFile();
                if (!fileCreated) {
                    PupperClient.LOGGER.warn("Failed to create user data file: {}", userDataPath);
                }

                saveUserData(); // 保存空的用户数据
                return;
            }

            // 检查是否是文件而不是目录
            if (userDataFile.isDirectory()) {
                PupperClient.LOGGER.error("User data path is a directory, not a file: {}", userDataPath);
                users = new HashMap<>();
                return;
            }

            // 读取并解析用户数据
            String json = new String(Files.readAllBytes(userDataPath));
            if (json.trim().isEmpty()) {
                users = new HashMap<>();
                PupperClient.LOGGER.info("User data file is empty, starting fresh");
            } else {
                java.lang.reflect.Type type = new TypeToken<Map<String, UserData>>(){}.getType();
                users = GSON.fromJson(json, type);

                if (users == null) {
                    users = new HashMap<>();
                }

                PupperClient.LOGGER.info("Loaded {} users from disk", users.size());
            }
        } catch (Exception e) {
            PupperClient.LOGGER.error("Failed to load user data from: {}", FileLocation.USER_DATA_DIR.getAbsolutePath(), e);
            users = new HashMap<>();
        }
    }

    private void saveUserData() {
        try {
            File userDataFile = FileLocation.USER_DATA_DIR;
            Path userDataPath = userDataFile.toPath();

            PupperClient.LOGGER.info("Attempting to save user data to: {}", userDataPath);

            File parentDir = userDataFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean dirsCreated = parentDir.mkdirs();
                if (!dirsCreated) {
                    PupperClient.LOGGER.error("Failed to create parent directories: {}", parentDir.getAbsolutePath());
                    return;
                }
            }

            if (userDataFile.isDirectory()) {
                PupperClient.LOGGER.error("Cannot save user data: path is a directory: {}", userDataPath);
                return;
            }

            String json = GSON.toJson(users);
            Files.write(userDataPath, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            PupperClient.LOGGER.info("Successfully saved {} users to disk", users.size());
        } catch (Exception e) {
            PupperClient.LOGGER.error("Failed to save user data to: {}", FileLocation.USER_DATA_DIR.getAbsolutePath(), e);
        }
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            PupperClient.LOGGER.warn("Registration failed: invalid username or password");
            return false;
        }

        username = username.trim();

        if (users.containsKey(username)) {
            PupperClient.LOGGER.warn("Registration failed: user already exists: {}", username);
            return false;
        }

        UserData userData = new UserData(username, password);
        users.put(username, userData);
        saveUserData();

        PupperClient.LOGGER.info("Registered new user: {}", username);
        return true;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        username = username.trim();
        UserData userData = users.get(username);

        if (userData == null) {
            PupperClient.LOGGER.warn("Login failed: user not found: {}", username);
            return false;
        }

        String inputHash = hashPassword(password);
        if (userData.getPasswordHash().equals(inputHash)) {
            userData.setLastLogin(System.currentTimeMillis());
            currentUser = userData;
            saveUserData();

            PupperClient.LOGGER.info("User logged in: {}", username);
            return true;
        }

        PupperClient.LOGGER.warn("Login failed: incorrect password for user: {}", username);
        return false; // 密码错误
    }

    public void logout() {
        if (currentUser != null) {
            PupperClient.LOGGER.info("User logged out: {}", currentUser.getUsername());
        }
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public UserData getCurrentUser() {
        return currentUser;
    }

    public boolean userExists(String username) {
        return users.containsKey(username.trim());
    }

    public boolean deleteUser(String username) {
        if (username == null) {
            return false;
        }

        username = username.trim();
        if (users.remove(username) != null) {
            if (currentUser != null && currentUser.getUsername().equals(username)) {
                logout();
            }
            saveUserData();
            PupperClient.LOGGER.info("Deleted user: {}", username);
            return true;
        }
        return false;
    }

    public int getUserCount() {
        return users.size();
    }

    public String getUserDataPath() {
        return FileLocation.USER_DATA_DIR.getAbsolutePath();
    }
}
