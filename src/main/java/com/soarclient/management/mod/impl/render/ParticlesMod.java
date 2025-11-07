package com.soarclient.management.mod.impl.render;

import com.soarclient.event.EventBus;
import com.soarclient.event.server.impl.AttackEntityEvent;
import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.management.mod.settings.impl.BooleanSetting;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.skia.font.Icon;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;

public class ParticlesMod extends Mod {

	private BooleanSetting alwaysSharpnessSetting = new BooleanSetting("setting.alwayssharpness",
			"setting.alwayssharpness.description", Icon.CONTRAST, this, false);
	private BooleanSetting alwaysCriticalsSetting = new BooleanSetting("setting.alwayscriticals",
			"setting.alwayscriticals.description", Icon.CONTRAST, this, false);
	private BooleanSetting sharpnessSetting = new BooleanSetting("setting.sharpness", "setting.sharpness.description",
			Icon.FLARE, this, true);
	private BooleanSetting criticalsSetting = new BooleanSetting("setting.criticals", "setting.criticals.description",
			Icon.FLARE, this, true);
	private NumberSetting sharpnessAmountSetting = new NumberSetting("setting.sharpnessamount",
			"setting.sharpnessamount.description", Icon.FILTER_5, this, 2, 1, 10, 1);
	private NumberSetting criticalsAmountSetting = new NumberSetting("setting.criticalsamount",
			"setting.criticalsamount.description", Icon.FILTER_5, this, 2, 1, 10, 1);

	public ParticlesMod() {
		super("mod.particles.name", "mod.particles.description", Icon.FLARE, ModCategory.RENDER);
	}

	public final EventBus.EventListener<AttackEntityEvent> onAttackEntity = event -> {

		int sharpnessAmount = (int) sharpnessAmountSetting.getValue();
		int criticalsAmount = (int) criticalsAmountSetting.getValue();
		Entity target = mc.world.getEntityById(event.getEntityId());
		ClientPlayerEntity player = mc.player;

		boolean critical = criticalsSetting.isEnabled() && player.getAttackCooldownProgress(0.5F) > 0.9F
				&& !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater()
				&& !player.hasStatusEffect(StatusEffects.BLINDNESS) && !player.hasVehicle()
				&& target instanceof LivingEntity && !player.isSprinting();
		boolean alwaysSharpness = alwaysSharpnessSetting.isEnabled();
		boolean sharpness = sharpnessSetting.isEnabled() && EnchantmentHelper.getLevel(mc.world.getRegistryManager()
				.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), player.getWeaponStack()) > 0;
		boolean alwaysCriticals = alwaysCriticalsSetting.isEnabled();

		if (critical || alwaysCriticals) {
			for (int i = 0; i < criticalsAmount - 1; i++) {
				mc.particleManager.addEmitter(target, ParticleTypes.CRIT);
			}
		}

		if (alwaysSharpness || sharpness) {
			for (int i = 0; i < sharpnessAmount - 1; i++) {
				mc.particleManager.addEmitter(target, ParticleTypes.ENCHANTED_HIT);
			}
		}
	};
}
