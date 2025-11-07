package com.soarclient.management.mod.impl.render;

import com.soarclient.event.EventBus;
import com.soarclient.event.server.impl.AttackEntityEvent;
import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.management.mod.settings.impl.BooleanSetting;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.skia.font.Icon;

import net.minecraft.block.Blocks;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class BloodParticleMod extends Mod {

	private BooleanSetting soundSetting = new BooleanSetting("setting.sound", "setting.sound.description",
			Icon.EQUALIZER, this, true);
	private NumberSetting multiplierSetting = new NumberSetting("setting.multiplier",
			"setting.bloodparticle.multiplier.description", Icon.CALCULATE, this, 2, 1, 10, 1);

	public BloodParticleMod() {
		super("mod.bloodparticles.name", "mod.bloodparticles.description", Icon.MENSTRUAL_HEALTH, ModCategory.RENDER);
	}

	public final EventBus.EventListener<AttackEntityEvent> onAttackEntity = event -> {

        Entity target = null;
        if (mc.world != null) {
            target = mc.world.getEntityById(event.getEntityId());
        }

        if (target instanceof LivingEntity) {
			for (int i = 0; i < multiplierSetting.getValue(); i++) {
				mc.particleManager.addEmitter(target,
						new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState()));
			}
		}

		if (soundSetting.isEnabled() && target != null) {
			mc.getSoundManager()
					.play(new PositionedSoundInstance(SoundEvents.BLOCK_STONE_BREAK.id(), SoundCategory.BLOCKS, 4.0F,
							1.2F, SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.LINEAR,
							target.getX(), target.getY(), target.getZ(), false));
		}
	};
}
