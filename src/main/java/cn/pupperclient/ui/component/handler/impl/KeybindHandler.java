package cn.pupperclient.ui.component.handler.impl;

import cn.pupperclient.ui.component.handler.ComponentHandler;

import net.minecraft.client.util.InputUtil.Key;

public abstract class KeybindHandler extends ComponentHandler {
	public abstract void onBinded(Key key);
}
