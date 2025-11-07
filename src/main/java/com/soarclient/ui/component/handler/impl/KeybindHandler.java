package com.soarclient.ui.component.handler.impl;

import com.soarclient.ui.component.handler.ComponentHandler;

import net.minecraft.client.util.InputUtil.Key;

public abstract class KeybindHandler extends ComponentHandler {
	public abstract void onBinded(Key key);
}