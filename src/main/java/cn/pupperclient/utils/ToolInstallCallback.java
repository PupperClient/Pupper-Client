package cn.pupperclient.utils;

import cn.pupperclient.PupperClient;

public interface ToolInstallCallback {
    void onProgress(PupperClient.MusicToolStatus status, float progress, String message);
    void onComplete(boolean success);
}
