package cn.pupperclient.skia.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL11;

import cn.pupperclient.skia.context.SkiaContext;
import cn.pupperclient.skia.utils.SkiaUtils;

import io.github.humbleui.skija.ColorType;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.SurfaceOrigin;

public class ImageHelper {

	private Map<String, Image> images = new HashMap<>();
	private Map<Integer, TextureEntry> textures = new HashMap<>();

	public boolean load(int texture, float width, float height, SurfaceOrigin origin) {

        int w = Math.max(1, (int) width);
        int h = Math.max(1, (int) height);
        TextureEntry existing = textures.get(texture);
        if (existing == null || existing.width != w || existing.height != h || existing.origin != origin) {
            if (existing != null) {
                existing.image.close();
            }
            Image image = Image.adoptGLTextureFrom(SkiaContext.getContext(), texture, GL11.GL_TEXTURE_2D, w, h,
                GL11.GL_RGBA8, origin, ColorType.RGBA_8888);
            textures.put(texture, new TextureEntry(image, w, h, origin));
        }

		return true;
	}

	public boolean load(Identifier identifier) {
		
        String key = identifier.toString();
		if (!images.containsKey(key)) {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			Resource resource;
			try {
				resource = resourceManager.getResourceOrThrow(identifier);
				try (InputStream inputStream = resource.open()) {

					byte[] imageData = inputStream.readAllBytes();
					Image image = Image.makeDeferredFromEncodedBytes(imageData);
                    images.put(key, image);
					return true;
				} catch (IOException e) {
					cn.pupperclient.PupperLogger.error("ImageHelper", "Failed to read identifier bytes", e);
				}
			} catch (FileNotFoundException e) {
				cn.pupperclient.PupperLogger.error("ImageHelper", "Identifier resource not found", e);
			}
		}
		return true;
	}

	public boolean load(String filePath) {
		if (!images.containsKey(filePath)) {
			Optional<byte[]> encodedBytes = SkiaUtils.convertToBytes(filePath);
			if (encodedBytes.isPresent()) {
				images.put(filePath, Image.makeDeferredFromEncodedBytes(encodedBytes.get()));
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	public boolean load(File file) {

		if (!images.containsKey(file.getName())) {

			try {
				byte[] encoded = org.apache.commons.io.IOUtils.toByteArray(new FileInputStream(file));
				images.put(file.getName(), Image.makeDeferredFromEncodedBytes(encoded));
				return true;
			} catch (IOException e) {
				cn.pupperclient.PupperLogger.error("ImageHelper", "Failed to load image from file: " + file.getName(), e);
				return false;
			}
		}

		return true;
	}

	public Image get(String path) {

		if (images.containsKey(path)) {
			return images.get(path);
		}

		return null;
	}

	public Image get(int texture) {

		TextureEntry entry = textures.get(texture);
		if (entry != null) {
			return entry.image;
		}

		return null;
	}

	public void clear() {
		images.values().forEach(Image::close);
		images.clear();
		textures.values().forEach(entry -> entry.image.close());
		textures.clear();
	}

    private record TextureEntry(Image image, int width, int height, SurfaceOrigin origin) {
    }
}
