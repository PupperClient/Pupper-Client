package cn.pupperclient.skia.image;

import cn.pupperclient.skia.context.SkiaContext;
import cn.pupperclient.skia.utils.SkiaUtils;
import cn.pupperclient.utils.ImageUtils;
import com.mojang.blaze3d.textures.GpuTexture;
import io.github.humbleui.skija.ColorType;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.SurfaceOrigin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.mojang.blaze3d.textures.TextureFormat.*;

public class ImageHelper {

    private final Map<String, Image> images = new HashMap<>();
    private final Map<Integer, Image> textures = new HashMap<>();

    private TextureManager getTextureManager() {
        return MinecraftClient.getInstance().getTextureManager();
    }

    public boolean load(int texture, float width, float height, SurfaceOrigin origin) {
        if (textures.containsKey(texture)) {
            return true;
        }

        Image image = Image.adoptGLTextureFrom(SkiaContext.getContext(), texture, GL11.GL_TEXTURE_2D, (int) width,
            (int) height, GL11.GL_RGBA8, origin, ColorType.RGBA_8888);
        textures.put(texture, image);

        return true;
    }

    public boolean load(GpuTexture texture, float width, float height, SurfaceOrigin origin) {
        if (textures.containsKey(texture.hashCode())) {
            return true;
        }

        if (texture instanceof GlTexture glTexture) {
            var GlId = glTexture.getGlId();
            var mcFormat = glTexture.getFormat();

            int glInternalFormat = switch (mcFormat) {
                case RGBA8 -> GL30.GL_RGBA8; // GL_RGBA8
                case RED8 -> GL30.GL_R8;  // GL_R8
                case DEPTH32 -> GL30.GL_DEPTH_COMPONENT32; // GL_DEPTH_COMPONENT32
            };

            ColorType skColorType = switch (mcFormat) {
                case RGBA8 -> ColorType.RGBA_8888;
                case RED8 -> ColorType.GRAY_8;
                case DEPTH32 -> ColorType.GRAY_8;
            };

            Image image = Image.adoptGLTextureFrom(SkiaContext.getContext(), GlId, GL11.GL_TEXTURE_2D, (int) width,
               (int) height, glInternalFormat, origin, skColorType);
            textures.put(texture.hashCode(), image);

            return true;
        }

        return false;
    }

    public boolean load(GpuTexture texture, float width, float height) {
        return load(texture, width, height, SurfaceOrigin.TOP_LEFT);
    }

    public boolean load(Identifier identifier) {
        String path = identifier.getPath();

        // 如果已经缓存，直接返回
        if (images.containsKey(path)) {
            return true;
        }

        TextureManager textureManager = getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(identifier);

        if (abstractTexture instanceof ResourceTexture) {
            try {

                ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
                Resource resource = resourceManager.getResourceOrThrow(identifier);

                try (InputStream inputStream = resource.getInputStream()) {
                    byte[] imageData = inputStream.readAllBytes();
                    Image image = Image.makeDeferredFromEncodedBytes(imageData);
                    images.put(path, image);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public boolean load(String filePath) {
        // 如果已经缓存，直接返回
        if (images.containsKey(filePath)) {
            return true;
        }

        Optional<byte[]> encodedBytes = SkiaUtils.convertToBytes(filePath);
        if (encodedBytes.isPresent()) {
            Image image;
            try {
                image = Image.makeDeferredFromEncodedBytes(ImageUtils.convertToPng(encodedBytes.get()));
            } catch (IOException e) {
                return false;
            }
            images.put(filePath, image);
            return true;
        } else {
            return false;
        }
    }

    public boolean load(File file) {
        String fileName = file.getName();

        if (images.containsKey(fileName)) {
            return true;
        }

        try {
            byte[] encoded = org.apache.commons.io.IOUtils.toByteArray(new FileInputStream(file));
            Image image;
            try {
                image = Image.makeDeferredFromEncodedBytes(ImageUtils.convertToPng(encoded));
            } catch (IOException e) {
                return false;
            }
            images.put(fileName, image);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Image get(String path) {
        return images.get(path);
    }

    public Image get(int hashcode) {
        return textures.get(hashcode);
    }
}
