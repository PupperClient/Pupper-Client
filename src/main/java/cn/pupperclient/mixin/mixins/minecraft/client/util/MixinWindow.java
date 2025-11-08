package cn.pupperclient.mixin.mixins.minecraft.client.util;

import cn.pupperclient.Soar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.skia.context.SkiaContext;

import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(Window.class)
public class MixinWindow {

	@Inject(method = "onFramebufferSizeChanged", at = @At("RETURN"))
	private void onFramebufferSizeChanged(long window, int width, int height, CallbackInfo ci) {
		SkiaContext.createSurface(width, height);
	}

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onWindowInit(CallbackInfo ci) {
        try {
            // 获取窗口句柄
            long handle = ((Window)(Object)this).getHandle();

            // 从资源加载图标
            try (InputStream is = getClass().getResourceAsStream("/assets/pupper/logo.png")) {
                if (is == null) {
                    System.err.println("Soar icon not found!");
                    return;
                }

                byte[] imageBytes = is.readAllBytes();
                ByteBuffer imageBuffer = ByteBuffer.allocateDirect(imageBytes.length);
                imageBuffer.put(imageBytes);
                imageBuffer.flip();

                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer width = stack.mallocInt(10);
                    IntBuffer height = stack.mallocInt(10);
                    IntBuffer channels = stack.mallocInt(2);

                    ByteBuffer iconBuffer = STBImage.stbi_load_from_memory(
                        imageBuffer, width, height, channels, 4
                    );

                    if (iconBuffer == null) {
                        System.err.println("Failed to load Soar icon: " + STBImage.stbi_failure_reason());
                        return;
                    }

                    GLFWImage image = GLFWImage.malloc(stack);
                    image.set(width.get(0), height.get(0), iconBuffer);

                    GLFWImage.Buffer images = GLFWImage.malloc(1, stack);
                    images.put(0, image);

                    GLFW.glfwSetWindowIcon(handle, images);

                    STBImage.stbi_image_free(iconBuffer);
                    Soar.LOGGER.info("Soar icon loaded successfully!");
                }
            }
        } catch (IOException e) {
            Soar.LOGGER.error("Error loading Soar icon: {}", e.getMessage());
        }
    }

    @Inject(method = "setIcon", at = @At("HEAD"), cancellable = true)
    private void onSetIcon(CallbackInfo ci) {
        ci.cancel();
    }
}
