package com.soarclient.event.server.impl;

import com.soarclient.event.Event;

public class DamageEntityEvent extends Event {

	private final int entityId;

	public DamageEntityEvent(int entityId) {
		this.entityId = entityId;
	}

	public int getEntityId() {
		return entityId;
	}
}
