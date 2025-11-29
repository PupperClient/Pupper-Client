package cn.pupperclient.management.mod.api.hud;

import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.types.Rect;

import java.util.List;

public abstract class SimpleListHUDMod extends HUDMod{
    public SimpleListHUDMod (String name, String description, String icon) {
        super(name, description, icon);
    }

    protected void draw() {

        float fontSize = 9;
        float padding = 5;
        float maxWidth = 0;
        float height = 0;
        float lineHeight = 5; // 空行的间隔
                              // EN: Line height spacing

        for (int i = 0; i < getText().size(); i++) {
            Rect textBounds = Skia.getTextBounds(getText().get(i), Fonts.getRegular(fontSize));
            maxWidth = Math.max(maxWidth, textBounds.getWidth());
            height += textBounds.getHeight() + lineHeight;
        }

        float width = maxWidth + (padding * 2);

        this.begin();
        this.drawBackground(getX(), getY(), width, height + 5);

        float y = getY() + padding;

        for (int i = 0; i < getText().size(); i++) {
            this.drawText(getText().get(i), getX() + padding + (0),
                y, Fonts.getRegular(fontSize));
            y += Skia.getTextBounds(getText().get(i), Fonts.getRegular(fontSize)).getHeight() + lineHeight;
        }

        this.finish();
        position.setSize(width, height);
    }

    public abstract List<String> getText();

    public abstract String getIcon();

    @Override
    public float getRadius() {
        return 0.6f;
    }
}
