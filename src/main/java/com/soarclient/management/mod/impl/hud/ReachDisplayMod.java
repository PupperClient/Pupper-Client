package com.soarclient.management.mod.impl.hud;

import java.text.DecimalFormat;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.event.server.impl.AttackEntityEvent;
import com.soarclient.event.server.impl.DamageEntityEvent;
import com.soarclient.management.mod.api.hud.SimpleHUDMod;
import com.soarclient.skia.font.Icon;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ReachDisplayMod extends SimpleHUDMod {

	private DecimalFormat df = new DecimalFormat("0.##");

	private double distance = 0;
	private long hitTime = -1;
	private int possibleTarget;

	public ReachDisplayMod() {
		super("mod.reachdisplay.name", "mod.reachdisplay.description", Icon.SOCIAL_DISTANCE);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	public final EventBus.EventListener<DamageEntityEvent> onDamageEntity = event -> {

		if (event.getEntityId() == possibleTarget) {

			Entity entity = mc.world.getEntityById(event.getEntityId());

			possibleTarget = -1;
			distance = mc.player.getEyePos()
					.distanceTo(closestPointToBox(mc.player.getEyePos(), entity.getBoundingBox()));
			hitTime = System.currentTimeMillis();
		}
	};

	public final EventBus.EventListener<AttackEntityEvent> onAttackEntity = event -> {
		possibleTarget = event.getEntityId();
	};

	private Vec3d closestPointToBox(Vec3d start, Box box) {
		return new Vec3d(coerceIn(start.x, box.minX, box.maxX), coerceIn(start.y, box.minY, box.maxY),
				coerceIn(start.z, box.minZ, box.maxZ));
	}

	private double coerceIn(double target, double min, double max) {
		if (target > max) {
			return max;
		}
		return Math.max(target, min);
	}

	@Override
	public String getText() {

		if ((System.currentTimeMillis() - hitTime) > 5000) {
			distance = 0;
		}

		if (distance == 0) {
			return "Hasn't attacked";
		} else {
			return df.format(distance) + " blocks";
		}
	}

	@Override
	public String getIcon() {
		return Icon.SOCIAL_DISTANCE;
	}
}