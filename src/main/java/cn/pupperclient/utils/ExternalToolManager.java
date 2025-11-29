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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExternalToolManager {
    private static final String TOOLS_DIR = "pupper/tools";

    private static final String YT_DLP_URL_WINDOWS_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
    private static final String YT_DLP_URL_LINUX_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";
    private static final String YT_DLP_URL_MAC_RAW = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos";

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

            // 方法3: 通过IP检测（异步执行，不影响主流程）
            CompletableFuture.runAsync(() -> {
                try {
                    // 使用简单的IP检测服务
                    URL ipApiUrl = new URL("http://ip-api.com/json/?fields=countryCode");
                    HttpURLConnection conn = (HttpURLConnection) ipApiUrl.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    try (InputStream is = conn.getInputStream()) {
                        byte[] buffer = new byte[1024];
                        int read = is.read(buffer);
                        if (read > 0) {
                            String response = new String(buffer, 0, read);
                            if (response.contains("\"countryCode\":\"CN\"")) {
                                isCNRegion = true;
                                PupperClient.LOGGER.info("Detected CN region by IP, using mirror links");
                            }
                        }
                    }
                } catch (Exception e) {
                    // IP检测失败，不影响主要功能
                    PupperClient.LOGGER.debug("IP region detection failed: {}", e.getMessage());
                }
            });

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
                callback.onProgress(PupperClient.MusicToolStatus.CHECKING, 0.1f);

                boolean ytDlpAvailable = checkYtDlp();
                boolean ffmpegAvailable = checkFfmpeg();

                if (ytDlpAvailable && ffmpegAvailable) {
                    callback.onProgress(PupperClient.MusicToolStatus.INSTALLED, 1.0f);
                    callback.onComplete(true);
                    return true;
                }

                // 步骤2: 下载缺失的工具
                callback.onProgress(PupperClient.MusicToolStatus.DOWNLOADING, 0.3f);

                List<CompletableFuture<Boolean>> downloads = new ArrayList<>();

                if (!ytDlpAvailable) {
                    downloads.add(downloadYtDlp(progress -> {
                        float overallProgress = 0.3f + (progress * 0.35f);
                        callback.onProgress(PupperClient.MusicToolStatus.DOWNLOADING, overallProgress);
                    }));
                }

                if (!ffmpegAvailable) {
                    downloads.add(downloadFfmpeg(progress -> {
                        float overallProgress = 0.65f + (progress * 0.35f);
                        callback.onProgress(PupperClient.MusicToolStatus.DOWNLOADING, overallProgress);
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
                    callback.onProgress(PupperClient.MusicToolStatus.INSTALLED, 1.0f);
                    callback.onComplete(true);
                } else {
                    callback.onProgress(PupperClient.MusicToolStatus.FAILED, 1.0f);
                    callback.onComplete(false);
                }

                return success;

            } catch (Exception e) {
                PupperClient.LOGGER.error("Tool installation failed: {}", e.getMessage());
                callback.onProgress(PupperClient.MusicToolStatus.FAILED, 1.0f);
                callback.onComplete(false);
                return false;
            }
        });
    }

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
                PupperClient.LOGGER.error("Failed to download ffmpeg: {}", e.getMessage());
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
                PupperClient.LOGGER.error("Failed to download yt-dlp: {}", e.getMessage());
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
