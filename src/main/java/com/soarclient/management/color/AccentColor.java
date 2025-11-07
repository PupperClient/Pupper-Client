package com.soarclient.management.color;

import com.soarclient.animation.SimpleAnimation;
import com.soarclient.utils.ColorUtils;

import java.awt.Color;

public class AccentColor {
    private static AccentColor instance;

    private final SimpleAnimation animation = new SimpleAnimation();

    private final String name;
    private final Color color1;
    private final Color color2;

    public AccentColor(String name, Color color1, Color color2) {
        this.name = name;
        this.color1 = color1;
        this.color2 = color2;
    }

    public String getName() {
        return name;
    }

    public Color getColor1() {
        return color1;
    }

    public Color getColor2() {
        return color2;
    }

    public Color getInterpolateColor() {
        return ColorUtils.interpolateColors(15, 0, color1, color2);
    }

    public Color getInterpolateColor(int index) {
        return ColorUtils.interpolateColors(15, index, color1, color2);
    }

    public SimpleAnimation getAnimation() {
        return animation;
    }

    public static AccentColor getInstance() {
        return instance;
    }
}
