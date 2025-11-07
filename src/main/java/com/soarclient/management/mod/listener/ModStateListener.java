package com.soarclient.management.mod.listener;

import com.soarclient.management.mod.event.ModStateChangeEvent;

@FunctionalInterface
public interface ModStateListener {
    void onModStateChanged(ModStateChangeEvent event);
}
