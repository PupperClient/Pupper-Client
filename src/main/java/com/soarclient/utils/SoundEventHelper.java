package com.soarclient.utils;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;

public class SoundEventHelper {
    public static Entity lastSoundSource;
    public static SoundEvent lastSoundEvent;

    public static Entity getLastSoundSource() {
        return lastSoundSource;
    }

    public static SoundEvent getLastSoundEvent() {
        return lastSoundEvent;
    }

    public static void clearLastSound() {
        lastSoundSource = null;
        lastSoundEvent = null;
    }
}
