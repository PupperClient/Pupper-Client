package cn.pupperclient.management.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cn.pupperclient.Soar;
import cn.pupperclient.management.config.impl.KeyConfig;
import cn.pupperclient.management.config.impl.ModConfig;
import cn.pupperclient.management.keybind.KeybindManager;
import cn.pupperclient.utils.file.FileUtils;

public class ConfigManager {

	private final List<Config> configs = new ArrayList<>();

	public ConfigManager() {
		configs.add(new ModConfig());
		load(ConfigType.MOD);
        configs.add(new KeyConfig());
        load(ConfigType.KEY);
	}

    public void save(ConfigType type) {
        Config config = getConfig(type);
        Gson gson = new Gson();

        if (config == null || config.getFile() == null) {
            return;
        }

        FileUtils.createFile(config.getFile());

        if (config.getJsonObject() == null) {
            config.setJsonObject(new JsonObject());
        }

        try (FileWriter writer = new FileWriter(config.getFile())) {
            config.onSave();
            gson.toJson(config.getJsonObject(), writer);
        } catch (Exception e) {
            Soar.LOGGER.error("Failed to save config: {}", config.getType(), e);
        }
    }

    public void load(ConfigType type) {
        Config config = getConfig(type);
        Gson gson = new Gson();

        if (config.getFile() == null || !config.getFile().exists()) {
            config.setJsonObject(new JsonObject());
            return;
        }

        try (FileReader reader = new FileReader(config.getFile())) {
            JsonObject loadedJson = gson.fromJson(reader, JsonObject.class);
            config.setJsonObject(loadedJson != null ? loadedJson : new JsonObject());
            config.onLoad();

            if (type == ConfigType.KEY) {
                KeybindManager.getInstance().refreshKeybinds();
            }
        } catch (Exception e) {
            Soar.LOGGER.error("Failed to load config: {}", config.getType(), e);
            config.setJsonObject(new JsonObject());
        }
    }

	public Config getConfig(ConfigType type) {
		return configs.stream().filter(config -> config.getType().equals(type)).findFirst().get();
	}
}
