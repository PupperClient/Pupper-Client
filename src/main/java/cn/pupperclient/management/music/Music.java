package cn.pupperclient.management.music;

import java.awt.Color;
import java.io.File;

public class Music {

	private final File audio;
	private final String title, artist;
	private final File album;
	private final Color color;

	public Music(File audio, String title, String artist, File album, Color color) {
		this.audio = audio;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.color = color;
	}

	public File getAudio() {
		return audio;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public File getAlbum() {
		return album;
	}

	public Color getColor() {
		return color;
	}
}
