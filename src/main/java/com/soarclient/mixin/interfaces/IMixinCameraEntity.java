package com.soarclient.mixin.interfaces;

public interface IMixinCameraEntity {
	float soarClient_CN$getCameraPitch();
	float soarClient_CN$getCameraYaw();

	void soarClient_CN$setCameraPitch(float pitch);
	void soarClient_CN$setCameraYaw(float yaw);
}
