package com.soarclient.management.mod.settings.impl;

import java.util.List;

import com.soarclient.Soar;
import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.settings.Setting;

public class ComboSetting extends Setting {

	private List<String> options;
	private String defaultOption, option;

	public ComboSetting(String name, String description, String icon, Mod parent, List<String> options, String option) {
		super(name, description, icon, parent);
		this.options = options;
		this.option = option;
		this.defaultOption = option;

		Soar.getInstance().getModManager().addSetting(this);
	}

	@Override
	public void reset() {
		this.option = defaultOption;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public List<String> getOptions() {
		return options;
	}

	public String getDefaultOption() {
		return defaultOption;
	}

	public void setDefaultOption(String defaultOption) {
		this.defaultOption = defaultOption;
	}

	public boolean has(String s) {

		for (String option : options) {
			if (option.equals(s)) {
				return true;
			}
		}

		return false;
	}
}
