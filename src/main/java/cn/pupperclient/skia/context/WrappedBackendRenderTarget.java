package cn.pupperclient.skia.context;

import io.github.humbleui.skija.BackendRenderTarget;
import io.github.humbleui.skija.impl.Stats;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class WrappedBackendRenderTarget extends BackendRenderTarget {
    private final int width;
    private final int height;
    private final int sampleCnt;
    private final int stencilBits;
    private final int fbId;
    private final int fbFormat;

    public WrappedBackendRenderTarget(
            int width,
            int height,
            int sampleCnt,
            int stencilBits,
            int fbId,
            int fbFormat,
            long ptr
    ) {
        super(ptr);
        this.width = width;
        this.height = height;
        this.sampleCnt = sampleCnt;
        this.stencilBits = stencilBits;
        this.fbId = fbId;
        this.fbFormat = fbFormat;
    }

    /**
     * @see BackendRenderTarget#makeGL(int, int, int, int, int, int)
     */
    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull WrappedBackendRenderTarget makeGL(
            int width,
            int height,
            int sampleCnt,
            int stencilBits,
            int fbId,
            int fbFormat
    ) {
        Stats.onNativeCall();
        return new WrappedBackendRenderTarget(
                width,
                height,
                sampleCnt,
                stencilBits,
                fbId,
                fbFormat,
                _nMakeGL(width, height, sampleCnt, stencilBits, fbId, fbFormat)
        );
    }

    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    @SuppressWarnings("unused") public int getSampleCnt() { return sampleCnt; }
    @SuppressWarnings("unused") public int getStencilBits() { return stencilBits; }
    @SuppressWarnings("unused") public int getFbId() { return fbId; }
    @SuppressWarnings("unused") public int getFbFormat() { return fbFormat; }
}
