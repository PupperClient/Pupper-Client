package cn.pupperclient.utils;

import java.awt.Dimension;
import java.awt.Toolkit;

public class ScreenInfo {
    private static final Toolkit toolkit = Toolkit.getDefaultToolkit();
    private static final Dimension screenSize = toolkit.getScreenSize();
    private static final int screenWidth = screenSize.width;
    private static final int screenHeight = screenSize.height;
    private static final int screenResolution = toolkit.getScreenResolution();

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static int getScreenResolution() {
        return screenResolution;
    }
}
