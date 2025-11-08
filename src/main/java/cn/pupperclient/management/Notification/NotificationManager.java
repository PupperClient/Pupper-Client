package cn.pupperclient.management.Notification;

import cn.pupperclient.PupperClient;
import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.font.Icon;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private final List<Notification> notifications = new ArrayList<>();
    private final SimpleAnimation globalAnimation = new SimpleAnimation();
    private PupperClient.MusicToolStatus lastDisplayedStatus = null;

    public void showToolCheckNotification(PupperClient.MusicToolStatus status, float progress) {
        // 只有当状态改变时才创建新通知
        if (status == lastDisplayedStatus && !notifications.isEmpty()) {
            // 更新现有通知的进度
            Notification existing = notifications.get(0);
            existing.progress = progress;
            existing.updateTime = System.currentTimeMillis();
            return;
        }

        // 移除旧通知
        notifications.clear();
        lastDisplayedStatus = status;

        String message = getMessageForStatus(status);
        String icon = getIconForStatus(status);

        Notification notification = new Notification(message, icon, status, progress);
        notifications.add(notification);

        globalAnimation.setValue(0);
    }

    private String getMessageForStatus(PupperClient.MusicToolStatus status) {
        return switch (status) {
            case CHECKING -> "正在检查音乐工具...";
            case INSTALLED -> "音乐工具已安装!";
            case DOWNLOADING -> "正在下载音乐工具...";
            case FAILED -> "音乐工具安装失败";
            case DONE -> "音乐工具准备就绪";
            default -> "";
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
        PupperClient.MusicToolStatus currentStatus = PupperClient.getInstance().getMusicToolStatus();

        // 更新全局动画
        globalAnimation.onTick(notifications.isEmpty() ? 0 : 1, 15);

        if (globalAnimation.getValue() <= 0.01f) {
            return;
        }

        // 绘制通知
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();
        float windowWidth = cn.pupperclient.utils.IMinecraft.mc.getWindow().getWidth();

        float notificationWidth = 320;
        float notificationHeight = 70;
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
                i--;
            }
        }
    }


}
