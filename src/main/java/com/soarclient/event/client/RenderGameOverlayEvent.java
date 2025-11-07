package com.soarclient.event.client;

import com.soarclient.event.Event;

import net.minecraft.client.gui.DrawContext;

public class RenderGameOverlayEvent extends Event {
	
	private final DrawContext context;
	
	public RenderGameOverlayEvent(DrawContext context) {
		this.context = context;
	}

	public DrawContext getContext() {
		return context;
	}
}
