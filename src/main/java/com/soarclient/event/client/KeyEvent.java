package com.soarclient.event.client;

import com.soarclient.event.Event;

public class KeyEvent extends Event {

    private final int key;
    private final boolean state;

	public KeyEvent(int key, boolean state) {
        this.key = key;
        this.state = state;
	}

    public int getKey() {
        return this.key;
    }

    public boolean isState() {
        return this.state;
    }
}
