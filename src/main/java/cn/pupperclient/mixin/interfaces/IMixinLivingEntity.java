package cn.pupperclient.mixin.interfaces;

import net.minecraft.world.InteractionHand;

public interface IMixinLivingEntity {
	void soarClient_CN$fakeSwingHand(InteractionHand hand);
}
