package com.soarclient.ui.component.handler.impl;

import com.soarclient.libraries.material3.hct.Hct;
import com.soarclient.ui.component.handler.ComponentHandler;

public abstract class HctColorPickerHandler extends ComponentHandler {
	public abstract void onPicking(Hct hct);
}