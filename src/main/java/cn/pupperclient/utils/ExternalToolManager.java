package cn.pupperclient.utils;

import cn.pupperclient.Soar;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExternalToolManager {
    private static final String TOOLS_DIR = "pupper/tools";
    private static final String YT_DLP_URL_WINDOWS = "https://gh.llkk.cc/https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
    private static final String YT_DLP_URL_LINUX = "https://gh.llkk.cc/https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";
    private static final String YT_DLP_URL_MAC = "https://gh.llkk.cc/https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos";

    private File toolsDir;
    private File ytDlpPath;
    private File ffmpegPath;

    private boolean ytDlpAvailable = false;
    private boolean ffmpegAvailable = false;

    public ExternalToolManager() {
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

    public void checkAndInstallTools(ToolInstallCallback callback) {
        CompletableFuture.supplyAsync(() -> {
            try {
                // 步骤1: 检查工具
                callback.onProgress(Soar.MusicToolStatus.CHECKING, 0.1f);

                boolean ytDlpAvailable = checkYtDlp();
                boolean ffmpegAvailable = checkFfmpeg();

                if (ytDlpAvailable && ffmpegAvailable) {
                    callback.onProgress(Soar.MusicToolStatus.INSTALLED, 1.0f);
                    callback.onComplete(true);
                    return true;
                }

                // 步骤2: 下载缺失的工具
                callback.onProgress(Soar.MusicToolStatus.DOWNLOADING, 0.3f);

                List<CompletableFuture<Boolean>> downloads = new ArrayList<>();

                if (!ytDlpAvailable) {
                    downloads.add(downloadYtDlp(progress -> {
                        float overallProgress = 0.3f + (progress * 0.35f);
                        callback.onProgress(Soar.MusicToolStatus.DOWNLOADING, overallProgress);
                    }));
                }

                if (!ffmpegAvailable) {
                    downloads.add(downloadFfmpeg(progress -> {
                        float overallProgress = 0.65f + (progress * 0.35f);
                        callback.onProgress(Soar.MusicToolStatus.DOWNLOADING, overallProgress);
                    }));
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
                    callback.onProgress(Soar.MusicToolStatus.INSTALLED, 1.0f);
                    callback.onComplete(true);
                } else {
                    callback.onProgress(Soar.MusicToolStatus.FAILED, 1.0f);
                    callback.onComplete(false);
                }

                return success;

            } catch (Exception e) {
                Soar.LOGGER.error("Tool installation failed: {}", e.getMessage());
                callback.onProgress(Soar.MusicToolStatus.FAILED, 1.0f);
                callback.onComplete(false);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> downloadFfmpeg(Consumer<Float> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                disableSSLCertificateChecking();

                // 使用 GitHub 上的预编译单个 ffmpeg.exe
                String ffmpegUrl = "https://gh.llkk.cc/https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip";
                File zipFile = new File(toolsDir, "ffmpeg.zip");

                // 下载 ZIP 文件
                URL url = new URL(ffmpegUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int fileSize = connection.getContentLength();

                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(zipFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        if (fileSize > 0 && progressCallback != null) {
                            float progress = (float) totalBytesRead / fileSize * 0.8f; // 下载占80%
                            progressCallback.accept(progress);
                        }
                    }
                }

                // 简化的解压逻辑
                if (progressCallback != null) {
                    progressCallback.accept(0.8f);
                }

                boolean extractSuccess = extractFfmpegSimple(zipFile, progressCallback);

                // 清理临时文件
                zipFile.delete();

                return extractSuccess;

            } catch (Exception e) {
                Soar.LOGGER.error("Failed to download ffmpeg: {}", e.getMessage());
                return false;
            }
        });
    }

    private boolean extractFfmpegSimple(File zipFile, Consumer<Float> progressCallback) {
        try {
            java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile);
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 寻找 ffmpeg.exe（通常在 bin 目录下）
                if (entryName.endsWith("ffmpeg.exe") && !entry.isDirectory()) {
                    try (InputStream inputStream = zip.getInputStream(entry);
                         FileOutputStream outputStream = new FileOutputStream(new File(toolsDir, "ffmpeg.exe"))) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        Soar.LOGGER.info("Successfully extracted ffmpeg.exe");
                        break;
                    }
                }
            }

            zip.close();

            if (progressCallback != null) {
                progressCallback.accept(1.0f);
            }

            // 验证文件
            File ffmpegExe = new File(toolsDir, "ffmpeg.exe");
            return ffmpegExe.exists() && ffmpegExe.length() > 0;

        } catch (Exception e) {
            Soar.LOGGER.error("Failed to extract ffmpeg: {}", e.getMessage());
            return false;
        }
    }

    public CompletableFuture<Boolean> downloadYtDlp(Consumer<Float> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                disableSSLCertificateChecking();

                URL url = getYtDlpDownloadUrl();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int fileSize = connection.getContentLength();

                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(new File(toolsDir, "yt-dlp.exe"))) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        if (fileSize > 0 && progressCallback != null) {
                            float progress = (float) totalBytesRead / fileSize;
                            progressCallback.accept(progress);
                        }
                    }
                }

                return true;
            } catch (Exception e) {
                Soar.LOGGER.error("Failed to download yt-dlp: {}", e.getMessage());
                return false;
            }
        });
    }

    private boolean checkYtDlp() {
        // 检查系统PATH中的yt-dlp
        if (checkCommand("yt-dlp --version")) {
            ytDlpAvailable = true;
            ytDlpPath = new File("yt-dlp");
        }

        // 检查工具目录中是否已有工具
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

            // 验证 FFmpeg 是否可执行
            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegExe.getAbsolutePath(), "-version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 等待进程完成
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return false;
            }

            int exitCode = process.exitValue();
            return exitCode == 0;

        } catch (Exception e) {
            Soar.LOGGER.error("Error checking ffmpeg: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkCommand(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command.split(" "));
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private URL getYtDlpDownloadUrl() throws MalformedURLException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new URL(YT_DLP_URL_WINDOWS);
        } else if (os.contains("mac")) {
            return new URL(YT_DLP_URL_MAC);
        } else {
            return new URL(YT_DLP_URL_LINUX);
        }
    }

    public boolean areToolsAvailable() {
        return ytDlpAvailable && ffmpegAvailable;
    }

    public File getYtDlpPath() {
        return ytDlpPath;
    }

    public File getFfmpegPath() {
        return ffmpegPath;
    }

    private static void disableSSLCertificateChecking() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
