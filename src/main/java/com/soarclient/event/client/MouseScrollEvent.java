package com.soarclient.event.client;

import com.soarclient.event.Event;

public class MouseScrollEvent extends Event {

	private final double amount;

	public MouseScrollEvent(double amount) {
		this.amount = amount;
	}

	public double getAmount() {
		return amount;
	}
}
