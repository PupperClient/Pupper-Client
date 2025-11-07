package com.soarclient.gui.modmenu.component;

import java.io.File;

import com.soarclient.Soar;
import com.soarclient.animation.SimpleAnimation;
import com.soarclient.libraries.material3.hct.Hct;
import com.soarclient.management.color.api.ColorPalette;
import com.soarclient.management.mod.settings.Setting;
import com.soarclient.management.mod.settings.impl.BooleanSetting;
import com.soarclient.management.mod.settings.impl.ComboSetting;
import com.soarclient.management.mod.settings.impl.FileSetting;
import com.soarclient.management.mod.settings.impl.HctColorSetting;
import com.soarclient.management.mod.settings.impl.KeybindSetting;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.management.mod.settings.impl.StringSetting;
import com.soarclient.skia.Skia;
import com.soarclient.skia.font.Fonts;
import com.soarclient.ui.component.Component;
import com.soarclient.ui.component.handler.impl.ComboButtonHandler;
import com.soarclient.ui.component.handler.impl.FileSelectorHandler;
import com.soarclient.ui.component.handler.impl.HctColorPickerHandler;
import com.soarclient.ui.component.handler.impl.KeybindHandler;
import com.soarclient.ui.component.handler.impl.SliderHandler;
import com.soarclient.ui.component.handler.impl.SwitchHandler;
import com.soarclient.ui.component.handler.impl.TextHandler;
import com.soarclient.ui.component.impl.ComboButton;
import com.soarclient.ui.component.impl.FileSelector;
import com.soarclient.ui.component.impl.HctColorPicker;
import com.soarclient.ui.component.impl.Keybind;
import com.soarclient.ui.component.impl.Slider;
import com.soarclient.ui.component.impl.Switch;
import com.soarclient.ui.component.impl.text.TextField;
import com.soarclient.utils.language.I18n;

import net.minecraft.client.util.InputUtil;

public class SettingBar extends Component {

	private SimpleAnimation yAnimation = new SimpleAnimation();
	private String title, description, icon;
	private Component component;

	public SettingBar(Setting setting, float x, float y, float width) {
		super(x, y);
		this.title = setting.getName();
		this.description = setting.getDescription();
		this.icon = setting.getIcon();
		this.width = width;
		this.height = 68;

		if (setting instanceof BooleanSetting) {

			BooleanSetting bSetting = (BooleanSetting) setting;
			Switch switchComp = new Switch(x, y, bSetting.isEnabled());

			switchComp.setHandler(new SwitchHandler() {

				@Override
				public void onEnabled() {
					bSetting.setEnabled(true);
				}

				@Override
				public void onDisabled() {
					bSetting.setEnabled(false);
				}
			});

			component = switchComp;
		}

		if (setting instanceof NumberSetting) {

			NumberSetting nSetting = (NumberSetting) setting;
			Slider slider = new Slider(0, 0, 200, nSetting.getValue(), nSetting.getMinValue(), nSetting.getMaxValue(),
					nSetting.getStep());

			slider.setHandler(new SliderHandler() {
				@Override
				public void onValueChanged(Object value) {
                    nSetting.setValue((Float) value);
				}
			});

			component = slider;
		}

		if (setting instanceof ComboSetting) {

			ComboSetting cSetting = (ComboSetting) setting;
			ComboButton button = new ComboButton(0, 0, cSetting.getOptions(), cSetting.getOption());

			button.setHandler(new ComboButtonHandler() {

				@Override
				public void onChanged(String option) {
					cSetting.setOption(option);
				}
			});

			component = button;
		}

		if (setting instanceof KeybindSetting) {

			KeybindSetting kSetting = (KeybindSetting) setting;
			Keybind bind = new Keybind(0, 0, kSetting.getKey());

			bind.setHandler(new KeybindHandler() {

				@Override
				public void onBinded(InputUtil.Key key) {
					kSetting.setKey(key);
				}
			});

			component = bind;
		}

		if (setting instanceof HctColorSetting) {

			HctColorSetting hSetting = (HctColorSetting) setting;
			HctColorPicker picker = new HctColorPicker(0, 0, hSetting.getHct());

			picker.setHandler(new HctColorPickerHandler() {

				@Override
				public void onPicking(Hct hct) {
					hSetting.setHct(hct);
				}
			});

			component = picker;
		}

		if (setting instanceof StringSetting) {

			StringSetting sSetting = (StringSetting) setting;
			TextField textField = new TextField(0, 0, 150, sSetting.getValue());

			textField.setHandler(new TextHandler() {

				@Override
				public void onTyped(String value) {
					sSetting.setValue(value);
				}
			});

			component = textField;
		}
		
		if(setting instanceof FileSetting) {
			
			FileSetting fSetting = (FileSetting) setting;
			FileSelector fileSelector = new FileSelector(0, 0, fSetting.getFile(), fSetting.getExtensions());
			
			fileSelector.setHandler(new FileSelectorHandler() {

				@Override
				public void onSelect(File file) {
					fSetting.setFile(file);
				}
			});
			
			component = fileSelector;
		}
	}

	@Override
	public void draw(double mouseX, double mouseY) {

		ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

		float itemY = y;

		yAnimation.onTick(itemY, 14);
		itemY = yAnimation.getValue();

		if (component != null) {
			component.setX(x + width - component.getWidth() - 22);
			component.setY(itemY + (height - component.getHeight()) / 2);
		}

		Skia.drawRoundedRect(x, itemY, width, height, 18, palette.getSurface());
		Skia.drawFullCenteredText(icon, x + 30, itemY + (height / 2), palette.getOnSurface(), Fonts.getIcon(32));
		Skia.drawText(I18n.get(title), x + 52, itemY + 20, palette.getOnSurface(), Fonts.getRegular(17));
		Skia.drawText(I18n.get(description), x + 52, itemY + 37, palette.getOnSurfaceVariant(), Fonts.getRegular(14));

		if (component != null) {
			component.draw(mouseX, mouseY);
		}
	}

	@Override
	public void mousePressed(double mouseX, double mouseY, int button) {

		if (component != null) {
			component.mousePressed(mouseX, mouseY, button);
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {

		if (component != null) {
			component.mouseReleased(mouseX, mouseY, button);
		}
	}

	@Override
	public void charTyped(char chr, int modifiers) {

		if (component != null) {
			component.charTyped(chr, modifiers);
		}
	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {

		if (component != null) {
			component.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}
}
