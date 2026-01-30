package cn.pupperclient.utils;

import com.mojang.blaze3d.textures.TextureFormat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class TextureUtils {
    public static int getInternalFormat(TextureFormat format) {
        return switch (format) {
            case RGBA8 -> GL11.GL_RGBA8;
            case RED8 -> GL30.GL_R8;
            case DEPTH32 -> GL30.GL_DEPTH_COMPONENT32;
        };
    }

    public static int getPixelFormat(TextureFormat format) {
        return switch (format) {
            case RGBA8 -> GL11.GL_RGBA;
            case RED8 -> GL11.GL_RED;
            case DEPTH32 -> GL11.GL_DEPTH_COMPONENT;
        };
    }

    public static int getType(TextureFormat format) {
        return switch (format) {
            case RGBA8, RED8 -> GL11.GL_UNSIGNED_BYTE;
            case DEPTH32 -> GL11.GL_FLOAT;
        };
    }
}
