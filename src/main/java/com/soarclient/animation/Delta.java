package com.soarclient.animation;

import org.lwjgl.glfw.GLFW;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.GameLoopEvent;

public class Delta {

	private static double deltaTime;
	private double lastFrame;

	public final EventBus.EventListener<GameLoopEvent> onGameLoop = event -> {
		final double currentTime = GLFW.glfwGetTime() * 1000;
		final double deltaTime = currentTime - lastFrame;
		lastFrame = currentTime;
		Delta.deltaTime = deltaTime;
	};

	public static double getDeltaTime() {
		return deltaTime;
	}
}
