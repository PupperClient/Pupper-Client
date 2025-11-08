package cn.pupperclient;

import cn.pupperclient.animation.Delta;
import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.server.PacketHandler;
import cn.pupperclient.gui.AuthScreen;
import cn.pupperclient.gui.welcomegui.TermsScreen;
import cn.pupperclient.libraries.browser.JCefBrowser;
import cn.pupperclient.management.auth.AuthManager;
import cn.pupperclient.management.color.ColorManager;
import cn.pupperclient.management.command.SoarCommand;
import cn.pupperclient.management.config.ConfigManager;
import cn.pupperclient.management.hypixel.HypixelManager;
import cn.pupperclient.management.keybind.KeybindManager;
import cn.pupperclient.management.mod.ModManager;
import cn.pupperclient.management.music.MusicManager;
import cn.pupperclient.management.profile.ProfileManager;
import cn.pupperclient.management.user.UserManager;
import cn.pupperclient.management.websocket.WebSocketManager;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.utils.ExternalToolManager;
import cn.pupperclient.utils.IMinecraft;
import cn.pupperclient.utils.file.FileLocation;
import cn.pupperclient.utils.language.I18n;
import cn.pupperclient.utils.language.Language;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.ViaFabricPlusBase;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PupperClient implements IMinecraft {
    private static final String CONFIG_FILE_NAME = "pupper.ok";
    public static boolean hasAcceptedTerms = false;

    public static final Logger LOGGER = PupperLogger.getLogger();
    private final static PupperClient instance = new PupperClient();

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

        EventBus.getInstance().register(new PupperEventHandle());
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
                        "PupperClient Version: " + version + "\n"
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
            LOGGER.error("PupperClient icon not found in resources!");
        } else {
            LOGGER.info("PupperClient icon found in resources");
        }

    }

    public void onShutdown() {
        keybindManager.cleanup();
    }

	public static PupperClient getInstance() {
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
