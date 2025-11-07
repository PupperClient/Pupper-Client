package com.soarclient.event.server.impl;

import com.soarclient.event.Event;

public class SendChatEvent extends Event {

	private String message;

	public SendChatEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
