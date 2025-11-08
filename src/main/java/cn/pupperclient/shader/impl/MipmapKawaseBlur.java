package cn.pupperclient.shader.impl;

import cn.pupperclient.management.mod.impl.settings.SystemSettings;
import cn.pupperclient.shader.Framebuffer;
import cn.pupperclient.shader.PostProcessRenderer;
import cn.pupperclient.shader.Shader;
import cn.pupperclient.shader.ShaderHelper;
import cn.pupperclient.utils.TimerUtils;

import it.unimi.dsi.fastutil.ints.IntDoubleImmutablePair;
import net.minecraft.client.MinecraftClient;

public class MipmapKawaseBlur {

    public static final MipmapKawaseBlur GUI_BLUR = new MipmapKawaseBlur();
    public static final MipmapKawaseBlur INGAME_BLUR = new MipmapKawaseBlur();

    private static final IntDoubleImmutablePair[] STRENGTHS = new IntDoubleImmutablePair[] {
        IntDoubleImmutablePair.of(1, 0.5),
        IntDoubleImmutablePair.of(1, 0.75),
        IntDoubleImmutablePair.of(1, 1.0),
        IntDoubleImmutablePair.of(2, 0.75),
        IntDoubleImmutablePair.of(2, 1.0),
        IntDoubleImmutablePair.of(2, 1.25),
        IntDoubleImmutablePair.of(2, 1.5),
        IntDoubleImmutablePair.of(3, 1.0),
        IntDoubleImmutablePair.of(3, 1.25),
        IntDoubleImmutablePair.of(3, 1.5),
        IntDoubleImmutablePair.of(3, 1.75),
        IntDoubleImmutablePair.of(3, 2.0),
        IntDoubleImmutablePair.of(4, 1.25),
        IntDoubleImmutablePair.of(4, 1.5),
        IntDoubleImmutablePair.of(4, 1.75),
        IntDoubleImmutablePair.of(4, 2.0),
        IntDoubleImmutablePair.of(4, 2.25),
        IntDoubleImmutablePair.of(5, 1.5),
        IntDoubleImmutablePair.of(5, 1.75),
        IntDoubleImmutablePair.of(5, 2.0)
    };

    private static Shader shaderDown, shaderUp, shaderPassthrough, mipmapBlurShader;
    private final Framebuffer[] fbos = new Framebuffer[6];
    private final TimerUtils timer = new TimerUtils();
    private boolean firstTick = true;
    private int mipmapLevel = 0;
    private int currentIterations = 0;

    public void setMipmapLevel(int level) {
        this.mipmapLevel = Math.max(0, Math.min(level, 4));
        resize();
    }

    public void resize() {
        for (int i = 0; i < fbos.length; i++) {
            if (fbos[i] != null) {
                fbos[i].resize();
            } else {
                double baseMulti = 1.0 / Math.pow(2, mipmapLevel);
                double levelMulti = baseMulti / Math.pow(2, i);
                fbos[i] = new Framebuffer(levelMulti);
                if (mipmapLevel > 0 && i == 0) {
                    fbos[i].enableMipmap();
                }
            }
        }
    }

    public void draw(int radius) {
        if (shaderDown == null) {
            shaderDown = new Shader("blur.vert", "blur_down.frag");
            shaderUp = new Shader("blur.vert", "blur_up.frag");
            shaderPassthrough = new Shader("passthrough.vert", "passthrough.frag");
            mipmapBlurShader = new Shader("blur.vert", "mipmap_kawase.frag");
        }

        if (firstTick) {
            resize();
            firstTick = false;
        }

        SystemSettings setting = SystemSettings.getInstance();

        if(setting.isFastBlur()) {
            if (timer.delay(16)) {
                timer.reset();
            } else {
                return;
            }
        }

        IntDoubleImmutablePair strength = STRENGTHS[Math.min(radius - 1, STRENGTHS.length - 1)];
        int iterations = strength.leftInt();
        double offset = strength.rightDouble();

        if (currentIterations != iterations) {
            currentIterations = iterations;
            resize();
        }

        PostProcessRenderer.beginRender();

        if (mipmapLevel > 0) {
            drawMipmapEnhanced(iterations, offset);
        } else {
            drawTraditional(iterations, offset);
        }

        PostProcessRenderer.endRender();
    }

    private void drawTraditional(int iterations, double offset) {
        renderToFbo(fbos[0], MinecraftClient.getInstance().getFramebuffer().getColorAttachment(), shaderDown, offset);

        for (int i = 0; i < iterations; i++) {
            renderToFbo(fbos[i + 1], fbos[i].texture, shaderDown, offset);
        }

        for (int i = iterations; i >= 1; i--) {
            renderToFbo(fbos[i - 1], fbos[i].texture, shaderUp, offset);
        }

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        shaderPassthrough.bind();
        ShaderHelper.bindTexture(fbos[0].texture);
        shaderPassthrough.set("uTexture", 0);
        PostProcessRenderer.render();
    }

    private void drawMipmapEnhanced(int iterations, double offset) {
        renderToFbo(fbos[0], MinecraftClient.getInstance().getFramebuffer().getColorAttachment(), shaderDown, offset);

        fbos[0].generateMipmap();

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        mipmapBlurShader.bind();
        ShaderHelper.bindTexture(fbos[0].texture);
        mipmapBlurShader.set("uTexture", 0);
        mipmapBlurShader.set("uMipmapLevel", mipmapLevel);
        mipmapBlurShader.set("uIterations", iterations);
        mipmapBlurShader.set("uOffset", (float) offset);
        mipmapBlurShader.set("uResolution",
            (float) MinecraftClient.getInstance().getWindow().getFramebufferWidth(),
            (float) MinecraftClient.getInstance().getWindow().getFramebufferHeight()
        );
        PostProcessRenderer.render();
    }

    public int getTexture() {
        return fbos[0].texture;
    }

    private void renderToFbo(Framebuffer targetFbo, int sourceText, Shader shader, double offset) {
        targetFbo.bind();
        targetFbo.setViewport();
        shader.bind();
        ShaderHelper.bindTexture(sourceText);
        shader.set("uTexture", 0);
        shader.set("uHalfTexelSize", .5 / targetFbo.width, .5 / targetFbo.height);
        shader.set("uOffset", offset);
        PostProcessRenderer.render();
    }
}
