package cn.pupperclient.mixin.mixins.minecraft.client.gui;

import cn.pupperclient.management.mod.api.Position;
import cn.pupperclient.management.mod.impl.hud.BossBarMod;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(BossHealthOverlay.class)
public abstract class MixinBossBarHud {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Map<UUID, LerpingBossEvent> events = Maps.newLinkedHashMap();

    @Shadow
    private void extractBar(GuiGraphicsExtractor graphics, int x, int y, BossEvent event, int width, Identifier[] sprites, Identifier[] overlaySprites) {
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onExtractRenderState(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        BossBarMod mod = BossBarMod.getInstance();

        if (mod.isEnabled()) {
            if (!mod.isVanillaPosition()) {
                Position position = mod.getPosition();
                onCustomRender(graphics, (int) position.getX(), (int) position.getY());
                position.setScale(1.0F);
                position.setSize(182, 14);
                ci.cancel();
            }
        } else {
            ci.cancel();
        }
    }

    @Unique
    private void onCustomRender(GuiGraphicsExtractor graphics, int x, int y) {
        if (!this.events.isEmpty()) {
            ProfilerFiller profiler = Profiler.get();
            profiler.push("bossHealth");

            int currentY = y;
            for (LerpingBossEvent event : this.events.values()) {
                Component name = event.getName();
                int textWidth = this.minecraft.font.width(name);
                int textX = x - textWidth / 2;
                graphics.text(this.minecraft.font, name, textX, currentY, -1);

                int barX = x - 91;
                int barY = currentY + 9;
                this.drawCustomBar(graphics, barX, barY, event);

                currentY += 19; // 10 + 9
                if (currentY >= graphics.guiHeight() / 3) {
                    break;
                }
            }

            profiler.pop();
        }
    }

    @Unique
    private void drawCustomBar(GuiGraphicsExtractor graphics, int x, int y, BossEvent event) {
        // 绘制背景
        Identifier[] bgSprites = new Identifier[]{
            Identifier.withDefaultNamespace("boss_bar/pink_background"),
            Identifier.withDefaultNamespace("boss_bar/blue_background"),
            Identifier.withDefaultNamespace("boss_bar/red_background"),
            Identifier.withDefaultNamespace("boss_bar/green_background"),
            Identifier.withDefaultNamespace("boss_bar/yellow_background"),
            Identifier.withDefaultNamespace("boss_bar/purple_background"),
            Identifier.withDefaultNamespace("boss_bar/white_background")
        };
        Identifier[] progressSprites = new Identifier[]{
            Identifier.withDefaultNamespace("boss_bar/pink_progress"),
            Identifier.withDefaultNamespace("boss_bar/blue_progress"),
            Identifier.withDefaultNamespace("boss_bar/red_progress"),
            Identifier.withDefaultNamespace("boss_bar/green_progress"),
            Identifier.withDefaultNamespace("boss_bar/yellow_progress"),
            Identifier.withDefaultNamespace("boss_bar/purple_progress"),
            Identifier.withDefaultNamespace("boss_bar/white_progress")
        };
        Identifier[] overlayBgSprites = new Identifier[]{
            Identifier.withDefaultNamespace("boss_bar/notched_6_background"),
            Identifier.withDefaultNamespace("boss_bar/notched_10_background"),
            Identifier.withDefaultNamespace("boss_bar/notched_12_background"),
            Identifier.withDefaultNamespace("boss_bar/notched_20_background")
        };
        Identifier[] overlayProgressSprites = new Identifier[]{
            Identifier.withDefaultNamespace("boss_bar/notched_6_progress"),
            Identifier.withDefaultNamespace("boss_bar/notched_10_progress"),
            Identifier.withDefaultNamespace("boss_bar/notched_12_progress"),
            Identifier.withDefaultNamespace("boss_bar/notched_20_progress")
        };

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
            bgSprites[event.getColor().ordinal()], 182, 5, 0, 0, x, y, 182, 5);
        if (event.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                overlayBgSprites[event.getOverlay().ordinal() - 1], 182, 5, 0, 0, x, y, 182, 5);
        }

        int progressWidth = Mth.lerpDiscrete(event.getProgress(), 0, 182);
        if (progressWidth > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                progressSprites[event.getColor().ordinal()], 182, 5, 0, 0, x, y, progressWidth, 5);
            if (event.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    overlayProgressSprites[event.getOverlay().ordinal() - 1], 182, 5, 0, 0, x, y, progressWidth, 5);
            }
        }
    }
}
