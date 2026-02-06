package cn.pupperclient.mixin.mixins.minecraft.client.sound;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.management.mod.impl.player.OldAnimationsMod;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

@Mixin(SoundSystem.class)
public class MixinSoundSystem {

	@Unique
	private final List<Identifier> newPvPSounds = Arrays.asList(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK.id(),
			SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP.id(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT.id(),
			SoundEvents.ENTITY_PLAYER_ATTACK_STRONG.id(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK.id(),
			SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE.id());

	@Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
	public void oldAnimations$disableNewPvPSounds(SoundInstance sound, CallbackInfo ci) {

		if (OldAnimationsMod.getInstance().isEnabled() && OldAnimationsMod.getInstance().isOldPvPSounds()
				&& newPvPSounds.contains(sound.getId())) {
			ci.cancel();
			return;
		}
	}
}
