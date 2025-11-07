package com.soarclient.management.mod.impl.hud;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.ClientTickEvent;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.event.server.impl.AttackEntityEvent;
import com.soarclient.event.server.impl.DamageEntityEvent;
import com.soarclient.management.mod.api.hud.SimpleHUDMod;
import com.soarclient.skia.font.Icon;

public class ComboCounterMod extends SimpleHUDMod {

	private long hitTime = -1;
	private int combo, possibleTarget;

	public ComboCounterMod() {
		super("mod.combocounter.name", "mod.combocounter.description", Icon.PERSON_ADD);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	public final EventBus.EventListener<AttackEntityEvent> onAttackEntity = event -> {
		possibleTarget = event.getEntityId();
	};

	public final EventBus.EventListener<DamageEntityEvent> onDamageEntity = event -> {

		if (event.getEntityId() == possibleTarget) {
			combo++;
			possibleTarget = -1;
			hitTime = System.currentTimeMillis();
		} else if (event.getEntityId() == mc.player.getId()) {
			combo = 0;
		}
	};

	public final EventBus.EventListener<ClientTickEvent> onClientTick = event -> {
		if ((System.currentTimeMillis() - hitTime) > 2000) {
			combo = 0;
		}
	};

	@Override
	public String getText() {
		if (combo == 0) {
			return "No Combo";
		} else {
			return combo + " Combo";
		}
	}

	@Override
	public String getIcon() {
		return Icon.PERSON_ADD;
	}
}
