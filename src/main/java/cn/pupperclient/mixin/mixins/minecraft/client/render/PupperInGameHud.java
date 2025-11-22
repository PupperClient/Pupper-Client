package cn.pupperclient.mixin.mixins.minecraft.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class PupperInGameHud {
    @Shadow @Final private MinecraftClient client;

    @Shadow
    private PlayerEntity getCameraPlayer() {
        Entity var2 = this.client.getCameraEntity();
        PlayerEntity var10000;
        if (var2 instanceof PlayerEntity playerEntity) {
            var10000 = playerEntity;
        } else {
            var10000 = null;
        }
        return var10000;
    }

    @Unique private long lastHotbarUpdate = 0;
    @Unique private int lastSelectedSlot = 0;
    @Unique private float slotAnimationProgress = 0f;
    @Unique private float offhandAlpha = 0f;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void replaceHotbarRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ci.cancel(); // 取消原版物品栏绘制
        drawCustomHotbar(context, tickCounter);
    }

    @Unique
    private void drawCustomHotbar(DrawContext context, RenderTickCounter tickCounter) {
        PlayerEntity playerEntity = this.getCameraPlayer();
        if (playerEntity == null) return;

        ItemStack itemStack = playerEntity.getOffHandStack();
        Arm arm = playerEntity.getMainArm().getOpposite();
        int centerX = context.getScaledWindowWidth() / 2;
        int hotbarY = context.getScaledWindowHeight() - 22;

        int selectedSlot = playerEntity.getInventory().selectedSlot;
        updateSlotAnimation(selectedSlot);
        updateOffhandAlpha(playerEntity);

        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, -90.0F);

        // 绘制自定义白色背景
        drawCustomHotbarBackground(context, centerX, hotbarY, selectedSlot);

        // 绘制原版选择框
        context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_SELECTION_TEXTURE,
            centerX - 91 - 1 + selectedSlot * 20, hotbarY - 1, 24, 23);

        // 绘制副手背景（带透明度）
        if (!itemStack.isEmpty() || offhandAlpha > 0) {
            drawOffhandBackground(context, centerX, hotbarY, arm, offhandAlpha);
        }

        context.getMatrices().pop();

        // 绘制物品（带动画）
        for(int i = 0; i < 9; ++i) {
            int slotX = centerX - 90 + i * 20 + 2;
            int slotY = context.getScaledWindowHeight() - 16 - 3;

            // 计算动画偏移
            float slotOffset = 0;
            if (i == lastSelectedSlot) {
                slotOffset = (1 - slotAnimationProgress) * 3;
            } else if (i == selectedSlot) {
                slotOffset = slotAnimationProgress * 3;
            }

            this.renderHotbarItem(context, slotX, (int)(slotY + slotOffset), tickCounter, playerEntity,
                playerEntity.getInventory().main.get(i), i + 1);
        }

        // 绘制副手物品（带透明度）
        if (!itemStack.isEmpty() && offhandAlpha > 0) {
            int offhandY = context.getScaledWindowHeight() - 16 - 3;
            if (arm == Arm.LEFT) {
                this.renderHotbarItem(context, centerX - 91 - 26, offhandY, tickCounter, playerEntity, itemStack, 100);
            } else {
                this.renderHotbarItem(context, centerX + 91 + 10, offhandY, tickCounter, playerEntity, itemStack, 100);
            }
        }

        // 保留原版攻击冷却指示器
        if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
            float f = this.client.player.getAttackCooldownProgress(0.0F);
            if (f < 1.0F) {
                int indicatorY = context.getScaledWindowHeight() - 20;
                int indicatorX = centerX + 91 + 6;
                if (arm == Arm.RIGHT) {
                    indicatorX = centerX - 91 - 22;
                }

                int progress = (int)(f * 19.0F);
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE,
                    indicatorX, indicatorY, 18, 18);
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE,
                    18, 18, 0, 18 - progress, indicatorX, indicatorY + 18 - progress, 18, progress);
            }
        }
    }

    @Unique
    private void drawCustomHotbarBackground(DrawContext context, int centerX, int hotbarY, int selectedSlot) {
        int hotbarWidth = 182;
        int hotbarX = centerX - 91;

        // 绘制主背景（白色半透明）
        for(int i = 0; i < 9; ++i) {
            int slotX = hotbarX + i * 20;

            // 计算动画偏移
            float slotOffset = 0;
            if (i == lastSelectedSlot) {
                slotOffset = (1 - slotAnimationProgress) * 3;
            } else if (i == selectedSlot) {
                slotOffset = slotAnimationProgress * 3;
            }

            // 槽位背景颜色
            int color = (i == selectedSlot) ?
                0x80FFFFFF : // 选中槽位更亮
                0x40FFFFFF;  // 普通槽位

            // 绘制槽位背景
            context.fill(slotX + 1, (int)(hotbarY + 1 + slotOffset),
                slotX + 19, (int)(hotbarY + 19 + slotOffset), color);

            // 绘制选中指示器
            if (i == selectedSlot) {
                context.fill(slotX, (int)(hotbarY - 1 + slotOffset),
                    slotX + 20, (int)(hotbarY + 1 + slotOffset), 0xE0FFFFFF);
            }
        }
    }

    @Unique
    private void drawOffhandBackground(DrawContext context, int centerX, int hotbarY, Arm arm, float alpha) {
        int color = (int)(alpha * 64) << 24 | 0xFFFFFF; // 白色带透明度

        if (arm == Arm.LEFT) {
            // 左侧副手背景
            context.fill(centerX - 91 - 29, hotbarY - 1,
                centerX - 91, hotbarY + 23, color);
        } else {
            // 右侧副手背景
            context.fill(centerX + 91, hotbarY - 1,
                centerX + 91 + 29, hotbarY + 23, color);
        }
    }

    @Unique
    private void updateSlotAnimation(int currentSlot) {
        long currentTime = System.currentTimeMillis();

        if (currentSlot != lastSelectedSlot) {
            lastSelectedSlot = currentSlot;
            lastHotbarUpdate = currentTime;
            slotAnimationProgress = 0f;
        }

        if (slotAnimationProgress < 1f) {
            long elapsed = currentTime - lastHotbarUpdate;
            slotAnimationProgress = Math.min(elapsed / 200f, 1f);
            slotAnimationProgress = easeOutCubic(slotAnimationProgress);
        }
    }

    @Unique
    private void updateOffhandAlpha(PlayerEntity player) {
        ItemStack offhandStack = player.getOffHandStack();

        if (!offhandStack.isEmpty()) {
            offhandAlpha = Math.min(offhandAlpha + 0.1f, 1f);
        } else {
            offhandAlpha = Math.max(offhandAlpha - 0.05f, 0f);
        }
    }

    @Unique
    private float easeOutCubic(float x) {
        return (float) (1 - Math.pow(1 - x, 3));
    }

    @Shadow
    private void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed) {
    }
}
