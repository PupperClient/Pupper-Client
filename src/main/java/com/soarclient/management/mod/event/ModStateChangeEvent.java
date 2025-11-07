package com.soarclient.management.mod.event;

import com.soarclient.event.Event;
import com.soarclient.management.mod.Mod;

public class ModStateChangeEvent extends Event {
    private final Mod mod;
    private final boolean enabled;
    private final long timestamp;

    public ModStateChangeEvent(Mod mod, boolean enabled) {
        this.mod = mod;
        this.enabled = enabled;
        this.timestamp = System.currentTimeMillis();
    }

    public Mod getMod() {
        return mod;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getModName() {
        return mod.getName();
    }
}
