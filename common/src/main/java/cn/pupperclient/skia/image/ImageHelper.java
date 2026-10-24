/*
 * Copyright (C) 2024-2026 Pupper Client contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.pupperclient.skia.image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import cn.pupperclient.PupperLogger;
import cn.pupperclient.skia.utils.SkiaUtils;

import io.github.humbleui.skija.Image;

/**
 * Decoded-image cache shared by the {@link cn.pupperclient.skia.Skia} draw helpers.
 *
 * <p>Cache keys are stable identities - resource paths, absolute file paths and
 * identifiers - so two files that merely share a name never alias each other.
 * Every load reports whether it produced an image; callers must not draw without one.
 */
public class ImageHelper {

    /**
     * Bounded so a long session cannot retain every skin and cape it has ever drawn.
     * Eviction closes the decoded image; the UI texture cache drops the matching
     * sprite on its next frame.
     */
    private static final int MAX_CACHED_IMAGES = 256;

    private final Map<String, Image> images = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
            if (size() <= MAX_CACHED_IMAGES) {
                return false;
            }
            eldest.getValue().close();
            return true;
        }
    };

    public boolean load(Identifier identifier) {
        String key = identifier.toString();
        if (images.containsKey(key)) {
            return true;
        }

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        try {
            Resource resource = resourceManager.getResourceOrThrow(identifier);
            try (InputStream inputStream = resource.open()) {
                return decode(key, inputStream.readAllBytes());
            }
        } catch (IOException e) {
            PupperLogger.error("ImageHelper", "Failed to read identifier bytes: " + identifier, e);
            return false;
        }
    }

    public boolean load(String resourcePath) {
        if (images.containsKey(resourcePath)) {
            return true;
        }
        return SkiaUtils.convertToBytes(resourcePath).map(bytes -> decode(resourcePath, bytes)).orElse(false);
    }

    public boolean load(File file) {
        String key = file.getAbsolutePath();
        if (images.containsKey(key)) {
            return true;
        }

        try {
            return decode(key, Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            PupperLogger.error("ImageHelper", "Failed to load image from file: " + file, e);
            return false;
        }
    }

    /**
     * Returns the decoded image, loading it on first use.
     *
     * @return the image, or {@code null} when it is missing or cannot be decoded
     */
    public Image getOrLoad(Identifier identifier) {
        return load(identifier) ? get(identifier) : null;
    }

    /** @see #getOrLoad(Identifier) */
    public Image getOrLoad(String resourcePath) {
        return load(resourcePath) ? get(resourcePath) : null;
    }

    /** @see #getOrLoad(Identifier) */
    public Image getOrLoad(File file) {
        return load(file) ? get(file) : null;
    }

    public Image get(String key) {
        return images.get(key);
    }

    public Image get(File file) {
        return images.get(file.getAbsolutePath());
    }

    public Image get(Identifier identifier) {
        return images.get(identifier.toString());
    }

    public void clear() {
        images.values().forEach(Image::close);
        images.clear();
    }

    private boolean decode(String key, byte[] encoded) {
        try {
            images.put(key, Image.makeDeferredFromEncodedBytes(encoded));
            return true;
        } catch (RuntimeException e) {
            PupperLogger.error("ImageHelper", "Failed to decode image: " + key, e);
            return false;
        }
    }
}
