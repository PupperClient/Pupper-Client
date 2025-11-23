package cn.pupperclient.event.client;

import cn.pupperclient.event.Event;
import net.minecraft.entity.damage.DamageSource;

public class EventUseTotem extends Event {
    public final DamageSource source;

    public EventUseTotem(DamageSource source) {
        this.source = source;
    }
}
