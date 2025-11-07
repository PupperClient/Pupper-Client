package com.soarclient.management.music.lyric;

public class LyricLine {
    private final float time;
    private final String text;

    public LyricLine(float time, String text) {
        this.time = time;
        this.text = text;
    }

    public float getTime() {
        return time;
    }

    public String getText() {
        return text;
    }
}
