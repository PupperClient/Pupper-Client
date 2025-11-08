package cn.pupperclient.utils;

import cn.pupperclient.Soar;

public interface ToolInstallCallback {
    void onProgress(Soar.MusicToolStatus status, float progress);
    void onComplete(boolean success);
}
