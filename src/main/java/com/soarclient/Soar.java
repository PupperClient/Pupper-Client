package com.soarclient;

import com.soarclient.animation.Delta;
import com.soarclient.event.EventBus;
import com.soarclient.event.server.PacketHandler;
import com.soarclient.gui.AuthScreen;
import com.soarclient.gui.welcomegui.TermsScreen;
import com.soarclient.libraries.browser.JCefBrowser;
import com.soarclient.logger.SoarLogger;
import com.soarclient.management.auth.AuthManager;
import com.soarclient.management.color.ColorManager;
import com.soarclient.management.command.SoarCommand;
import com.soarclient.management.config.ConfigManager;
import com.soarclient.management.hypixel.HypixelManager;
import com.soarclient.management.keybind.KeybindManager;
import com.soarclient.management.mod.ModManager;
import com.soarclient.management.music.MusicManager;
import com.soarclient.management.profile.ProfileManager;
import com.soarclient.management.user.UserManager;
import com.soarclient.management.websocket.WebSocketManager;
import com.soarclient.skia.font.Fonts;
import com.soarclient.utils.ExternalToolManager;
import com.soarclient.utils.IMinecraft;
import com.soarclient.utils.file.FileLocation;
import com.soarclient.utils.language.I18n;
import com.soarclient.utils.language.Language;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.ViaFabricPlusBase;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Soar implements IMinecraft {
    private static final String CONFIG_FILE_NAME = "pupper.ok";
    public static boolean hasAcceptedTerms = false;

    public static final Logger LOGGER = SoarLogger.getLogger();
    private final static Soar instance = new Soar();

    final ViaFabricPlusBase platform = ViaFabricPlus.getImpl();
    private ExternalToolManager toolManager;
    private MusicToolStatus musicToolStatus = MusicToolStatus.CHECKING;

	private final String name = "Pupper";
	private final String version = "8.5.0";

	private long launchTime;

	private ModManager modManager;
	private ColorManager colorManager;
	private MusicManager musicManager;
	private ConfigManager configManager;
	private ProfileManager profileManager;
	private WebSocketManager webSocketManager;
	private UserManager userManager;
	private HypixelManager hypixelManager;
    private KeybindManager keybindManager;

	public void start() throws IOException {
		JCefBrowser.download();
		Fonts.loadAll();
		FileLocation.init();
		I18n.setLanguage(Language.ENGLISH);

		launchTime = System.currentTimeMillis();

		modManager = new ModManager();
		modManager.init();
		colorManager = new ColorManager();
		musicManager = new MusicManager();
		configManager = new ConfigManager();
		profileManager = new ProfileManager();
		webSocketManager = new WebSocketManager();
		userManager = new UserManager();
		hypixelManager = new HypixelManager();
        keybindManager = KeybindManager.getInstance();
        keybindManager.initialize();
        toolManager = new ExternalToolManager();
        SoarCommand.register();

        EventBus.getInstance().register(new SoarHandler());
		EventBus.getInstance().register(new PacketHandler());
		EventBus.getInstance().register(new Delta());

        Path configDir = FileLocation.MAIN_DIR.toPath();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configFile)) {
            boolean firstLaunch = true;
            try {
                Files.createFile(configFile);
                Files.writeString(configFile,
                    "First launch: " + launchTime + "\n" +
                        "Soar Version: " + version + "\n"
                );
            } catch (IOException e) {
                LOGGER.error("Failed to create first launch detection file: {}", e.getMessage());
            }
        }

        if (!AuthManager.getInstance().isLoggedIn()) {
            MinecraftClient.getInstance().execute(() -> {
                MinecraftClient.getInstance().setScreen(new AuthScreen());
            });
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
//            if (firstLaunch && client.world != null && !hasAcceptedTerms) {
//                TermsScreen termsScreen = new TermsScreen();
//                client.setScreen(termsScreen.build());
//            }
            if (client.world != null && !hasAcceptedTerms) { //debug
                TermsScreen termsScreen = new TermsScreen();
                client.setScreen(termsScreen.build());
            }
        });

        if (getClass().getClassLoader().getResource("assets/pupper/logo.png") == null) {
            LOGGER.error("Soar icon not found in resources!");
        } else {
            LOGGER.info("Soar icon found in resources");
        }

    }

    public void onShutdown() {
        keybindManager.cleanup();
    }

	public static Soar getInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public long getLaunchTime() {
		return launchTime;
	}

	public ModManager getModManager() {
		return modManager;
	}

	public ColorManager getColorManager() {
		return colorManager;
	}

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

	public MusicManager getMusicManager() {
		return musicManager;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public ProfileManager getProfileManager() {
		return profileManager;
	}

	public WebSocketManager getWebSocketManager() {
		return webSocketManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public HypixelManager getHypixelManager() {
		return hypixelManager;
	}

    public ProtocolVersion getProtocolVersion() {
        return ViaFabricPlus.getImpl().getTargetVersion();
    }

    public void setProtocolVersion(ProtocolVersion version) {
        platform.setTargetVersion(version);
        this.getProtocolVersion();
    }

    public ExternalToolManager getToolManager() {
        return toolManager;
    }

    public MusicToolStatus getMusicToolStatus() {
        return musicToolStatus;
    }

    public void setMusicToolStatus(MusicToolStatus status) {
        this.musicToolStatus = status;
    }

    public enum MusicToolStatus {
        CHECKING, INSTALLED, DOWNLOADING, FAILED, DONE
    }
}
