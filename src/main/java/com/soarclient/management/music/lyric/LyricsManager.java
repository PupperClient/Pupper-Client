package com.soarclient.management.music.lyric;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;
import com.soarclient.management.music.Music;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LyricsManager {
    private static final Logger LOGGER = Logger.getLogger(LyricsManager.class.getName());
    private final Map<String, List<LyricLine>> lyricsCache = new ConcurrentHashMap<>();

    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.audio").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.audio.AudioFileIO").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.audio.AudioFile").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.audio.mp3").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.audio.mp3.MP3File").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.tag").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.tag.id3").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.tag.id3.AbstractID3v2Tag").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.tag.id3.ID3v23Tag").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.tag.id3.ID3v24Tag").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.tag.id3.framebody").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.tag.id3.framebody.AbstractID3v2FrameBody").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.tag.datatype").setLevel(Level.OFF);

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.WARNING);
    }

    public List<LyricLine> getLyrics(Music music) {
        if (music == null || music.getAudio() == null) {
            return Collections.emptyList();
        }

        String cacheKey = music.getAudio().getAbsolutePath();
        return lyricsCache.computeIfAbsent(cacheKey, k -> loadLyrics(music));
    }

    public String getCurrentLyric(Music music, float currentTime) {
        List<LyricLine> lyrics = getLyrics(music);
        if (lyrics.isEmpty()) {
            return null;
        }

        LyricLine currentLine = null;
        for (LyricLine line : lyrics) {
            if (line.getTime() <= currentTime) {
                currentLine = line;
            } else {
                break;
            }
        }

        return currentLine != null ? currentLine.getText() : null;
    }

    private List<LyricLine> loadLyrics(Music music) {
        File audioFile = music.getAudio();
        String fileName = audioFile.getName().toLowerCase();

        try {
            if (fileName.endsWith(".flac") || fileName.endsWith(".mp3")) {
                return loadAudioLyrics(audioFile);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load lyrics for: " + audioFile.getName(), e);
        }

        return Collections.emptyList();
    }

    private List<LyricLine> loadAudioLyrics(File file) throws Exception {
        AudioFile audioFile = AudioFileIO.read(file);
        Tag tag = audioFile.getTag();

        if (tag != null) {
            String lyrics = tag.getFirst(FieldKey.LYRICS);
            if (lyrics != null && !lyrics.isEmpty()) {
                return parseLrcLyrics(lyrics);
            }
        }
        return Collections.emptyList();
    }

    private List<LyricLine> parseLrcLyrics(String lrcContent) {
        List<LyricLine> lines = new ArrayList<>();
        String[] lrcLines = lrcContent.split("\\n");

        for (String line : lrcLines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("[") && !isTimeTag(line)) {
                continue;
            }

            LyricLine lyricLine = parseTimeLine(line);
            if (lyricLine != null) {
                lines.add(lyricLine);
            }
        }

        lines.sort(Comparator.comparing(LyricLine::getTime));
        return lines;
    }

    private boolean isTimeTag(String line) {
        int colonIndex = line.indexOf(':');
        int dotIndex = line.indexOf('.');
        int closeIndex = line.indexOf(']');

        return colonIndex > 1 && dotIndex > colonIndex && closeIndex > dotIndex;
    }

    private LyricLine parseTimeLine(String line) {
        if (!line.startsWith("[")) {
            return null;
        }

        int closeIndex = line.indexOf(']');
        if (closeIndex == -1) {
            return null;
        }

        String timeStr = line.substring(1, closeIndex);
        String text = line.substring(closeIndex + 1).trim();

        if (text.contains("[")) {
            int nextBracket = text.indexOf('[');
            text = text.substring(0, nextBracket).trim();
        }

        if (text.isEmpty()) {
            return null;
        }

        try {
            float time = parseTime(timeStr);
            return new LyricLine(time, text);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to parse time: " + timeStr, e);
            return null;
        }
    }

    private float parseTime(String timeStr) {
        int colonIndex = timeStr.indexOf(':');
        if (colonIndex == -1) {
            throw new IllegalArgumentException("Invalid time format: " + timeStr);
        }

        String minuteStr = timeStr.substring(0, colonIndex);
        String secondStr = timeStr.substring(colonIndex + 1);

        int minutes = Integer.parseInt(minuteStr);
        float seconds = Float.parseFloat(secondStr);

        return minutes * 60 + seconds;
    }

    public void clearCache() {
        lyricsCache.clear();
    }
}
