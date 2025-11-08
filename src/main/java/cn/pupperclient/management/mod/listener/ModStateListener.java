package cn.pupperclient.management.mod.listener;

import cn.pupperclient.management.mod.event.ModStateChangeEvent;

@FunctionalInterface
public interface ModStateListener {
    void onModStateChanged(ModStateChangeEvent event);
}
