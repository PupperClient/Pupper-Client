package com.soarclient.management.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.decoder.JavaLayerException;

import com.soarclient.animation.SimpleAnimation;
import com.soarclient.libraries.flac.FLACDecoder;
import com.soarclient.libraries.flac.frame.Frame;
import com.soarclient.libraries.flac.metadata.StreamInfo;
import com.soarclient.libraries.flac.util.ByteData;

public class MusicPlayer implements Runnable {

    public static final int SPECTRUM_BANDS = 100;
    public static float[] VISUALIZER = new float[SPECTRUM_BANDS];
    public static SimpleAnimation[] ANIMATIONS = new SimpleAnimation[SPECTRUM_BANDS];

    static {
        for (int i = 0; i < SPECTRUM_BANDS; i++) {
            VISUALIZER[i] = 0.0F;
            ANIMATIONS[i] = new SimpleAnimation();
        }
    }

    private static final int FFT_SIZE = 1024;
    private float[] fftBuffer = new float[FFT_SIZE];
    private float[] magnitudes = new float[SPECTRUM_BANDS];

    private Runnable runnable;

    // FLAC
    private FLACDecoder decoder;
    private StreamInfo streamInfo;

    // MP3
    private Bitstream bitstream;
    private Decoder mp3Decoder;
    private Header mp3Header;

    private AudioFormat audioFormat;
    private DataLine.Info info;
    private SourceDataLine sourceDataLine;

    private Music currentMusic;
    private boolean playing;
    private float volume;

    private float lastCurrentTime;
    private long totalFrames;
    private long currentFrame;
    private float mp3Duration = 0;

    public MusicPlayer(Runnable runnable) {
        this.runnable = runnable;
        this.playing = false;
        this.volume = 0.5F;
    }

    @Override
    public void run() {
        if (currentMusic != null && playing) {
            String fileName = currentMusic.getAudio().getName().toLowerCase();

            if (fileName.endsWith(".flac")) {
                playFlacFile();
            } else if (fileName.endsWith(".mp3")) {
                playMp3File();
            }
        }
    }

    private void playFlacFile() {
        try {
            decoder = new FLACDecoder(new FileInputStream(currentMusic.getAudio()));
            streamInfo = decoder.readStreamInfo();
            audioFormat = new AudioFormat(streamInfo.getSampleRate(),
                streamInfo.getBitsPerSample() == 24 ? 16 : streamInfo.getBitsPerSample(),
                streamInfo.getChannels(), (streamInfo.getBitsPerSample() <= 8) ? false : true, false);
            info = new DataLine.Info(SourceDataLine.class, audioFormat);

            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);
            setVolume(volume);
            sourceDataLine.start();

            Frame frame;
            ByteData byteData = new ByteData(FFT_SIZE * 4);

            while ((frame = decoder.readNextFrame()) != null) {

                while (!playing) {
                    Thread.sleep(10);
                }

                ByteData pcm = decoder.decodeFrame(frame, byteData);
                updateSpectrum(pcm.getData());
                sourceDataLine.write(pcm.getData(), 0, pcm.getLen());
            }

            if ((int) getCurrentTime() >= (int) getEndTime()) {
                runnable.run();
            }

            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMp3File() {
        try {
            FileInputStream fis = new FileInputStream(currentMusic.getAudio());
            BufferedInputStream bis = new BufferedInputStream(fis);

            calculateMp3Duration(currentMusic.getAudio());

            bitstream = new Bitstream(bis);
            mp3Decoder = new Decoder();

            mp3Header = bitstream.readFrame();
            if (mp3Header == null) return;

            int sampleRate = mp3Header.frequency();
            int channels = mp3Header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;

            audioFormat = new AudioFormat(sampleRate, 16, channels, true, false);
            info = new DataLine.Info(SourceDataLine.class, audioFormat);

            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);
            setVolume(volume);
            sourceDataLine.start();

            do {
                while (!playing) {
                    Thread.sleep(10);
                }

                SampleBuffer output = (SampleBuffer) mp3Decoder.decodeFrame(mp3Header, bitstream);
                if (output != null) {
                    byte[] buffer = toByteArray(output.getBuffer(), output.getBufferLength());
                    updateSpectrum(buffer);
                    sourceDataLine.write(buffer, 0, buffer.length);
                }

                bitstream.closeFrame();
                mp3Header = bitstream.readFrame();

            } while (mp3Header != null && playing);

            if (mp3Header == null) {
                runnable.run();
            }

            sourceDataLine.drain();
            sourceDataLine.close();
            bitstream.close();
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateMp3Duration(File mp3File) {
        try {
            com.mpatric.mp3agic.Mp3File mp3 = new com.mpatric.mp3agic.Mp3File(mp3File);
            mp3Duration = (float) mp3.getLengthInSeconds();
        } catch (Exception e) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(mp3File);
                AudioFormat format = audioInputStream.getFormat();
                long frames = audioInputStream.getFrameLength();
                mp3Duration = (float) (frames / format.getFrameRate());
                audioInputStream.close();
            } catch (Exception ex) {
                mp3Duration = 0;
                ex.printStackTrace();
            }
        }
    }
    private byte[] toByteArray(short[] samples, int length) {
        byte[] buffer = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            buffer[i * 2] = (byte) (samples[i] & 0xFF);
            buffer[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        return buffer;
    }

    private void updateSpectrum(byte[] audioData) {

        for (int i = 0; i < Math.min(audioData.length / 2, FFT_SIZE); i++) {
            int index = i * 2;
            if (index + 1 < audioData.length) {
                short sample = (short) ((audioData[index + 1] << 8) | (audioData[index] & 0xFF));
                fftBuffer[i] = sample / 32768.0f;
            }
        }

        for (int i = 0; i < SPECTRUM_BANDS; i++) {
            float sum = 0;
            int startIdx = (i * FFT_SIZE) / SPECTRUM_BANDS;
            int endIdx = ((i + 1) * FFT_SIZE) / SPECTRUM_BANDS;

            for (int j = startIdx; j < endIdx; j++) {
                sum += Math.abs(fftBuffer[j]);
            }

            float average = sum / (endIdx - startIdx);
            magnitudes[i] = average * 60;
            VISUALIZER[i] = magnitudes[i] * (-2F);
        }
    }

    public void setCurrentMusic(Music currentMusic) {

        playing = false;

        if (sourceDataLine != null) {
            sourceDataLine.stop();
            sourceDataLine.drain();
            sourceDataLine.close();
        }

        this.currentMusic = currentMusic;
        playing = true;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public float getCurrentTime() {

        if (sourceDataLine == null || audioFormat == null) {
            return 0;
        }

        if (!playing) {
            return lastCurrentTime;
        }

        lastCurrentTime = (float) (sourceDataLine.getMicrosecondPosition() / 1000000.0);
        return lastCurrentTime;
    }

    public float getEndTime() {
        String fileName = currentMusic != null ? currentMusic.getAudio().getName().toLowerCase() : "";

        if (fileName.endsWith(".flac")) {
            if (streamInfo == null) {
                return 0;
            }

            long totalSamples = streamInfo.getTotalSamples();
            int sampleRate = streamInfo.getSampleRate();

            if (totalSamples > 0 && sampleRate > 0) {
                return (float) totalSamples / sampleRate;
            }
        } else if (fileName.endsWith(".mp3")) {
            return mp3Duration;
        }

        return 0;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume >= 0.0f && volume <= 1.0f) {
            this.volume = volume;
            if (sourceDataLine != null) {
                try {
                    FloatControl gainControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
                    float gain = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                    gainControl.setValue(gain);
                } catch (Exception e) {
                }
            }
        }
    }
}