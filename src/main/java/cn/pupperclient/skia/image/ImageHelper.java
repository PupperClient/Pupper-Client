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
import net.minecraft.resources.ResourceLocation;
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
	private Map<Integer, Image> textures = new HashMap<>();

	public boolean load(int texture, float width, float height, SurfaceOrigin origin) {

		if (!textures.containsKey(texture)) {
			Image image = Image.adoptGLTextureFrom(SkiaContext.getContext(), texture, GL11.GL_TEXTURE_2D, (int) width,
					(int) height, GL11.GL_RGBA8, origin, ColorType.RGBA_8888);
			textures.put(texture, image);
		}

		return true;
	}

	public boolean load(ResourceLocation identifier) {
		
		if (!images.containsKey(identifier.getPath())) {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			Resource resource;
			try {
				resource = resourceManager.getResourceOrThrow(identifier);
				try (InputStream inputStream = resource.open()) {

					byte[] imageData = inputStream.readAllBytes();
					Image image = Image.makeDeferredFromEncodedBytes(imageData);
					if (image == null) {
						return false;
					}
					images.put(identifier.getPath(), image);
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

		if (textures.containsKey(texture)) {
			return textures.get(texture);
		}

		return null;
	}

	public void clear() {
		images.values().forEach(Image::close);
		images.clear();
		textures.values().forEach(Image::close);
		textures.clear();
	}
}
