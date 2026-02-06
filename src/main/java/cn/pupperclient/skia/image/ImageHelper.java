package cn.pupperclient.skia.image;

import com.mojang.blaze3d.opengl.GlConst;
import io.github.humbleui.skija.ColorType;
import io.github.humbleui.skija.DirectContext;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.SurfaceOrigin;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public final class ImageHelper {

    private static final Map<Integer, Image> textures = new HashMap<>();

    public ImageHelper() {}

    public static Image get(DirectContext context, int textureId, int width, int height) {
        return get(context, textureId, width, height, true, SurfaceOrigin.BOTTOM_LEFT);
    }

    public static Image get(
        DirectContext context,
        int textureId,
        int width,
        int height,
        boolean hasAlpha,
        SurfaceOrigin origin
    ) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        GL11.glBindTexture(GlConst.GL_TEXTURE_2D, textureId);

        Image image = textures.computeIfAbsent(textureId, id ->
            create(context, id, width, height, origin, hasAlpha)
        );

        if (image.getWidth() != width || image.getHeight() != height) {
            image = create(context, textureId, width, height, origin, hasAlpha);
            textures.put(textureId, image);
        }

        return image;
    }

    private static Image create(
        DirectContext context,
        int textureId,
        int width,
        int height,
        SurfaceOrigin origin,
        boolean hasAlpha
    ) {
        return Image.adoptGLTextureFrom(
            context,
            textureId,
            GL11.GL_TEXTURE_2D,
            width,
            height,
            GL11.GL_RGBA8,
            origin,
            hasAlpha ? ColorType.RGBA_8888 : ColorType.RGB_888X
        );
    }
}
