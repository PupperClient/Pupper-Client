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

package cn.pupperclient.skia.font;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.types.Rect;

/**
 * Cached text measurements.
 *
 * <p>{@link Font#measureText} and {@link Font#getMetrics} are native calls, and a HUD
 * frame measures the same short strings many times: once to place the text, once to
 * fit it, and once more per shadow or badge. Both are pure functions of the font and
 * the string, so they are memoized here.
 */
public final class TextMetrics {

    /** Bounds both caches; a HUD shows far fewer distinct strings than this per frame. */
    private static final int MAX_ENTRIES = 4096;

    private record Key(Font font, String text) {}

    private static final Map<Key, Rect> bounds = new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Rect> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    private static final Map<Font, FontMetrics> metrics = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Font, FontMetrics> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    private TextMetrics() {}

    /**
     * Returns the ink bounds of {@code text}, measuring it only on first use.
     *
     * <p>The returned rectangle is shared; callers must not mutate it, which
     * {@link Rect} already prevents.
     */
    public static Rect bounds(Font font, String text) {
        if (text.isEmpty()) {
            return Rect.makeWH(0, 0);
        }
        Rect cached = bounds.get(new Key(font, text));
        if (cached != null) {
            return cached;
        }
        Rect measured = font.measureText(text);
        bounds.put(new Key(font, text), measured);
        return measured;
    }

    /** Returns the font metrics, reading them only on first use. */
    public static FontMetrics metrics(Font font) {
        return metrics.computeIfAbsent(font, Font::getMetrics);
    }

    public static void clear() {
        bounds.clear();
        metrics.clear();
    }
}
