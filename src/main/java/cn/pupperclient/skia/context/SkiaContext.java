package cn.pupperclient.skia.context;

import cn.pupperclient.PupperLogger;
import cn.pupperclient.skia.image.ImageHelper;
import cn.pupperclient.utils.Unstable;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import io.github.humbleui.skija.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SkiaContext {

    private static DirectContext directContext = null;
    private static Surface skiaSurface = null;
    private static BackendRenderTarget renderTarget = null;

    private static GpuTexture currentTexture = null;
    private static NativeImage currentNativeImage = null;

    private static int surfaceWidth = 0;
    private static int surfaceHeight = 0;

    /**
     * 使用 GpuTexture 创建 Skia 表面
     */
    public static void createSurfaceFromGpuTexture(GpuTexture texture) {
        if (texture == null) {
            throw new IllegalArgumentException("GpuTexture cannot be null");
        }

        // 清理旧资源
        cleanup();

        // 保存当前纹理
        currentTexture = texture;
        surfaceWidth = texture.getWidth(0);
        surfaceHeight = texture.getHeight(0);

        // 创建 Skia 上下文（如果需要）
        if (directContext == null) {
            directContext = DirectContext.makeGL();
        }

        try {
            // 方法1：尝试创建 GPU 表面
            createGpuSurface(surfaceWidth, surfaceHeight);
        } catch (Exception e) {
            System.err.println("Failed to create GPU surface, falling back to CPU surface: " + e.getMessage());
            // 方法2：回退到 CPU 表面
            createCpuSurface(surfaceWidth, surfaceHeight);
        }

        // 从 GpuTexture 加载数据到表面
        loadTextureToSurface();
    }

    /**
     * 创建 GPU 表面（使用 OpenGL 纹理）
     */
    private static void createGpuSurface(int width, int height) {
        // 生成一个临时的 OpenGL 纹理供 Skia 使用
        int glTextureId = generateGlTexture(width, height);

        // 创建 Skia 渲染目标
        renderTarget = BackendRenderTarget.makeGL(
            width,
            height,
            0,          // 采样数
            8,          // 模板位数
            glTextureId,
            org.lwjgl.opengl.GL11.GL_RGBA8
        );

        // 创建 Skia 表面
        skiaSurface = Surface.wrapBackendRenderTarget(
            directContext,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.getSRGB()
        );

    }

    /**
     * 创建 CPU 表面（纯软件渲染）
     */
    private static void createCpuSurface(int width, int height) {
        ImageInfo imageInfo = ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL);
        skiaSurface = Surface.makeRaster(imageInfo);

    }

    /**
     * 生成临时的 OpenGL 纹理
     */
    private static int generateGlTexture(int width, int height) {
        // 使用 LWJGL 直接创建 OpenGL 纹理
        int[] textures = new int[1];
        org.lwjgl.opengl.GL11.glGenTextures(textures);
        int textureId = textures[0];

        org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, textureId);

        // 设置纹理参数
        org.lwjgl.opengl.GL11.glTexParameteri(
            org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
            org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER,
            org.lwjgl.opengl.GL11.GL_LINEAR
        );
        org.lwjgl.opengl.GL11.glTexParameteri(
            org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
            org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER,
            org.lwjgl.opengl.GL11.GL_LINEAR
        );

        // 分配纹理存储
        org.lwjgl.opengl.GL11.glTexImage2D(
            org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
            0,
            org.lwjgl.opengl.GL11.GL_RGBA8,
            width,
            height,
            0,
            org.lwjgl.opengl.GL11.GL_RGBA,
            org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE,
            (ByteBuffer) null
        );

        org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0);

        return textureId;
    }

    /**
     * 从 GpuTexture 加载数据到 Skia 表面
     */
    private static void loadTextureToSurface() {
        if (currentTexture == null || skiaSurface == null) {
            return;
        }

        try {
            // 读取 GpuTexture 数据到 ByteBuffer
            ByteBuffer pixelData = ImageHelper.readGpuTextureDataCorrect(currentTexture);
            if (pixelData == null) {
                return;
            }

            // 创建 Skia Image
            ImageInfo imageInfo = ImageInfo.makeS32(surfaceWidth, surfaceHeight, ColorAlphaType.UNPREMUL);
            Data data = Data.makeFromBytes(pixelData.array());

            try (Image sourceImage = Image.makeRasterFromData(imageInfo, data, imageInfo.getMinRowBytes())) {
                // 清除表面
                skiaSurface.getCanvas().clear(Color.makeARGB(0, 255, 255, 255)); //TRANSPARENT

                // 绘制到表面
                skiaSurface.getCanvas().drawImage(sourceImage, 0, 0);

                // 刷新
                if (directContext != null) {
                    directContext.flush();
                }
            }

        } catch (Exception e) {
            PupperLogger.error("SkiaContext", "image error:" + e);
        }
    }

    /**
     * 执行绘制逻辑
     */
    public static void draw(Consumer<Canvas> drawingLogic) {
        if (skiaSurface == null) {
            throw new IllegalStateException("Skia surface not initialized. Call createSurfaceFromGpuTexture() first.");
        }

        // 保存当前 OpenGL 状态
        saveGlState();

        try {
            // 获取画布
            Canvas canvas = getCanvas();
            if (canvas == null) {
                return;
            }

            // 执行用户绘制逻辑
            drawingLogic.accept(canvas);

            // 刷新上下文
            if (directContext != null) {
                directContext.flush();
            }

            // 将结果保存回 GpuTexture
            saveSurfaceToTexture();

        } finally {
            // 恢复 OpenGL 状态
            restoreGlState();
        }
    }

    /**
     * 将 Skia 表面内容保存回 GpuTexture
     */
    private static void saveSurfaceToTexture() {
        if (skiaSurface == null || currentTexture == null) {
            return;
        }

        try {
            // 获取表面快照

            try (Image snapshot = skiaSurface.makeImageSnapshot()) {
                // 将 Skia Image 转换为 NativeImage

                try (NativeImage nativeImage = convertSkiaImageToNativeImage(snapshot)) {
                    // 使用 CommandEncoder 写入 GpuTexture
                    var device = RenderSystem.getDevice();
                    var encoder = device.createCommandEncoder();

                    encoder.writeToTexture(currentTexture, nativeImage);

                }
            }

        } catch (Exception e) {
            PupperLogger.error("SkiaContext", "image error:" + e);
        }
    }

    /**
     * 将 Skia Image 转换为 NativeImage
     */
    @Unstable
    private static NativeImage convertSkiaImageToNativeImage(Image skiaImage) {
       return convertToNativeImageOptimized(skiaImage);
    }

    /**
     * 获取画布
     */
    @Nullable
    public static Canvas getCanvas() {
        return skiaSurface != null ? skiaSurface.getCanvas() : null;
    }

    /**
     * 获取 DirectContext
     */
    public static DirectContext getContext() {
        if (directContext == null) {
            directContext = DirectContext.makeGL();
        }
        return directContext;
    }

    /**
     * 清理资源
     */
    public static void cleanup() {
        if (skiaSurface != null) {
            skiaSurface.close();
            skiaSurface = null;
        }

        if (renderTarget != null) {
            renderTarget.close();
            renderTarget = null;
        }

        if (currentNativeImage != null) {
            currentNativeImage.close();
            currentNativeImage = null;
        }

        currentTexture = null;
        surfaceWidth = 0;
        surfaceHeight = 0;
    }

    /**
     * 完全关闭（释放所有资源）
     */
    public static void close() {
        cleanup();

        if (directContext != null) {
            directContext.close();
            directContext = null;
        }
    }

    /**
     * 保存当前 OpenGL 状态
     */
    private static void saveGlState() {
        // 使用直接 OpenGL 调用保存状态
        // 注意：1.21.5 移除了很多 RenderSystem 方法
        int[] prevTexture = new int[1];
        org.lwjgl.opengl.GL11.glGetIntegerv(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D, prevTexture);

        // 保存混合状态
        int[] blendEnabled = new int[1];
        org.lwjgl.opengl.GL11.glGetIntegerv(org.lwjgl.opengl.GL11.GL_BLEND, blendEnabled);

        // 保存视口
        int[] viewport = new int[4];
        org.lwjgl.opengl.GL11.glGetIntegerv(org.lwjgl.opengl.GL11.GL_VIEWPORT, viewport);

        // 存储到线程局部变量
        GlStateSavedState savedState = new GlStateSavedState();
        savedState.textureBinding = prevTexture[0];
        savedState.blendEnabled = blendEnabled[0] == org.lwjgl.opengl.GL11.GL_TRUE;
        savedState.viewportX = viewport[0];
        savedState.viewportY = viewport[1];
        savedState.viewportWidth = viewport[2];
        savedState.viewportHeight = viewport[3];

        ThreadLocalState.setState(savedState);
    }

    /**
     * 恢复 OpenGL 状态
     */
    private static void restoreGlState() {
        GlStateSavedState savedState = ThreadLocalState.getState();
        if (savedState == null) {
            return;
        }

        // 恢复纹理绑定
        org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, savedState.textureBinding);

        // 恢复混合状态
        if (savedState.blendEnabled) {
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_BLEND);
        } else {
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);
        }

        // 恢复视口
        org.lwjgl.opengl.GL11.glViewport(
            savedState.viewportX,
            savedState.viewportY,
            savedState.viewportWidth,
            savedState.viewportHeight
        );
    }

    /**
     * 创建一个简单的表面（用于独立的 Skia 绘制）
     */
    public static void createSimpleSurface(int width, int height) {
        cleanup();

        surfaceWidth = width;
        surfaceHeight = height;

        // 创建 CPU 表面
        createCpuSurface(width, height);
    }

    /**
     * 获取表面尺寸
     */
    public static int[] getSurfaceSize() {
        return new int[]{surfaceWidth, surfaceHeight};
    }

    /**
     * 检查表面是否已创建
     */
    public static boolean isSurfaceCreated() {
        return skiaSurface != null;
    }

    /**
     * 获取当前使用的纹理
     */
    @Nullable
    public static GpuTexture getCurrentTexture() {
        return currentTexture;
    }

    /**
     * 从 Minecraft 的帧缓冲创建表面
     */
    public static void createSurfaceFromFramebuffer() {
        var framebuffer = MinecraftClient.getInstance().getFramebuffer();
        GpuTexture colorTexture = framebuffer.getColorAttachment();

        if (colorTexture == null) {
            throw new IllegalStateException("Framebuffer has no color attachment");
        }

        createSurfaceFromGpuTexture(colorTexture);
    }

    // 线程局部状态保存
    private static class ThreadLocalState {
        private static final ThreadLocal<GlStateSavedState> STATE = new ThreadLocal<>();

        public static void setState(GlStateSavedState state) {
            STATE.set(state);
        }

        public static GlStateSavedState getState() {
            return STATE.get();
        }

        public static void clear() {
            STATE.remove();
        }
    }

    // OpenGL 状态保存结构
    private static class GlStateSavedState {
        int textureBinding = 0;
        boolean blendEnabled = false;
        int viewportX = 0;
        int viewportY = 0;
        int viewportWidth = 0;
        int viewportHeight = 0;
    }

    private static NativeImage convertToNativeImageOptimized(Image skiaImage) {
        int width = skiaImage.getWidth();
        int height = skiaImage.getHeight();

        NativeImage nativeImage = new NativeImage(
            net.minecraft.client.texture.NativeImage.Format.RGBA,
            width,
            height,
            false
        );

        try {
            // 创建临时 Bitmap
            Bitmap bitmap = new Bitmap();
            ImageInfo bitmapInfo = ImageInfo.makeS32(width, height, ColorAlphaType.UNPREMUL);

            if (!bitmap.allocPixels(bitmapInfo)) {
                System.err.println("无法分配 Bitmap 内存");
                bitmap.close();
                return nativeImage;
            }

            // 读取像素
            if (!skiaImage.readPixels(bitmap, 0, 0)) {
                System.err.println("无法读取像素");
                bitmap.close();
                return nativeImage;
            }

            // 获取像素缓冲区
            ByteBuffer pixelBuffer = bitmap.peekPixels();
            if (pixelBuffer == null) {
                System.err.println("无法获取像素缓冲区");
                bitmap.close();
                return nativeImage;
            }

            // 使用更高效的转换方法
            if (pixelBuffer.hasArray()) {
                // 使用数组访问（最快）
                byte[] pixelArray = pixelBuffer.array();
                int offset = pixelBuffer.arrayOffset();

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int idx = offset + (y * width + x) * 4;

                        int r = pixelArray[idx] & 0xFF;
                        int g = pixelArray[idx + 1] & 0xFF;
                        int b = pixelArray[idx + 2] & 0xFF;
                        int a = pixelArray[idx + 3] & 0xFF;

                        int abgrColor = (a << 24) | (b << 16) | (g << 8) | r;
                        nativeImage.setColor(x, y, abgrColor);
                    }
                }
            } else {
                // 使用 ByteBuffer 访问
                pixelBuffer.rewind();

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int r = pixelBuffer.get() & 0xFF;
                        int g = pixelBuffer.get() & 0xFF;
                        int b = pixelBuffer.get() & 0xFF;
                        int a = pixelBuffer.get() & 0xFF;

                        int abgrColor = (a << 24) | (b << 16) | (g << 8) | r;
                        nativeImage.setColor(x, y, abgrColor);
                    }
                }
            }

            bitmap.close();

        } catch (Exception e) {
            e.printStackTrace();
            nativeImage.close();
        }
        return nativeImage;
    }
}
