package cn.pupperclient.mixin.interfaces;

public interface IMixinCameraEntity {
	float pupper$getCameraPitch();
	float pupper$getCameraYaw();

	void pupper$setCameraPitch(float pitch);
	void pupper$setCameraYaw(float yaw);
}
