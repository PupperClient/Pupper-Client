package cn.pupperclient.mixin.mixins.minecraft.network.packet;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundInteractPacket.class)
public interface PlayerInteractEntityC2SPacketAccessor {

	@Accessor("action")
	ServerboundInteractPacket.Action getInteractTypeHandler();

	@Accessor("entityId")
	int entityId();
}
