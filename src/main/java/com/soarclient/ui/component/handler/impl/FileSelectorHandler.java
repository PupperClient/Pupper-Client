package com.soarclient.ui.component.handler.impl;

import java.io.File;

import com.soarclient.ui.component.handler.ComponentHandler;

public abstract class FileSelectorHandler extends ComponentHandler {
	public abstract void onSelect(File file);
}
