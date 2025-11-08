package cn.pupperclient.management.mod.impl.hud;

import java.text.DecimalFormat;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class SpeedometerMod extends SimpleHUDMod {

	private DecimalFormat speedFormat = new DecimalFormat("0.00");

	public SpeedometerMod() {
		super("mod.speedometer.name", "mod.speedometer.description", Icon.SPEED);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};
	
	@Override
	public String getText() {

		String suffix = " m/s";
		
		if (mc.player != null) {
			
			Entity entity = mc.player.getVehicle() == null ? mc.player : mc.player.getVehicle();
			Vec3d vec = entity.getMovement();
			
			if (entity.isOnGround() && vec.y < 0) {
				vec = new Vec3d(vec.x, 0, vec.z);
			}
			
			return speedFormat.format(vec.length() * 20) + suffix;
		}

		return speedFormat.format(0) + suffix;
	}

	@Override
	public String getIcon() {
		return Icon.SPEED;
	}
}
