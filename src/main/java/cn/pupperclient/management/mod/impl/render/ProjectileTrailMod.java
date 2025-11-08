package cn.pupperclient.management.mod.impl.render;

import java.util.Arrays;
import java.util.Objects;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.ClientTickEvent;
import cn.pupperclient.management.mod.Mod;
import cn.pupperclient.management.mod.ModCategory;
import cn.pupperclient.management.mod.settings.impl.ComboSetting;
import cn.pupperclient.skia.font.Icon;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;

public class ProjectileTrailMod extends Mod {

	private ComboSetting effectSetting = new ComboSetting("setting.effect", "setting.effect.description",
			Icon.EXPLOSION, this,
			Arrays.asList("setting.blacksmoke", "setting.fire", "setting.greenstar", "setting.hearts", "setting.magic",
					"setting.musicnote", "setting.slime", "setting.spark", "setting.swirl", "setting.whitesmoke"),
			"setting.hearts");

	public ProjectileTrailMod() {
		super("mod.projectiletrail.name", "mod.projectiletrail.description", Icon.TRAIL_LENGTH, ModCategory.RENDER);
	}

	public final EventBus.EventListener<ClientTickEvent> onClientTick = event -> {

		if (mc.player != null && mc.world != null) {

			Box box = new Box(mc.player.getPos().add(-150.0, -150.0, -150.0),
					mc.player.getPos().add(150.0, 150.0, 150.0));

			for (ProjectileEntity projectile : mc.world.getEntitiesByClass(ProjectileEntity.class, box,
					entity -> true)) {

				if (!(projectile.getVelocity().lengthSquared() > 0.01) || projectile.isOnGround()
						|| mc.world.getBlockState(projectile.getBlockPos()).isSolidBlock(mc.world,
								projectile.getBlockPos()) && Objects.requireNonNull(projectile.getOwner()).getId() != mc.player.getId()) {
					continue;
				}

				ParticleType<?> type = getCurrentType();

				if (type != null && type instanceof ParticleEffect) {
					mc.world.addParticle((ParticleEffect) type, projectile.getX(), projectile.getY(),
							projectile.getZ(), 0.0, 0.0, 0.0);
				}
			}
		}
	};

	private ParticleType<?> getCurrentType() {

		String option = effectSetting.getOption();

		if (option.contains("blacksmoke")) {
			return ParticleTypes.SMOKE;
		} else if (option.contains("fire")) {
			return ParticleTypes.FLAME;
		} else if (option.contains("greenstar")) {
			return ParticleTypes.HAPPY_VILLAGER;
		} else if (option.contains("hearts")) {
			return ParticleTypes.HEART;
		} else if (option.contains("magic")) {
			return ParticleTypes.WITCH;
		} else if (option.contains("musicnote")) {
			return ParticleTypes.NOTE;
		} else if (option.contains("slime")) {
			return ParticleTypes.ITEM_SLIME;
		} else if (option.contains("spark")) {
			return ParticleTypes.FIREWORK;
		} else if (option.contains("whitesmoke")) {
			return ParticleTypes.WHITE_SMOKE;
		}

		return null;
	}
}
