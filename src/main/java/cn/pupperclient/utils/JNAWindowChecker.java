package cn.pupperclient.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JNAWindowChecker {
    private static final User32 USER32 = User32.INSTANCE;
    private static ScheduledExecutorService scheduler;
    private static String cachedWindowTitle = "No Song";
    private static long lastUpdateTime = 0;

    // 网易云音乐相关的窗口类名和进程名
    private static final String[] TARGET_CLASS_NAMES = {
        "OrpheusBrowserHost",
        "CloudMusic"
    };

    private static final String[] TARGET_PROCESS_NAMES = {
        "cloudmusic",
        "netease"
    };

    public static void startBackgroundMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cachedWindowTitle = findCloudMusicWindowTitle();
                lastUpdateTime = System.currentTimeMillis();
            } catch (Exception e) {
                // 忽略异常，继续监控
            }
        }, 0, 500, TimeUnit.MILLISECONDS); // 每500ms检查一次
    }

    public static void stopBackgroundMonitoring() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
    }

    public static String getCurrentWindowTitle() {
        return cachedWindowTitle;
    }

    private static String findCloudMusicWindowTitle() {
        final List<String> windowTitles = new ArrayList<>();

        WNDENUMPROC enumProc = new WNDENUMPROC() {
            @Override
            public boolean callback(HWND hwnd, Pointer arg) {
                if (USER32.IsWindowVisible(hwnd)) {
                    // 获取窗口标题
                    char[] windowText = new char[1024];
                    USER32.GetWindowText(hwnd, windowText, 1024);
                    String title = Native.toString(windowText);

                    // 获取窗口类名
                    char[] className = new char[256];
                    USER32.GetClassName(hwnd, className, 256);
                    String classNameStr = Native.toString(className);

                    // 检查是否为网易云音乐窗口
                    if (isCloudMusicWindow(title, classNameStr, hwnd)) {
                        windowTitles.add(title);
                    }
                }
                return true; // 继续枚举
            }
        };

        USER32.EnumWindows(enumProc, null);

        return windowTitles.isEmpty() ? "No Song" : windowTitles.get(0);
    }

    private static boolean isCloudMusicWindow(String title, String className, HWND hwnd) {
        // 检查窗口标题是否包含音乐相关信息
        if (title == null || title.isEmpty() || title.equals("Program Manager")) {
            return false;
        }

        // 网易云音乐窗口标题通常包含 " - 网易云音乐"
        if (title.contains("网易云音乐") && !title.equals("网易云音乐")) {
            return true;
        }

        // 检查窗口类名
        for (String targetClass : TARGET_CLASS_NAMES) {
            if (className.contains(targetClass)) {
                return true;
            }
        }

        // 通过进程名检查
        IntByReference processId = new IntByReference();
        USER32.GetWindowThreadProcessId(hwnd, processId);

        String processName = getProcessName(processId.getValue());
        if (processName != null) {
            for (String targetProcess : TARGET_PROCESS_NAMES) {
                if (processName.toLowerCase().contains(targetProcess)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String getProcessName(int processId) {
        // 这里可以进一步实现获取进程名的逻辑
        // 对于简单使用，可以跳过这个检查
        return null;
    }

    // 简化的窗口检查版本（性能更好）
    public static String getCloudMusicTitleSimple() {
        final String[] result = new String[1];

        WNDENUMPROC enumProc = new WNDENUMPROC() {
            @Override
            public boolean callback(HWND hwnd, Pointer arg) {
                if (USER32.IsWindowVisible(hwnd)) {
                    char[] windowText = new char[512];
                    USER32.GetWindowText(hwnd, windowText, 512);
                    String title = Native.toString(windowText);

                    if (title != null && title.contains("网易云音乐") && !title.equals("网易云音乐")) {
                        result[0] = title;
                        return false; // 找到后停止枚举
                    }
                }
                return true; // 继续枚举
            }
        };

        USER32.EnumWindows(enumProc, null);
        return result[0] != null ? result[0] : "No Song";
    }
}
