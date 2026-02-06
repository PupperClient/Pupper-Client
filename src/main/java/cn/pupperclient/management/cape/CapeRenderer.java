package cn.pupperclient.management.cape;

import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.context.SkiaContext;
import cn.pupperclient.skia.image.ImageHelper;
import io.github.humbleui.skija.Image;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.util.Identifier;
import io.github.humbleui.skija.ClipMode;
import io.github.humbleui.skija.Path;
import io.github.humbleui.skija.SurfaceOrigin;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

import java.util.Objects;

public class CapeRenderer {

    public static void renderCapePreview(Identifier capeTexture, float x, float y, float width, float height) {
        if (capeTexture == null) return;

        var abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(capeTexture);
        if (abstractTexture == null) return;

        int glId = -1;
        var gl = abstractTexture.getGlTexture();
        if (gl instanceof GlTexture glTexture) {
            glId = glTexture.getGlId();
        }

        if (glId != -1) {
            Image img = ImageHelper.get(SkiaContext.INSTANCE.getContext(), glId, 64, 32);

            Skia.save();
            Skia.translate(x + 2, y + 8);
            Skia.scale(2f, 2f, 1f);

            Rect srcRect = Rect.makeXYWH(1, 1, 10, 16);
            Rect dstRect = Rect.makeXYWH(0, 0, 10, 16);
            Skia.getCanvas().drawImageRect(img, srcRect, dstRect, null, false);

            Skia.restore();

            Skia.save();
            Skia.translate(x + 26, y + 8);
            Skia.scale(2f, 2f, 1f);

            Rect srcRect2 = Rect.makeXYWH(12, 1, 10, 16);
            Rect dstRect2 = Rect.makeXYWH(0, 0, 10, 16);
            Skia.getCanvas().drawImageRect(img, srcRect2, dstRect2, null, false);

            Skia.restore();
        }
    }

    public static void renderRoundedCapePreview(Identifier capeTexture, float x, float y, float width, float height, float radius) {
        if (capeTexture == null) return;

        var abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(capeTexture);
        if (abstractTexture == null) return;

        int glId = -1;
        var gl = abstractTexture.getGlTexture();
        if (gl instanceof GlTexture glTexture) {
            glId = glTexture.getGlId();
        }

        if (glId != -1) {
            Image img = ImageHelper.get(SkiaContext.INSTANCE.getContext(), glId, 64, 32);

            try (Path path = Path.makeRRect(RRect.makeXYWH(x, y, width, height, radius))) {
                Rect srcRect = Rect.makeXYWH(1, 1, 10, 16);
                Rect dstRect = Rect.makeXYWH(x, y, width, height);

                Skia.save();
                Skia.getCanvas().clipPath(path, ClipMode.INTERSECT, true);
                Skia.getCanvas().drawImageRect(img, srcRect, dstRect, null, false);
                Skia.restore();
            }
        }
    }
}
