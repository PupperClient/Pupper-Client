package cn.pupperclient.utils.system;

import cn.pupperclient.PupperClient;

public interface ToolInstallCallback {
    void onProgress(PupperClient.MusicToolStatus status, float progress, String message);
    void onComplete(boolean success);
}
