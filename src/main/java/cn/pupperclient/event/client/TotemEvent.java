package cn.pupperclient.event.client;

import cn.pupperclient.event.Event;
import net.minecraft.entity.player.PlayerEntity;

public class TotemEvent extends Event {
    private PlayerEntity player;

    public TotemEvent(PlayerEntity entity) {
        player = entity;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
