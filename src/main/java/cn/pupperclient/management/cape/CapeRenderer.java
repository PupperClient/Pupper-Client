package cn.pupperclient.management.cape;

import cn.pupperclient.skia.Skia;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import io.github.humbleui.skija.ClipMode;
import io.github.humbleui.skija.Path;
import io.github.humbleui.skija.SurfaceOrigin;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

public class CapeRenderer {

    public static void renderCapePreview(Identifier capeTexture, float x, float y, float width, float height) {
        if (capeTexture == null) return;

        try {
            int textureId = MinecraftClient.getInstance().getTextureManager().getTexture(capeTexture).getGlId();

            // 尝试不同的纹理尺寸
            boolean loaded = Skia.getImageHelper().load(textureId, 64, 32, SurfaceOrigin.TOP_LEFT) ||
                           Skia.getImageHelper().load(textureId, 128, 64, SurfaceOrigin.TOP_LEFT);

            if (loaded) {
                Skia.save();
                Skia.translate(x + 2, y + 8);
                Skia.scale(2f, 2f, 1f);

                Rect srcRect = Rect.makeXYWH(1, 1, 10, 16);
                Rect dstRect = Rect.makeXYWH(0, 0, 10, 16);
                Skia.getCanvas().drawImageRect(Skia.getImageHelper().get(textureId), srcRect, dstRect, null, false);

                Skia.restore();

                Skia.save();
                Skia.translate(x + 26, y + 8);
                Skia.scale(2f, 2f, 1f);

                Rect srcRect2 = Rect.makeXYWH(12, 1, 10, 16);
                Rect dstRect2 = Rect.makeXYWH(0, 0, 10, 16);
                Skia.getCanvas().drawImageRect(Skia.getImageHelper().get(textureId), srcRect2, dstRect2, null, false);

                Skia.restore();
            }
        } catch (Exception e) {
            System.err.println("Failed to render cape preview: " + e.getMessage());
        }
    }

    public static void renderRoundedCapePreview(Identifier capeTexture, float x, float y,
                                                float width, float height, float radius) {
        if (capeTexture == null) return;

        try {
            int textureId = MinecraftClient.getInstance().getTextureManager().getTexture(capeTexture).getGlId();

            // 尝试不同的纹理尺寸
            boolean loaded = Skia.getImageHelper().load(textureId, 64, 32, SurfaceOrigin.TOP_LEFT) ||
                           Skia.getImageHelper().load(textureId, 128, 64, SurfaceOrigin.TOP_LEFT);

            if (loaded) {
                Path path = Path.makeRRect(RRect.makeXYWH(x, y, width, height, radius));

                // 根据实际纹理尺寸调整源矩形
                Rect srcRect = Rect.makeXYWH(1, 1, 10, 16);
                Rect dstRect = Rect.makeXYWH(x, y, width, height);

                Skia.save();
                Skia.getCanvas().clipPath(path, ClipMode.INTERSECT, true);
                Skia.getCanvas().drawImageRect(Skia.getImageHelper().get(textureId), srcRect, dstRect, null, false);
                Skia.restore();
            }
        } catch (Exception e) {
            System.err.println("Failed to render rounded cape preview: " + e.getMessage());
        }
    }
}
