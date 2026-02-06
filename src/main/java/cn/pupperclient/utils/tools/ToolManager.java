package cn.pupperclient.utils.tools;

import cn.pupperclient.PupperClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ToolManager {
    private static final String TOOLS_DIR = "pupper/tools";

    private static final String YT_DLP_URL_WINDOWS_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
    private static final String YT_DLP_URL_LINUX_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";
    private static final String YT_DLP_URL_MAC_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos";
    private static final String FFMPEG_URL_RAW = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip";

    private File toolsDir;
    private File ytDlpPath;
    private File ffmpegPath;

    private boolean ytDlpAvailable = false;

    private static final int DOWNLOAD_THREADS = 4;
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    private static final ExecutorService downloadExecutor = Executors.newFixedThreadPool(DOWNLOAD_THREADS);

    private static float ytDlpProgress = 0f;
    private static float ffmpegProgress = 0f;
    private static String currentDownload = "";

    public ToolManager() {
        init();
    }

    private void init() {
        toolsDir = new File(TOOLS_DIR);
        if (!toolsDir.exists()) {
            toolsDir.mkdirs();
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            ytDlpPath = new File(toolsDir, "yt-dlp.exe");
            ffmpegPath = new File(toolsDir, "ffmpeg.exe");
        } else {
            ytDlpPath = new File(toolsDir, "yt-dlp");
            ffmpegPath = new File(toolsDir, "ffmpeg");
        }
    }

    private URL getYtDlpDownloadUrl() throws MalformedURLException {
        String os = System.getProperty("os.name").toLowerCase();
        String url;

        if (os.contains("win")) {
            url = YT_DLP_URL_WINDOWS_RAW;
        } else if (os.contains("mac")) {
            url = YT_DLP_URL_MAC_RAW;
        } else {
            url = YT_DLP_URL_LINUX_RAW;
        }

        PupperClient.LOGGER.info("Using yt-dlp download URL: {}", url);
        return URI.create(url).toURL();
    }

    private String getFfmpegDownloadUrl() {
        String url = FFMPEG_URL_RAW;
        PupperClient.LOGGER.info("Using ffmpeg download URL: {}", url);
        return url;
    }

    public void checkAndInstallTools(ToolInstallCallback callback) {
        CompletableFuture.supplyAsync(() -> {
            try {
                callback.onProgress(PupperClient.MusicToolStatus.CHECKING, 0.1f, "Checking Tool...");

                boolean ytDlpAvailable = checkYtDlp();
                boolean ffmpegAvailable = checkFfmpeg();

                if (ytDlpAvailable && ffmpegAvailable) {
                    callback.onProgress(PupperClient.MusicToolStatus.INSTALLED, 1.0f, "Tool installed");
                    callback.onComplete(true);
                    return true;
                }

                callback.onProgress(PupperClient.MusicToolStatus.DOWNLOADING, 0.3f, "Starting install Tool...");

                List<CompletableFuture<Boolean>> downloads = new ArrayList<>();

                if (!ytDlpAvailable) {
                    downloads.add(downloadYtDlp(progress -> {
                        ytDlpProgress = progress;
                        currentDownload = "YT-DLP";
                        float overallProgress = 0.3f + (progress * 0.35f);
                        callback.onProgress(PupperClient.MusicToolStatus.DOWNLOADING, overallProgress,
                            String.format("Downloading YT-DLP: %.0f%%", progress * 100));
                    }));
                } else {
                    ytDlpProgress = 1.0f;
                }

                if (!ffmpegAvailable) {
                    downloads.add(downloadFfmpeg(progress -> {
                        ffmpegProgress = progress;
                        currentDownload = "FFmpeg";
                        float overallProgress = 0.65f + (progress * 0.35f);
                        callback.onProgress(PupperClient.MusicToolStatus.DOWNLOADING, overallProgress,
                            String.format("Downloading FFmpeg: %.0f%%", progress * 100));
                    }));
                } else {
                    ffmpegProgress = 1.0f;
                }

                CompletableFuture<Void> allDownloads = CompletableFuture.allOf(
                    downloads.toArray(new CompletableFuture[0])
                );

                boolean success = allDownloads.thenApply(v ->
                    downloads.stream().allMatch(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                ).get();

                if (success) {
                    callback.onProgress(PupperClient.MusicToolStatus.INSTALLED, 1.0f, "Tool installed!");
                    callback.onComplete(true);
                } else {
                    callback.onProgress(PupperClient.MusicToolStatus.FAILED, 1.0f, "Tool install failed");
                    callback.onComplete(false);
                }

                return success;

            } catch (Exception e) {
                PupperClient.LOGGER.error("Tool installation failed: {}", e.getMessage());
                callback.onProgress(PupperClient.MusicToolStatus.FAILED, 1.0f, "Tool install failed: " + e.getMessage());
                callback.onComplete(false);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> downloadFfmpeg(Consumer<Float> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String ffmpegUrl = getFfmpegDownloadUrl();
                File zipFile = new File(toolsDir, "ffmpeg.zip");

                boolean downloadSuccess = downloadFileWithProgress(URI.create(ffmpegUrl).toURL(), zipFile, progressCallback);

                if (!downloadSuccess) {
                    return false;
                }

                if (progressCallback != null) {
                    progressCallback.accept(0.8f);
                }

                boolean extractSuccess = extractFfmpegSimple(zipFile, progressCallback);

                zipFile.delete();

                return extractSuccess;

            } catch (Exception e) {
                PupperClient.LOGGER.error("Failed to download ffmpeg: {}", e.getMessage());
                return false;
            }
        }, downloadExecutor);
    }

    private boolean downloadFileWithProgress(URL url, File outputFile, Consumer<Float> progressCallback) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int fileSize = connection.getContentLength();

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                AtomicLong totalRead = new AtomicLong(0);
                long lastProgressUpdate = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalRead.addAndGet(bytesRead);

                    long currentTime = System.currentTimeMillis();
                    if (fileSize > 0 && (currentTime - lastProgressUpdate > 100 || totalRead.get() == fileSize)) {
                        float progress = (float) totalRead.get() / fileSize;
                        if (progressCallback != null) {
                            progressCallback.accept(progress);
                        }
                        lastProgressUpdate = currentTime;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            PupperClient.LOGGER.error("File download failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean extractFfmpegSimple(File zipFile, Consumer<Float> progressCallback) {
        try {
            var zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith("ffmpeg.exe") && !entry.isDirectory()) {
                    var inputStream = zip.getInputStream(entry);

                    try (var outputStream = new FileOutputStream(new File(toolsDir, "ffmpeg.exe"))) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        PupperClient.LOGGER.info("Successfully extracted ffmpeg.exe");
                        break;
                    }
                }
            }

            zip.close();

            if (progressCallback != null) {
                progressCallback.accept(1.0f);
            }

            File ffmpegExe = new File(toolsDir, "ffmpeg.exe");
            return ffmpegExe.exists() && ffmpegExe.length() > 0;

        } catch (Exception e) {
            PupperClient.LOGGER.error("Failed to extract ffmpeg: {}", e.getMessage());
            return false;
        }
    }

    public CompletableFuture<Boolean> downloadYtDlp(Consumer<Float> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = getYtDlpDownloadUrl();
                File outputFile = new File(toolsDir, "yt-dlp.exe");

                return downloadFileWithProgress(url, outputFile, progressCallback);
            } catch (Exception e) {
                PupperClient.LOGGER.error("Failed to download yt-dlp: {}", e.getMessage());
                return false;
            }
        }, downloadExecutor);
    }

    private boolean checkYtDlp() {
        if (checkCommand()) {
            ytDlpAvailable = true;
            ytDlpPath = new File("yt-dlp");
        }

        if (!ytDlpAvailable && ytDlpPath.exists() && ytDlpPath.canExecute()) {
            ytDlpAvailable = true;
        }

        return ytDlpAvailable;
    }

    public boolean checkFfmpeg() {
        try {
            File ffmpegExe = new File(toolsDir, "ffmpeg.exe");
            if (!ffmpegExe.exists()) {
                return false;
            }

            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegExe.getAbsolutePath(), "-version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return false;
            }

            int exitCode = process.exitValue();
            return exitCode == 0;

        } catch (Exception e) {
            PupperClient.LOGGER.error("Error checking FFmpeg: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkCommand() {
        try {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp --version".split(" "));
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public File getYtDlpPath() {
        return ytDlpPath;
    }

    public File getFfmpegPath() {
        return ffmpegPath;
    }

    public static float getYtDlpProgress() {
        return ytDlpProgress;
    }

    public static float getFfmpegProgress() {
        return ffmpegProgress;
    }

    public static String getCurrentDownload() {
        return currentDownload;
    }

    public static void resetProgress() {
        ytDlpProgress = 0f;
        ffmpegProgress = 0f;
        currentDownload = "";
    }
}
