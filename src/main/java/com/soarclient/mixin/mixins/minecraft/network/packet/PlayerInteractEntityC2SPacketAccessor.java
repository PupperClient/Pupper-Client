package com.soarclient.mixin.mixins.minecraft.network.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

@Mixin(PlayerInteractEntityC2SPacket.class)
public interface PlayerInteractEntityC2SPacketAccessor {

	@Accessor("type")
	PlayerInteractEntityC2SPacket.InteractTypeHandler getInteractTypeHandler();

	@Accessor("entityId")
	int entityId();
}
