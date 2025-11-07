package com.soarclient.utils;

import com.soarclient.Soar;

public interface ToolInstallCallback {
    void onProgress(Soar.MusicToolStatus status, float progress);
    void onComplete(boolean success);
}
