package cn.pupperclient.mixin.mixins.minecraft.client.render;

import cn.pupperclient.PupperClient;
import cn.pupperclient.PupperLogger;
import cn.pupperclient.management.mod.impl.misc.HypixelMod;
import cn.pupperclient.utils.server.Server;
import cn.pupperclient.utils.server.ServerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {
    @Unique
    private Entity entity;

    @Shadow
    @Final
    private Font font;

    @Inject(method = "extractRenderState", at = @At(value = "TAIL"))
    private void getEntity(T entity, S state, float partialTicks, CallbackInfo ci) {
        this.entity = entity;
    }

    @Inject(
        method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
            shift = At.Shift.AFTER
        )
    )
    private void onRenderLevelHead(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, int offset, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        if (!ServerUtils.isJoin(Server.HYPIXEL)) return;
        if (!HypixelMod.getInstance().isEnabled() || !HypixelMod.getInstance().getLevelHeadSetting().isEnabled()) return;
        if (state.entityType != EntityType.PLAYER) return;
        if (state.nameTag == null) return;

        AbstractClientPlayer player = (AbstractClientPlayer) entity;
        if (player == null) return;

        String levelText = ChatFormatting.AQUA + "Level: " + ChatFormatting.YELLOW +
            PupperClient.getInstance().getHypixelManager()
                .getByUuid(player.getUUID().toString().replace("-", ""))
                .getNetworkLevel();

        float x = -font.width(levelText) / 2.0F;
        float y = -10.0F;
        int backgroundColor = (int) (client.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        Matrix4f matrix = poseStack.last().pose();
        int light = state.lightCoords;

        if (submitNodeCollector instanceof MultiBufferSource bufferSource) {
            font.drawInBatch(
                Component.literal(levelText),
                x, y,
                CommonColors.WHITE,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                backgroundColor,
                light
            );
        } else {
            PupperLogger.warn("MixinEntityRenderer", "submitNodeCollector cast MultiBufferSource error");
        }
    }
}
