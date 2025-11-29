package cn.pupperclient.management.Notification;

import cn.pupperclient.PupperClient;
import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.utils.ExternalToolManager;
import cn.pupperclient.utils.language.I18n;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private final List<Notification> notifications = new ArrayList<>();
    private final SimpleAnimation globalAnimation = new SimpleAnimation();
    private PupperClient.MusicToolStatus lastDisplayedStatus = null;
    private boolean multiProgressMode = false;

    public void showToolCheckNotification(PupperClient.MusicToolStatus status, float progress, String message) {
        // 当切换到下载状态时，启用多进度模式
        if (status == PupperClient.MusicToolStatus.DOWNLOADING && !multiProgressMode) {
            multiProgressMode = true;
            // 移除旧通知
            notifications.clear();
            lastDisplayedStatus = status;

            Notification notification = new Notification(message, getIconForStatus(status), status, progress);
            notification.setMultiProgressMode(true);
            notifications.add(notification);

            globalAnimation.setValue(0);
            return;
        }

        // 只有当状态改变时才创建新通知
        if (status == lastDisplayedStatus && !notifications.isEmpty()) {
            // 更新现有通知
            Notification existing = notifications.get(0);
            existing.progress = progress;
            existing.updateTime = System.currentTimeMillis();

            // 如果是多进度模式，更新进度信息
            if (multiProgressMode) {
                existing.updateMultiProgress(
                    ExternalToolManager.getYtDlpProgress(),
                    ExternalToolManager.getFfmpegProgress(),
                    ExternalToolManager.getCurrentDownload()
                );
            }
            return;
        }

        // 移除旧通知
        notifications.clear();
        multiProgressMode = false;
        lastDisplayedStatus = status;

        String finalMessage = message != null ? message : getMessageForStatus(status);
        String icon = getIconForStatus(status);

        Notification notification = new Notification(finalMessage, icon, status, progress);
        notifications.add(notification);

        globalAnimation.setValue(0);
    }

    private String getMessageForStatus(PupperClient.MusicToolStatus status) {
        return switch (status) {
            case CHECKING -> I18n.get("musictool.name.check");
            case INSTALLED -> I18n.get("musictool.name.installed");
            case DOWNLOADING -> I18n.get("musictool.name.downloading");
            case FAILED -> I18n.get("musictool.name.failed");
            case DONE -> I18n.get("musictool.name.done");
        };
    }

    private String getIconForStatus(PupperClient.MusicToolStatus status) {
        return switch (status) {
            case CHECKING -> Icon.AUTORENEW;
            case INSTALLED, DONE -> Icon.CHECK;
            case DOWNLOADING -> Icon.DOWNLOAD;
            case FAILED -> Icon.ERROR;
            default -> Icon.INFO;
        };
    }

    public void draw(double mouseX, double mouseY) {
        // 更新全局动画
        globalAnimation.onTick(notifications.isEmpty() ? 0 : 1, 15);

        if (globalAnimation.getValue() <= 0.01f) {
            return;
        }

        // 绘制通知
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();
        float windowWidth = cn.pupperclient.utils.IMinecraft.mc.getWindow().getWidth();

        float notificationWidth = 350; // 稍微加宽以容纳双进度信息
        float notificationHeight = multiProgressMode ? 90 : 70; // 多进度模式更高
        float margin = 20;
        float x = windowWidth - notificationWidth - margin;
        float y = margin + 80;

        // 应用全局动画（从右侧滑入）
        x += (1 - globalAnimation.getValue()) * (notificationWidth + margin);

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            float notificationY = y + (i * (notificationHeight + 10));
            notification.draw(x, notificationY, notificationWidth, notificationHeight);

            // 检查通知是否应该移除（只有成功或失败状态才自动移除）
            if (notification.shouldRemove()) {
                notifications.remove(i);
                lastDisplayedStatus = null;
                multiProgressMode = false;
                ExternalToolManager.resetProgress(); // 重置进度
                i--;
            }
        }
    }
}
