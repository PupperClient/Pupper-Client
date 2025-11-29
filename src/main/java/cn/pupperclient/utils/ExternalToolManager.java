package cn.pupperclient.utils;

import cn.pupperclient.PupperClient;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ExternalToolManager {
    private static final String TOOLS_DIR = "pupper/tools";

    // 原始GitHub链接
    private static final String YT_DLP_URL_WINDOWS_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
    private static final String YT_DLP_URL_LINUX_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";
    private static final String YT_DLP_URL_MAC_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos";

    // 镜像链接（用于CN地区）
    private static final String YT_DLP_URL_WINDOWS_MIRROR = "https://gh.llkk.cc/https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
    private static final String YT_DLP_URL_LINUX_MIRROR = "https://gh.llkk.cc/https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";
    private static final String YT_DLP_URL_MAC_MIRROR = "https://gh.llkk.cc/https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos";

    private static final String FFMPEG_URL_RAW = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip";
    private static final String FFMPEG_URL_MIRROR = "https://gh.llkk.cc/https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip";

    private File toolsDir;
    private File ytDlpPath;
    private File ffmpegPath;

    private boolean ytDlpAvailable = false;
    private boolean ffmpegAvailable = false;
    private boolean isCNRegion = false;

    // 多线程下载
    private static final int DOWNLOAD_THREADS = 4;
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    private static final ExecutorService downloadExecutor = Executors.newFixedThreadPool(DOWNLOAD_THREADS);

    // 下载状态
    private static float ytDlpProgress = 0f;
    private static float ffmpegProgress = 0f;
    private static String currentDownload = "";

    public ExternalToolManager() {
        init();
    }

    private void init() {
        toolsDir = new File(TOOLS_DIR);
        if (!toolsDir.exists()) {
            toolsDir.mkdirs();
        }

        // 检测地区
        detectRegion();

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            ytDlpPath = new File(toolsDir, "yt-dlp.exe");
            ffmpegPath = new File(toolsDir, "ffmpeg.exe");
        } else {
            ytDlpPath = new File(toolsDir, "yt-dlp");
            ffmpegPath = new File(toolsDir, "ffmpeg");
        }
    }

    /**
     * 检测用户所在地区，判断是否为CN地区
     */
    private void detectRegion() {
        try {
            // 方法1: 通过系统属性判断
            String country = System.getProperty("user.country");
            String language = System.getProperty("user.language");

            if ("CN".equalsIgnoreCase(country) || "zh".equalsIgnoreCase(language)) {
                isCNRegion = true;
                PupperClient.LOGGER.info("Detected CN region, using mirror links");
                return;
            }

            // 方法2: 通过时区判断
            String timezone = System.getProperty("user.timezone");
            if (timezone != null && timezone.contains("Asia/Shanghai")) {
                isCNRegion = true;
                PupperClient.LOGGER.info("Detected CN region by timezone, using mirror links");
                return;
            }

            PupperClient.LOGGER.info("Detected non-CN region, using direct GitHub links");

        } catch (Exception e) {
            // 如果检测失败，默认使用镜像链接以确保可用性
            isCNRegion = true;
            PupperClient.LOGGER.warn("Region detection failed, defaulting to mirror links");
        }
    }

    /**
     * 根据地区选择合适的yt-dlp下载链接
     */
    private URL getYtDlpDownloadUrl() throws MalformedURLException {
        String os = System.getProperty("os.name").toLowerCase();
        String url;

        if (isCNRegion) {
            // CN地区使用镜像
            if (os.contains("win")) {
                url = YT_DLP_URL_WINDOWS_MIRROR;
            } else if (os.contains("mac")) {
                url = YT_DLP_URL_MAC_MIRROR;
            } else {
                url = YT_DLP_URL_LINUX_MIRROR;
            }
        } else {
            // 非CN地区使用原始GitHub链接
            if (os.contains("win")) {
                url = YT_DLP_URL_WINDOWS_RAW;
            } else if (os.contains("mac")) {
                url = YT_DLP_URL_MAC_RAW;
            } else {
                url = YT_DLP_URL_LINUX_RAW;
            }
        }

        PupperClient.LOGGER.info("Using yt-dlp download URL: {}", url);
        return new URL(url);
    }

    /**
     * 根据地区选择合适的ffmpeg下载链接
     */
    private String getFfmpegDownloadUrl() {
        String url = isCNRegion ? FFMPEG_URL_MIRROR : FFMPEG_URL_RAW;
        PupperClient.LOGGER.info("Using ffmpeg download URL: {}", url);
        return url;
    }

    public void checkAndInstallTools(ToolInstallCallback callback) {
        CompletableFuture.supplyAsync(() -> {
            try {
                // 步骤1: 检查工具
                callback.onProgress(PupperClient.MusicToolStatus.CHECKING, 0.1f, "检查工具...");

                boolean ytDlpAvailable = checkYtDlp();
                boolean ffmpegAvailable = checkFfmpeg();

                if (ytDlpAvailable && ffmpegAvailable) {
                    callback.onProgress(PupperClient.MusicToolStatus.INSTALLED, 1.0f, "工具已安装");
                    callback.onComplete(true);
                    return true;
                }

                // 步骤2: 并行下载缺失的工具
                callback.onProgress(PupperClient.MusicToolStatus.DOWNLOADING, 0.3f, "开始下载工具...");

                List<CompletableFuture<Boolean>> downloads = new ArrayList<>();

                if (!ytDlpAvailable) {
                    downloads.add(downloadYtDlp(progress -> {
                        ytDlpProgress = progress;
                        currentDownload = "YT-DLP";
                        float overallProgress = 0.3f + (progress * 0.35f);
                        callback.onProgress(PupperClient.MusicToolStatus.DOWNLOADING, overallProgress,
                            String.format("下载 YT-DLP: %.0f%%", progress * 100));
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
                            String.format("下载 FFmpeg: %.0f%%", progress * 100));
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
                    callback.onProgress(PupperClient.MusicToolStatus.INSTALLED, 1.0f, "工具安装完成");
                    callback.onComplete(true);
                } else {
                    callback.onProgress(PupperClient.MusicToolStatus.FAILED, 1.0f, "工具安装失败");
                    callback.onComplete(false);
                }

                return success;

            } catch (Exception e) {
                PupperClient.LOGGER.error("Tool installation failed: {}", e.getMessage());
                callback.onProgress(PupperClient.MusicToolStatus.FAILED, 1.0f, "工具安装失败: " + e.getMessage());
                callback.onComplete(false);
                return false;
            }
        });
    }

    /**
     * 优化的多线程下载方法
     */
    public CompletableFuture<Boolean> downloadFfmpeg(Consumer<Float> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 只在CN地区禁用SSL证书检查（因为镜像站可能需要）
                if (isCNRegion) {
                    disableSSLCertificateChecking();
                }

                // 使用根据地区选择的下载链接
                String ffmpegUrl = getFfmpegDownloadUrl();
                File zipFile = new File(toolsDir, "ffmpeg.zip");

                // 使用优化的下载方法
                boolean downloadSuccess = downloadFileWithProgress(new URL(ffmpegUrl), zipFile, progressCallback);

                if (!downloadSuccess) {
                    return false;
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
                PupperClient.LOGGER.error("Failed to download ffmpeg: {}", e.getMessage());
                return false;
            }
        }, downloadExecutor);
    }

    /**
     * 优化的文件下载方法
     */
    private boolean downloadFileWithProgress(URL url, File outputFile, Consumer<Float> progressCallback) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // 获取文件大小
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

                    // 限制进度更新频率（每100ms更新一次）
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
            java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile);
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 寻找 ffmpeg.exe（通常在 bin 目录下）
                if (entryName.endsWith("ffmpeg.exe") && !entry.isDirectory()) {
                    try (InputStream inputStream = zip.getInputStream(entry);
                         FileOutputStream outputStream = new FileOutputStream(new File(toolsDir, "ffmpeg.exe"))) {

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

            // 验证文件
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
                // 只在CN地区禁用SSL证书检查
                if (isCNRegion) {
                    disableSSLCertificateChecking();
                }

                URL url = getYtDlpDownloadUrl();
                File outputFile = new File(toolsDir, "yt-dlp.exe");

                // 使用优化的下载方法
                return downloadFileWithProgress(url, outputFile, progressCallback);

            } catch (Exception e) {
                PupperClient.LOGGER.error("Failed to download yt-dlp: {}", e.getMessage());
                return false;
            }
        }, downloadExecutor);
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
            PupperClient.LOGGER.error("Error checking ffmpeg: {}", e.getMessage());
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

    public boolean areToolsAvailable() {
        return ytDlpAvailable && ffmpegAvailable;
    }

    public File getYtDlpPath() {
        return ytDlpPath;
    }

    public File getFfmpegPath() {
        return ffmpegPath;
    }

    /**
     * 获取当前地区检测结果
     */
    public boolean isCNRegion() {
        return isCNRegion;
    }

    /**
     * 获取下载进度信息
     */
    public static float getYtDlpProgress() {
        return ytDlpProgress;
    }

    public static float getFfmpegProgress() {
        return ffmpegProgress;
    }

    public static String getCurrentDownload() {
        return currentDownload;
    }

    /**
     * 重置下载进度
     */
    public static void resetProgress() {
        ytDlpProgress = 0f;
        ffmpegProgress = 0f;
        currentDownload = "";
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
