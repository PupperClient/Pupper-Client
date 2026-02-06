/**
 * @Author: oneachina
 * @link: github.com/oneachina
 */
package cn.pupperclient.skia.utils;

import cn.pupperclient.PupperLogger;
import cn.pupperclient.skia.image.ImageHelper;
import io.github.humbleui.skija.DirectContext;
import io.github.humbleui.skija.Image;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TextureUtils {
    private static final Map<String, NativeImageBackedTexture> TEXTURE_CACHE = new HashMap<>();

    public static Image getImageFromFile(DirectContext context, File file) {
        String path = file.getAbsolutePath();
        NativeImageBackedTexture nbt = TEXTURE_CACHE.get(path);

        if (nbt == null) {
            try (InputStream is = new FileInputStream(file)) {
                NativeImage nativeImage = NativeImage.read(is);
                nbt = new NativeImageBackedTexture(() -> "pupper_skin_" + file.getName(), nativeImage);
                TEXTURE_CACHE.put(path, nbt);
            } catch (Exception e) {
                PupperLogger.error("TextureUtils", "Failed to load image from file: " + path, e);
                return null;
            }
        }

        int glId = -1;
        var gl = nbt.getGlTexture();

        if (gl instanceof GlTexture glTexture) {
            glId = glTexture.getGlId();
        }

        if (glId == -1) {
            PupperLogger.warn("TextureUtils", "Could not retrieve valid GlId for: " + path);
            return null;
        }

        assert nbt.getImage() != null;

        int width = nbt.getImage().getWidth();
        int height = nbt.getImage().getHeight();

        return ImageHelper.get(context, glId, width, height);
    }

    @SuppressWarnings("unused")
    public static void clearCache() {
        TEXTURE_CACHE.values().forEach(NativeImageBackedTexture::close);
        TEXTURE_CACHE.clear();
    }
}
