/**
 * @Author: oneachina
 * @link: github.com/oneachina
 */
package cn.pupperclient.mixin.mixins.minecraft.client.render;

import cn.pupperclient.PupperClient;
import cn.pupperclient.mixin.mixins.accessors.GameRendererAccessor;
import cn.pupperclient.utils.minecraft.interfaces.IMinecraft;
import cn.pupperclient.utils.render.RenderUtils;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin implements IMinecraft {
    @Shadow
    public abstract void render(GpuBufferSlice fogBuffer);

    @Shadow
    public abstract void endFrame();

    @Unique
    private GuiRenderState renderState;

    @Inject(method = "draw", at = @At("HEAD"))
    private void draw(CallbackInfo ci) {
        var fogRenderer = ((GameRendererAccessor) client.gameRenderer).pupper$fogRenderer();

        if (RenderUtils.canUpdate()) {
            Profiler.get().push(PupperClient.getModId() + "_render_2d");

            RenderUtils.unscaledProjection();

            this.render(fogRenderer.getBuffer(FogRenderer.FogMode.NONE));

            RenderUtils.scaledProjection();

            Profiler.get().pop();
        }

        assert client.getMainRenderTarget().getDepthTexture() != null;
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(client.getMainRenderTarget().getDepthTexture(), 1.0);
        endFrame();
    }
}
