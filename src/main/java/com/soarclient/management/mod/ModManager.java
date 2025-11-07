package com.soarclient.management.mod;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.soarclient.management.mod.api.hud.*;
import com.soarclient.management.mod.api.hud.design.HUDDesign;
import com.soarclient.management.mod.api.hud.design.impl.*;
import com.soarclient.management.mod.event.ModStateChangeEvent;
import com.soarclient.management.mod.impl.fun.*;
import com.soarclient.management.mod.impl.hud.*;
import com.soarclient.management.mod.impl.misc.*;
import com.soarclient.management.mod.impl.player.*;
import com.soarclient.management.mod.impl.render.*;
import com.soarclient.management.mod.impl.settings.*;
import com.soarclient.management.mod.listener.ModStateListener;
import com.soarclient.management.mod.settings.Setting;
import com.soarclient.management.mod.settings.impl.KeybindSetting;

public class ModManager {
	private final List<Mod> mods = new CopyOnWriteArrayList<>();
	private final List<Setting> settings = new CopyOnWriteArrayList<>();
	private final List<HUDDesign> designs = new CopyOnWriteArrayList<>();
    private final List<ModStateListener> stateListeners = new CopyOnWriteArrayList<>();
    private final Map<String, Long> recentStateChanges = new HashMap<>();

    private HUDDesign currentDesign;

	public void init() {
		initMods();
		initDesigns();
	}

	private void initMods() {

		// HUD
		mods.add(new BedwarsStatsOverlayMod());
		mods.add(new BossBarMod());
		mods.add(new ClockMod());
		mods.add(new ComboCounterMod());
		mods.add(new CoordsMod());
		mods.add(new DayCounterMod());
		mods.add(new FPSDisplayMod());
		mods.add(new GameModeDisplayMod());
		mods.add(new HealthDisplayMod());
		mods.add(new JumpResetIndicatorMod());
		mods.add(new KeystrokesMod());
		mods.add(new MemoryUsageMod());
		mods.add(new MouseStrokesMod());
		mods.add(new MusicInfoMod());
		mods.add(new NameDisplayMod());
		mods.add(new PingDisplayMod());
		mods.add(new PitchDisplayMod());
		mods.add(new PlayerCounterMod());
		mods.add(new PlayTimeDisplayMod());
		mods.add(new ProtocolVersionMod());
		mods.add(new ReachDisplayMod());
		mods.add(new ServerIPDisplayMod());
		mods.add(new SpeedometerMod());
		mods.add(new StopwatchMod());
		mods.add(new WebBrowserMod());
		mods.add(new WeatherDisplayMod());
		mods.add(new YawDisplayMod());
        mods.add(new WatermarkMod());
        mods.add(new CPSDisplayMod());
        mods.add(new TargetHUDMod());
        mods.add(new ArrayListMod());
        mods.add(new Island());
        mods.add(new FallDamageHelp());
        mods.add(new CloudMusicHudMod());

		// Player
		mods.add(new AutoGGMod());
		mods.add(new ForceMainHandMod());
		mods.add(new FreelookMod());
		mods.add(new HitDelayFixMod());
		mods.add(new NoJumpDelayMod());
		mods.add(new OldAnimationsMod());
		mods.add(new SnapTapMod());
		mods.add(new TaplookMod());
		mods.add(new ZoomMod());
        mods.add(new AutoTextMod());
        mods.add(new Sprint());

		// Render
		mods.add(new BloodParticleMod());
		mods.add(new CustomHandMod());
		mods.add(new FullbrightMod());
		mods.add(new MusicWaveformMod());
		mods.add(new OverlayEditorMod());
		mods.add(new ParticlesMod());
		mods.add(new ProjectileTrailMod());
        mods.add(new ClickEffectMod());
        mods.add(new NoHurtFov());

		// Misc
		mods.add(new DiscordRPCMod());
		mods.add(new HypixelMod());
		mods.add(new TimeChangerMod());
		mods.add(new WeatherChangerMod());
        mods.add(new IRCChatMod());
		
		// Settings
		mods.add(new HUDModSettings());
		mods.add(new ModMenuSettings());
		mods.add(new SystemSettings());

        //Fun
        mods.add(new FakeFpsMod());
        mods.add(new HeypixelMod());

		sortMods();
        ClickEffectMod clickEffectMod = new ClickEffectMod();
        clickEffectMod.setEnabled(true);
	}

	private void initDesigns() {
		designs.add(new ClassicDesign());
		designs.add(new ClearDesign());
		designs.add(new MaterialYouDesign());
		designs.add(new SimpleDesign());
		setCurrentDesign("design.simple");
	}

	public List<Mod> getMods() {
		return mods;
	}

	public List<Setting> getSettings() {
		return settings;
	}

	public List<HUDMod> getHUDMods() {
		return mods.stream().filter(m -> m instanceof HUDMod).map(m -> (HUDMod) m).collect(Collectors.toList());
	}

	public List<KeybindSetting> getKeybindSettings() {
		return settings.stream().filter(s -> s instanceof KeybindSetting).map(s -> (KeybindSetting) s)
				.collect(Collectors.toList());
	}

	public List<Setting> getSettingsByMod(Mod m) {
		return settings.stream().filter(s -> s.getParent().equals(m)).collect(Collectors.toList());
	}

	public void addSetting(Setting setting) {
		settings.add(setting);
	}

	public HUDDesign getCurrentDesign() {
		return currentDesign;
	}

	public void setCurrentDesign(String name) {
		this.currentDesign = getDesignByName(name);
	}

	public HUDDesign getDesignByName(String name) {
		return designs.stream().filter(design -> design.getName().equals(name)).findFirst()
				.orElseGet(() -> getDesignByName("design.simple"));
	}

	private void sortMods() {
		mods.sort(Comparator.comparing(Mod::getName));
	}

    public void setModEnabledByName(boolean enabled, String modName) {
        Mod targetMod = getModByName(modName);
        if (targetMod != null) {
            targetMod.setEnabled(enabled);
        }
    }

    public Mod getModByName(String modName) {
        return mods.stream()
            .filter(mod -> mod.getName().equalsIgnoreCase(modName))
            .findFirst()
            .orElse(null);
    }

    public int getModByKeybind(String modName) {
        Mod targetMod = getModByName(modName);
        return targetMod.getKey();
    }

    public Boolean toggleMod(String modName) {
        Mod targetMod = getModByName(modName);
        if (targetMod != null) {
            targetMod.toggle();
            return targetMod.isEnabled();
        }
        return null;
    }

    public Boolean getModState(String modName) {
        Mod targetMod = getModByName(modName);
        return targetMod != null ? targetMod.isEnabled() : null;
    }

    public void addStateListener(ModStateListener listener) {
        stateListeners.add(listener);
    }

    public void removeStateListener(ModStateListener listener) {
        stateListeners.remove(listener);
    }

    public void onModStateChanged(Mod mod, boolean enabled) {
        ModStateChangeEvent event = new ModStateChangeEvent(mod, enabled);

        for (ModStateListener listener : stateListeners) {
            listener.onModStateChanged(event);
        }

        recentStateChanges.put(mod.getName(), System.currentTimeMillis());
    }

    public ModStateCheckResult checkRecentStateChange(String modName, long withinMillis) {
        Long changeTime = recentStateChanges.get(modName);
        if (changeTime != null) {
            long timeSinceChange = System.currentTimeMillis() - changeTime;
            if (timeSinceChange <= withinMillis) {
                Mod mod = getModByName(modName);
                if (mod != null) {
                    return new ModStateCheckResult(mod, mod.isEnabled(), true);
                }
            }
        }
        return new ModStateCheckResult(null, false, false);
    }

    public List<ModStateCheckResult> getRecentStateChanges(long withinMillis) {
        List<ModStateCheckResult> results = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, Long> entry : recentStateChanges.entrySet()) {
            long timeSinceChange = currentTime - entry.getValue();
            if (timeSinceChange <= withinMillis) {
                Mod mod = getModByName(entry.getKey());
                if (mod != null) {
                    results.add(new ModStateCheckResult(mod, mod.isEnabled(), true));
                }
            }
        }

        return results;
    }

    public void cleanupOldStateChanges(long olderThanMillis) {
        long currentTime = System.currentTimeMillis();
        recentStateChanges.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > olderThanMillis);
    }

    @SuppressWarnings("unchecked")
    public <T extends Mod> T getModule(Class<T> clazz) {
        return (T) mods.stream()
            .filter(mod -> mod.getClass() == clazz)
            .findFirst()
            .orElse(null);
    }

    /**
     * 通过类名获取 Mod 实例，如果不存在则创建新实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Mod> T getOrCreateModule(Class<T> clazz) {
        T existing = (T) mods.stream()
            .filter(mod -> mod.getClass() == clazz)
            .findFirst()
            .orElse(null);

        if (existing != null) {
            return existing;
        }

        try {
            T newInstance = clazz.getDeclaredConstructor().newInstance();
            mods.add(newInstance);
            sortMods(); // 重新排序
            return newInstance;
        } catch (Exception e) {
            System.err.println("Failed to create mod instance: " + clazz.getSimpleName());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检查 Mod 是否已启用
     */
    public <T extends Mod> boolean isModuleEnabled(Class<T> clazz) {
        T mod = getModule(clazz);
        return mod != null && mod.isEnabled();
    }

    /**
     * 启用/禁用指定类的 Mod
     */
    public <T extends Mod> void setModuleEnabled(Class<T> clazz, boolean enabled) {
        T mod = getModule(clazz);
        if (mod != null) {
            mod.setEnabled(enabled);
        }
    }
}
