package cn.pupperclient.skia.image;

import cn.pupperclient.PupperLogger;
import cn.pupperclient.skia.utils.SkiaUtils;
import cn.pupperclient.utils.Unstable;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import io.github.humbleui.skija.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ImageHelper {
    private static final ExecutorService LOADER_EXECUTOR = Executors.newFixedThreadPool(2);

    // 缓存系统
    private final Map<String, Image> fileImages = new ConcurrentHashMap<>();
    private final Map<Identifier, Image> resourceImages = new ConcurrentHashMap<>();
    private final Map<GpuTexture, Image> gpuTextureImages = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Image>> loadingFutures = new ConcurrentHashMap<>();

    // 回调系统
    private final Map<String, Consumer<Image>> callbacks = new ConcurrentHashMap<>();

    /**
     * 从 GpuTexture 加载图像
     */
    public CompletableFuture<Image> loadAsync(GpuTexture texture) {
        if (texture == null) {
            return CompletableFuture.completedFuture(null);
        }

        // 检查缓存
        if (gpuTextureImages.containsKey(texture)) {
            return CompletableFuture.completedFuture(gpuTextureImages.get(texture));
        }

        String key = "gpu:" + texture.hashCode();
        if (loadingFutures.containsKey(key)) {
            return loadingFutures.get(key);
        }

        CompletableFuture<Image> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 读取 GpuTexture 数据
                ByteBuffer pixelData = readGpuTextureData(texture);
                if (pixelData == null) {
                    return null;
                }

                // 创建 Skia Image
                ImageInfo info = ImageInfo.makeS32(
                    texture.getWidth(0),
                    texture.getHeight(0),
                    ColorAlphaType.UNPREMUL
                );

                Data data = Data.makeFromBytes(pixelData.array());
                Image image = Image.makeRasterFromData(info, data, info.getMinRowBytes());

                // 缓存结果
                gpuTextureImages.put(texture, image);
                return image;

            } catch (Exception e) {
                PupperLogger.error("ImageHelper", "image error:" + e);
                return null;
            }
        }, LOADER_EXECUTOR);

        loadingFutures.put(key, future);
        future.whenComplete((image, error) -> loadingFutures.remove(key));

        return future;
    }

    /**
     * 同步从 GpuTexture 加载图像
     */
    @Nullable
    @Unstable(reason = "float will be coerced to int")
    public Image load(GpuTexture texture, float width, float height) {
        if (texture == null) {
            return null;
        }

        // 检查缓存
        if (gpuTextureImages.containsKey(texture)) {
            return gpuTextureImages.get(texture);
        }

        try {
            // 必须在渲染线程执行
            RenderSystem.assertOnRenderThread();

            // 读取 GpuTexture 数据
            ByteBuffer pixelData = readGpuTextureData(texture);
            if (pixelData == null) {
                return null;
            }

            // 创建 Skia Image
            ImageInfo info = ImageInfo.makeS32(
                (int) width,
                (int) height,
                ColorAlphaType.UNPREMUL
            );

            Data data = Data.makeFromBytes(pixelData.array());
            Image image = Image.makeRasterFromData(info, data, info.getMinRowBytes());

            // 缓存结果
            gpuTextureImages.put(texture, image);
            return image;

        } catch (Exception e) {
            PupperLogger.error("ImageHelper", "image error:" + e);
            return null;
        }
    }

    /**
     * 从文件路径异步加载图像
     */
    public CompletableFuture<Image> loadAsync(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        // 检查缓存
        if (fileImages.containsKey(filePath)) {
            return CompletableFuture.completedFuture(fileImages.get(filePath));
        }

        if (loadingFutures.containsKey(filePath)) {
            return loadingFutures.get(filePath);
        }

        CompletableFuture<Image> future = CompletableFuture.supplyAsync(() -> {
            try {
                Optional<byte[]> encodedBytes = SkiaUtils.convertToBytes(filePath);
                if (encodedBytes.isEmpty()) {
                    return null;
                }

                Image image = Image.makeDeferredFromEncodedBytes(encodedBytes.get());
                fileImages.put(filePath, image);

                return image;

            } catch (Exception e) {
                PupperLogger.error("ImageHelper", "image error:" + e);
                return null;
            }
        }, LOADER_EXECUTOR);

        loadingFutures.put(filePath, future);
        future.whenComplete((image, error) -> {
            loadingFutures.remove(filePath);
            if (image != null && callbacks.containsKey(filePath)) {
                callbacks.get(filePath).accept(image);
                callbacks.remove(filePath);
            }
        });

        return future;
    }

    /**
     * 从文件异步加载图像
     */
    public CompletableFuture<Image> loadAsync(File file) {
        if (file == null || !file.exists()) {
            return CompletableFuture.completedFuture(null);
        }

        String filePath = file.getAbsolutePath();
        return loadAsync(filePath);
    }

    /**
     * 从资源标识符异步加载图像
     */
    public CompletableFuture<Image> loadAsync(Identifier identifier) {
        if (identifier == null) {
            return CompletableFuture.completedFuture(null);
        }

        // 检查缓存
        if (resourceImages.containsKey(identifier)) {
            return CompletableFuture.completedFuture(resourceImages.get(identifier));
        }

        String key = "resource:" + identifier.toString();
        if (loadingFutures.containsKey(key)) {
            return loadingFutures.get(key);
        }

        CompletableFuture<Image> future = CompletableFuture.supplyAsync(() -> {
            try {
                ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
                Resource resource = resourceManager.getResource(identifier).orElse(null);
                if (resource == null) {
                    return null;
                }

                try (InputStream inputStream = resource.getInputStream()) {
                    byte[] imageData = inputStream.readAllBytes();
                    Image image = Image.makeDeferredFromEncodedBytes(imageData);

                    resourceImages.put(identifier, image);

                    return image;
                }

            } catch (IOException e) {
                PupperLogger.error("ImageHelper", "image error:" + e);
                return null;
            }
        }, LOADER_EXECUTOR);

        loadingFutures.put(key, future);
        future.whenComplete((image, error) -> loadingFutures.remove(key));

        return future;
    }

    /**
     * 同步加载文件
     */
    @Nullable
    public Image load(String filePath) {
        try {
            Optional<byte[]> encodedBytes = SkiaUtils.convertToBytes(filePath);
            if (encodedBytes.isEmpty()) {
                return null;
            }

            Image image = Image.makeDeferredFromEncodedBytes(encodedBytes.get());
            fileImages.put(filePath, image);

            return image;

        } catch (Exception e) {
            PupperLogger.error("ImageHelper", "image error:" + e);
            return null;
        }
    }

    /**
     * 同步加载文件
     */
    @Nullable
    public Image load(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] encoded = org.apache.commons.io.IOUtils.toByteArray(fis);
            Image image = Image.makeDeferredFromEncodedBytes(encoded);

            fileImages.put(file.getAbsolutePath(), image);

            return image;

        } catch (IOException e) {
            PupperLogger.error("ImageHelper", "image error:" + e);
            return null;
        }
    }

    /**
     * 同步加载资源
     */
    @Nullable
    public Image load(Identifier identifier) {
        if (identifier == null) {
            return null;
        }

        // 检查缓存
        if (resourceImages.containsKey(identifier)) {
            return resourceImages.get(identifier);
        }

        try {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            Resource resource = resourceManager.getResource(identifier).orElse(null);
            if (resource == null) {
                return null;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                byte[] imageData = inputStream.readAllBytes();
                Image image = Image.makeDeferredFromEncodedBytes(imageData);

                resourceImages.put(identifier, image);

                return image;
            }

        } catch (IOException e) {
            PupperLogger.error("ImageHelper", "image error:" + e);
            return null;
        }
    }

    /**
     * 添加加载完成回调
     */
    public void addCallback(String key, Consumer<Image> callback) {
        callbacks.put(key, callback);
    }

    /**
     * 获取缓存的图像
     */
    @Nullable
    public Image get(String key) {
        return fileImages.get(key);
    }

    @Nullable
    public Image get(Identifier identifier) {
        return resourceImages.get(identifier);
    }

    @Nullable
    public Image get(GpuTexture texture) {
        return gpuTextureImages.get(texture);
    }

    public static ByteBuffer readGpuTextureData(GpuTexture texture) {
        if (texture == null) {
            return null;
        }

        RenderSystem.assertOnRenderThread();

        try {
            int width = texture.getWidth(0);
            int height = texture.getHeight(0);
            int bufferSize = width * height * 4; // RGBA8

            // 创建用于存储读取结果的引用
            AtomicReference<ByteBuffer> result = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            // 创建 GPU 缓冲区用于读取
            GpuBuffer readBuffer = RenderSystem.getDevice().createBuffer(
                () -> "TextureReadBuffer",
                BufferType.PIXEL_PACK,
                BufferUsage.STREAM_READ,
                bufferSize
            );

            try {
                // 创建命令编码器
                var commandEncoder = RenderSystem.getDevice().createCommandEncoder();

                // 复制纹理数据到缓冲区，并设置回调
                commandEncoder.copyTextureToBuffer(
                    texture,
                    readBuffer,
                    0,
                    () -> {
                        // 数据已上传到缓冲区的回调
                        try {
                            // 在回调中读取缓冲区数据
                            try (GpuBuffer.ReadView readView = commandEncoder.readBuffer(readBuffer)) {
                                ByteBuffer pixelData = BufferUtils.createByteBuffer(bufferSize);
                                ByteBuffer sourceBuffer = readView.data();
                                sourceBuffer.rewind();
                                pixelData.put(sourceBuffer);
                                pixelData.flip();

                                result.set(pixelData);
                            }
                        } catch (Exception e) {
                            PupperLogger.error("ImageHelper", "image error:" + e);
                        } finally {
                            latch.countDown(); // 通知主线程数据已读取完成
                        }
                    },
                    0 // mipLevel
                );

                // 等待数据读取完成
                boolean success = latch.await(5, TimeUnit.SECONDS);
                if (!success) {
                    System.err.println("读取纹理数据超时");
                    return null;
                }

                return result.get();

            } finally {
                readBuffer.close();
            }

        } catch (Exception e) {
            PupperLogger.error("ImageHelper", "image error:" + e);
            return null;
        }
    }

    @Nullable
    @Deprecated
    public GpuTexture convertToGpuTexture(Image image) {
        if (image == null) {
            return null;
        }

        try {
            RenderSystem.assertOnRenderThread();

            int width = image.getWidth();
            int height = image.getHeight();

            // 获取图像数据
            ImageInfo info = image.getImageInfo();
            Data data = image.encodeToData(EncodedImageFormat.PNG);
            if (data == null) {
                return null;
            }

            // 创建 GpuTexture
            GpuTexture texture = RenderSystem.getDevice().createTexture(
                () -> "SkiaImageTexture",
                com.mojang.blaze3d.textures.TextureFormat.RGBA8,
                width,
                height,
                1
            );

            return texture;

        } catch (Exception e) {
            PupperLogger.error("ImageHelper", "image error:" + e);
            return null;
        }
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        // 关闭所有缓存的图像
        for (Image image : fileImages.values()) {
            image.close();
        }
        for (Image image : resourceImages.values()) {
            image.close();
        }
        for (Image image : gpuTextureImages.values()) {
            image.close();
        }

        fileImages.clear();
        resourceImages.clear();
        gpuTextureImages.clear();
        loadingFutures.clear();
        callbacks.clear();
    }

    /**
     * 清理特定类型的缓存
     */
    public void clearFileCache() {
        fileImages.values().forEach(Image::close);
        fileImages.clear();
    }

    public void clearResourceCache() {
        resourceImages.values().forEach(Image::close);
        resourceImages.clear();
    }

    public void clearGpuTextureCache() {
        gpuTextureImages.values().forEach(Image::close);
        gpuTextureImages.clear();
    }

    /**
     * 检查图像是否已缓存
     */
    public boolean isCached(String key) {
        return fileImages.containsKey(key);
    }

    public boolean isCached(Identifier identifier) {
        return resourceImages.containsKey(identifier);
    }

    public boolean isCached(GpuTexture texture) {
        return gpuTextureImages.containsKey(texture);
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
            fileImages.size(),
            resourceImages.size(),
            gpuTextureImages.size(),
            loadingFutures.size()
        );
    }

    /**
     * 预加载常用资源
     */
    public void preloadCommonResources() {

    }

    @Unstable
    public static ByteBuffer readGpuTextureDataCorrect(GpuTexture texture) {
        if (texture == null) {
            return null;
        }

        RenderSystem.assertOnRenderThread();

        try {
            int width = texture.getWidth(0);
            int height = texture.getHeight(0);
            int pixelSize = texture.getFormat().pixelSize(); // 每个像素的字节数
            int bufferSize = width * height * pixelSize;

            // 创建缓冲区

            // 使用 CountDownLatch 等待异步操作完成

            try (GpuBuffer readBuffer = RenderSystem.getDevice().createBuffer(
                () -> "GpuTextureReadBuffer",
                BufferType.PIXEL_PACK,
                BufferUsage.STREAM_READ,
                bufferSize
            )) {
                CountDownLatch latch = new CountDownLatch(1);
                ByteBuffer[] resultHolder = new ByteBuffer[1];
                // 创建命令编码器
                var commandEncoder = RenderSystem.getDevice().createCommandEncoder();

                commandEncoder.copyTextureToBuffer(
                    texture,
                    readBuffer,
                    0,
                    () -> {
                        try {
                            try (GpuBuffer.ReadView readView = commandEncoder.readBuffer(readBuffer)) {

                                ByteBuffer pixelData = BufferUtils.createByteBuffer(bufferSize);
                                ByteBuffer sourceData = readView.data();
                                if (sourceData != null) {
                                    sourceData.rewind();
                                    pixelData.put(sourceData);
                                    pixelData.flip();
                                    resultHolder[0] = pixelData;
                                }
                            }
                        } catch (Exception e) {
                            PupperLogger.error("ImageHelper", "image error:" + e);
                        } finally {
                            latch.countDown();
                        }
                    },
                    0 // mipLevel
                );

                // 等待操作完成（最大等待5秒）
                boolean success = latch.await(5, TimeUnit.SECONDS);
                if (!success) {
                    System.err.println("读取纹理数据超时");
                    return null;
                }

                return resultHolder[0];

            }

        } catch (Exception e) {
            PupperLogger.error("ImageHelper", "image error:" + e);
            return null;
        }
    }

    public void close() {
        clearCache();
        LOADER_EXECUTOR.shutdown();
    }

    public record CacheStats(int fileImages, int resourceImages, int gpuTextureImages, int loadingTasks) {

        @Override
            public String toString() {
                return String.format(
                    "CacheStats{文件图像=%d, 资源图像=%d, GPU纹理=%d, 加载任务=%d}",
                    fileImages, resourceImages, gpuTextureImages, loadingTasks
                );
            }
    }

    private static final ImageHelper INSTANCE = new ImageHelper();

    public static ImageHelper getInstance() {
        return INSTANCE;
    }
}
