package com.soarclient.ui.component.handler.impl;

import com.soarclient.ui.component.handler.ComponentHandler;

public abstract class SwitchHandler extends ComponentHandler {
	public abstract void onEnabled();

	public abstract void onDisabled();
}
