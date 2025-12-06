package cn.pupperclient.utils;

import java.awt.Color;
import java.util.regex.Pattern;

public class ColorUtils {

	public static Color getColorFromInt(int color) {

		float r = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float b = (float) (color & 255) / 255.0F;
		float a = (float) (color >> 24 & 255) / 255.0F;

		return new Color(r, g, b, a);
	}

	public static Color blend(Color color1, Color color2, double ratio) {
		float r = (float) ratio;
		float ir = 1.0f - r;
		float[] rgb1 = new float[3];
		float[] rgb2 = new float[3];
		color1.getColorComponents(rgb1);
		color2.getColorComponents(rgb2);
        return new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);
	}

	public static String removeColorCode(String text) {
		return Pattern.compile("\\u00a7[0-9a-fklmnor]").matcher(text).replaceAll("");
	}

	public static Color applyAlpha(Color color, int alpha) {

		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();

		return new Color(red, green, blue, MathUtils.clamp(alpha, 0, 255));
	}

	public static Color applyAlpha(Color color, float alpha) {
		return applyAlpha(color, (int) (alpha * 255));
	}

    public static Color interpolateColors(int speed, int index, Color start, Color end) {

        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;

        return ColorUtils.interpolateColorHue(start, end, angle / 360f);
    }

    private static Color interpolateColorHue(Color color1, Color color2, float amount) {

        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(MathUtils.interpolateFloat(color1HSB[0], color2HSB[0], amount), MathUtils.interpolateFloat(color1HSB[1], color2HSB[1], amount), MathUtils.interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), MathUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColor(Color surfaceContainer, Color surfaceContainerHigh, float hoverValue) {
        return ColorUtils.interpolateColors(200, 0, surfaceContainer, surfaceContainerHigh);
    }

    public static Color withAlpha(Color surfaceVariant, float v) {
        return ColorUtils.applyAlpha(surfaceVariant, v);
    }
}
