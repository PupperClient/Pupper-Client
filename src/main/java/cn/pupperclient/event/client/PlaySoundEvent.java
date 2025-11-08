package cn.pupperclient.event.client;

import cn.pupperclient.event.Event;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;

public class PlaySoundEvent extends Event {
    private final SoundInstance sound;
    private final SoundCategory category;
    private final String name;
    private Entity sourceEntity;

    public PlaySoundEvent(SoundInstance sound) {
        this.sound = sound;
        this.category = sound.getCategory();
        this.name = sound.getId().toString();
    }


    public String getName() {
        return name;
    }

    public void setSourceEntity(Entity soundSource) {
        sourceEntity = soundSource;
    }
}
