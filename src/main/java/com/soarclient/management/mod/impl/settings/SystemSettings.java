package com.soarclient.management.mod.impl.settings;

import java.io.File;
import java.util.Arrays;

import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.management.mod.settings.impl.ComboSetting;
import com.soarclient.management.mod.settings.impl.FileSetting;
import com.soarclient.management.mod.settings.impl.StringSetting;
import com.soarclient.skia.font.Icon;
import com.soarclient.utils.OS;

public class SystemSettings extends Mod {

	private static SystemSettings instance;
	private ComboSetting blurTypeSetting = new ComboSetting("setting.blurtype", "setting.blurtype.description",
			Icon.BLUR_MEDIUM, this, Arrays.asList("setting.fastblur", "setting.normalblur"), "setting.normalblur");

	private FileSetting ytdlpPathSetting;
	private FileSetting ffmpegPathSetting;
	private StringSetting ytdlpCommandSetting;

	public SystemSettings() {
		super("mod.systemsettings.name", "mod.systemsettings.description", Icon.SETTINGS, ModCategory.MISC);
		instance = this;
		this.setHidden(true);
		this.setEnabled(true);

		if (OS.isWindows()) {
			ytdlpPathSetting = new FileSetting("setting.ytdlppath", "setting.ytdlppath.description",
					Icon.CONVERSION_PATH, this, null, "exe");
			ffmpegPathSetting = new FileSetting("setting.ffmpegpath", "setting.ffmpegpath.description",
					Icon.CONVERSION_PATH, this, null, "exe");
		} else if (OS.isMacOS() || OS.isLinux()) {
			ytdlpCommandSetting = new StringSetting("setting.ytdlpcommand", "setting.ytdlpcommand.description",
					Icon.TERMINAL, this, "yt-dlp");
		}
	}

	@Override
	public void onDisable() {
		this.setEnabled(true);
	}

	public static SystemSettings getInstance() {
		return instance;
	}

	public boolean isFastBlur() {
		return blurTypeSetting.getOption().contains("fastblur");
	}

	public File getYtdlpPath() {
		return ytdlpPathSetting != null && ytdlpPathSetting.getFile() != null ? ytdlpPathSetting.getFile() : null;
	}

	public File getFFmpegPath() {
		return ffmpegPathSetting != null && ffmpegPathSetting.getFile() != null ? ffmpegPathSetting.getFile() : null;
	}

	public String getYtdlpCommand() {
		return ytdlpCommandSetting != null ? ytdlpCommandSetting.getValue() : "";
	}
}
