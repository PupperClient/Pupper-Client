package cn.pupperclient.skia.font;

import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Typeface;
import java.io.InputStream;

public class FontManager {

    private static Font defaultFont;
    private static Font iconFont;

    public static Font getDefaultFont(float size) {
        if (defaultFont == null) {
            Typeface typeface = Typeface.makeDefault();
            defaultFont = new Font(typeface, size);
        }
        return new Font(defaultFont.getTypeface(), size);
    }

    public static Font getIconFont(float size) {
        if (iconFont == null) {
            try {
                InputStream fontStream = FontManager.class.getResourceAsStream("/assets/pupper/fonts/MaterialIcons.ttf");
                Typeface typeface = Typeface.makeFromData(io.github.humbleui.skija.Data.makeFromBytes(fontStream.readAllBytes()));
                iconFont = new Font(typeface, size);
            } catch (Exception e) {
                return getDefaultFont(size);
            }
        }
        return new Font(iconFont.getTypeface(), size);
    }
}
