package com.soarclient.management.profile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.soarclient.Soar;
import com.soarclient.management.config.Config;
import com.soarclient.management.config.ConfigManager;
import com.soarclient.management.config.ConfigType;
import com.soarclient.utils.JsonUtils;
import com.soarclient.utils.file.FileLocation;
import com.soarclient.utils.file.FileUtils;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

public class ProfileManager {

    private List<Profile> profiles = new ArrayList<>();

    public ProfileManager() {
        readProfiles();
    }

    public void save(String name, String author, String serverIp, Object icon, ConfigType... types) {
        File profileDir = new File(FileLocation.PROFILE_DIR, name);
        if (!profileDir.exists()) {
            profileDir.mkdirs();
        }

        ConfigManager configManager = Soar.getInstance().getConfigManager();

        JsonObject infoJson = new JsonObject();
        infoJson.addProperty("name", name);
        infoJson.addProperty("author", author);
        infoJson.addProperty("serverIp", serverIp);

        if (icon instanceof ProfileIcon) {
            infoJson.addProperty("icon", ((ProfileIcon) icon).getId());
        } else if (icon instanceof File) {
            infoJson.addProperty("icon", "custom");
            File iconFile = new File(profileDir, "icon.png");
            try {
                Files.copy(((File) icon).toPath(), iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileWriter writer = new FileWriter(new File(profileDir, "info.json"))) {
            writer.write(infoJson.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 保存各个配置
        for (ConfigType t : types) {
            Config c = configManager.getConfig(t);
            c.onSave();

            try (FileWriter writer = new FileWriter(new File(profileDir, t.getId() + ".json"))) {
                writer.write(c.getJsonObject().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void load(Profile profile) {
        List<ObjectObjectImmutablePair<ConfigType, JsonObject>> configs = profile.getConfigs();

        for (ObjectObjectImmutablePair<ConfigType, JsonObject> p : configs) {
            Config config = Soar.getInstance().getConfigManager().getConfig(p.left());
            config.setJsonObject(p.right());
            config.onLoad();
        }
    }

    public void readProfiles() {
        profiles.clear();

        File[] profileDirs = FileLocation.PROFILE_DIR.listFiles(File::isDirectory);
        if (profileDirs == null) return;

        for (File profileDir : profileDirs) {
            File infoFile = new File(profileDir, "info.json");
            if (!infoFile.exists()) continue;

            try (FileReader reader = new FileReader(infoFile)) {
                JsonObject infoJson = JsonParser.parseReader(reader).getAsJsonObject();

                String name = JsonUtils.getStringProperty(infoJson, "name", "null");
                String author = JsonUtils.getStringProperty(infoJson, "author", "null");
                String serverIp = JsonUtils.getStringProperty(infoJson, "serverIp", "");
                String iconId = JsonUtils.getStringProperty(infoJson, "icon", null);

                Object icon = null;
                if ("custom".equals(iconId)) {
                    File iconFile = new File(profileDir, "icon.png");
                    if (iconFile.exists()) {
                        icon = iconFile;
                    }
                } else if (iconId != null) {
                    icon = ProfileIcon.getIconById(iconId);
                }

                List<ObjectObjectImmutablePair<ConfigType, JsonObject>> configs = new ArrayList<>();

                // 读取所有配置文件
                for (ConfigType type : ConfigType.values()) {
                    File configFile = new File(profileDir, type.getId() + ".json");
                    if (configFile.exists()) {
                        try (FileReader configReader = new FileReader(configFile)) {
                            JsonObject configJson = JsonParser.parseReader(configReader).getAsJsonObject();
                            configs.add(ObjectObjectImmutablePair.of(type, configJson));
                        }
                    }
                }

                profiles.add(new Profile(name, author, configs, icon, serverIp));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Profile> getProfiles() {
        return profiles;
    }
}
