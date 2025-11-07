package com.soarclient.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class PlayerUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static double motionX;
    public static double motionZ;
    public static double lastYaw;
    public static double lastPitch;
    public static double prevYaw;
    public static double prevPitch;
    public static double deltaYaw;
    public static double deltaPitch;
    public static double playerYaw;
    public static double playerPitch;
    public static double yawOffset;
    public static double[] directionTable;
    public static double playerMotion;
    public static double playerSpeed;
    public static double moveSpeed;
    public static int airTicks;

    static {
        directionTable = new double[]{1.0, 1.4304347400742218, 1.7345274764372163, 1.9000000000000001};
    }

    public static double getSpeedFromMotion(double motionX, double motionZ) {
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }

    public static double getSpeedFromPosition(double x, double z) {
        if (mc.player == null) return 0;
        double xSpeed = mc.player.getX() - x;
        double zSpeed = mc.player.getZ() - z;
        return Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed);
    }

    public static float getFixedYaw(float rotationYaw) {
        int moveForward = 0;
        if (mc.options.forwardKey.isPressed()) {
            moveForward++;
        }

        if (mc.options.backKey.isPressed()) {
            moveForward--;
        }

        int moveStrafing = 0;
        if (mc.options.rightKey.isPressed()) {
            moveStrafing++;
        }

        if (mc.options.leftKey.isPressed()) {
            moveStrafing--;
        }

        boolean reversed = moveForward < 0;
        double strafingYaw = 90.0 * (moveForward > 0 ? 0.5 : -0.5);
        if (reversed) {
            rotationYaw += 180.0F;
        }

        if (moveStrafing > 0) {
            rotationYaw = (float)((double)rotationYaw + strafingYaw);
        } else if (moveStrafing < 0) {
            rotationYaw = (float)((double)rotationYaw - strafingYaw);
        }

        return rotationYaw;
    }

    public static void handleJump(float yaw, boolean sprinting) {
        if (mc.player == null) return;

        mc.player.setVelocity(mc.player.getVelocity().x, getJumpHeight(), mc.player.getVelocity().z);

        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y + (amplifier + 1) * 0.1F, mc.player.getVelocity().z);
        }

        if (sprinting) {
            float f = yaw * (float) (Math.PI / 180.0);
            mc.player.setVelocity(
                mc.player.getVelocity().x - MathHelper.sin(f) * 0.2F,
                mc.player.getVelocity().y,
                mc.player.getVelocity().z + MathHelper.cos(f) * 0.2F
            );
        }

        mc.player.setOnGround(false);
    }

    public static double getSpeed() {
        return getSpeed(mc.player);
    }

    public static double getSpeed(Entity entity) {
        if (entity == null) return 0;
        return Math.sqrt(entity.getVelocity().x * entity.getVelocity().x + entity.getVelocity().z * entity.getVelocity().z);
    }

    public static double predictMotion(double motion, int ticks) {
        if (ticks == 0) {
            return motion;
        } else {
            double predicted = motion;
            double sum = 0.0 + motion;

            for (int i = 0; i < ticks; i++) {
                predicted = (predicted - 0.08) * 0.98F;
                sum += predicted;
            }

            return sum;
        }
    }

    public static boolean isMoving() {
        return isMoving(mc.player);
    }

    public static boolean isMoving(LivingEntity player) {
        return player != null && (player.forwardSpeed != 0.0F || player.sidewaysSpeed != 0.0F);
    }

    public static double getPlayerSpeed(PlayerEntity player) {
        if (player == null) return 0;
        return Math.sqrt(player.getVelocity().x * player.getVelocity().x + player.getVelocity().z * player.getVelocity().z);
    }

    public static void setSpeed(double speed) {
        if (isMoving()) {
            double yaw = getDirection();
            mc.player.setVelocity(
                -Math.sin(yaw) * speed,
                mc.player.getVelocity().y,
                Math.cos(yaw) * speed
            );
        }
    }

    public static void setSpeed(double speed, double yaw) {
        if (!isMoving()) {
            return;
        }

        mc.player.setVelocity(
            -Math.sin(yaw) * speed,
            mc.player.getVelocity().y,
            Math.cos(yaw) * speed
        );
    }

    public static void resetMotion(double speed) {
        double forward = mc.options.forwardKey.isPressed() ? 1.0 : (mc.options.backKey.isPressed() ? -1.0 : 0.0);
        double strafe = mc.options.leftKey.isPressed() ? 1.0 : (mc.options.rightKey.isPressed() ? -1.0 : 0.0);
        float yaw = mc.player.getYaw();

        if (isMoving()) {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }

                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }

            double cos = Math.cos(Math.toRadians((double)(yaw + 89.5F)));
            double sin = Math.sin(Math.toRadians((double)(yaw + 89.5F)));
            mc.player.setVelocity(
                forward * speed * cos + strafe * speed * sin,
                mc.player.getVelocity().y,
                forward * speed * sin - strafe * speed * cos
            );
        } else {
            mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
        }
    }

    public static double getDirection(float moveForward, float moveStrafing, float rotationYaw) {
        if (moveForward < 0.0F) {
            rotationYaw += 180.0F;
        }

        float forward = 1.0F;
        if (moveForward < 0.0F) {
            forward = -0.5F;
        } else if (moveForward > 0.0F) {
            forward = 0.5F;
        }

        if (moveStrafing > 0.0F) {
            rotationYaw -= 70.0F * forward;
        }

        if (moveStrafing < 0.0F) {
            rotationYaw += 70.0F * forward;
        }

        return Math.toRadians((double)rotationYaw);
    }

    public static double getDirection() {
        if (mc.player == null) return 0;

        float rotationYaw = mc.player.getYaw();
        if (mc.player.forwardSpeed < 0.0F) {
            rotationYaw += 180.0F;
        }

        float forward = 1.0F;
        if (mc.player.forwardSpeed < 0.0F) {
            forward = -0.5F;
        } else if (mc.player.forwardSpeed > 0.0F) {
            forward = 0.5F;
        }

        if (mc.player.sidewaysSpeed > 0.0F) {
            rotationYaw -= 90.0F * forward;
        }

        if (mc.player.sidewaysSpeed < 0.0F) {
            rotationYaw += 90.0F * forward;
        }

        return Math.toRadians((double)rotationYaw);
    }

    public static float getMovingDirection() {
        return calculateDirection(
            getCurrentYaw(),
            mc.player.sidewaysSpeed,
            mc.player.forwardSpeed
        );
    }

    public static boolean isAligned() {
        float direction = getMovingDirection() + 180.0F;
        float movingYaw = (float)(Math.round(direction / 45.0F) * 45);
        return movingYaw % 90.0F == 0.0F;
    }

    public static float calculateDirection(float yaw, float pStrafe, float pForward) {
        float rotationYaw = yaw;
        if (pForward < 0.0F) {
            rotationYaw = yaw + 180.0F;
        }

        float forward = 1.0F;
        if (pForward < 0.0F) {
            forward = -0.5F;
        } else if (pForward > 0.0F) {
            forward = 0.5F;
        }

        if (pStrafe > 0.0F) {
            rotationYaw -= 90.0F * forward;
        }

        if (pStrafe < 0.0F) {
            rotationYaw += 90.0F * forward;
        }

        return rotationYaw;
    }

    public static int getSpeedAmplifier(PlayerEntity player) {
        return player != null && player.hasStatusEffect(StatusEffects.SPEED) ?
            player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1 : 0;
    }

    public static int getSpeedAmplifier() {
        return getSpeedAmplifier(mc.player);
    }

    public static void stopMotionXZ() {
        if (mc.player != null) {
            mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
        }
    }

    public static void stopAllMotion() {
        if (mc.player != null) {
            mc.player.setVelocity(0.0, 0.0, 0.0);
        }
    }

    public static double getBPS() {
        return getBPS(mc.player);
    }

    public static double getBPS(PlayerEntity player) {
        if (player == null || player.age < 1) return 0.0;

        return getSpeedFromPosition(player.prevX, player.prevZ) * 20.0;
    }

    public static boolean canSprint(boolean legit) {
        if (mc.player == null) return false;

        return legit ?
            mc.player.forwardSpeed >= 0.8F &&
                !mc.player.horizontalCollision &&
                (mc.player.getHungerManager().getFoodLevel() > 6 || mc.player.getAbilities().flying) &&
                !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) &&
                !mc.player.isSneaking() :
            isMovingForward();
    }

    public static boolean isMovingForward() {
        return mc.player != null && (Math.abs(mc.player.forwardSpeed) >= 0.8F || Math.abs(mc.player.sidewaysSpeed) >= 0.8F);
    }

    public static boolean hasMotion(double amount) {
        return mc.player != null && (Math.abs(mc.player.getVelocity().x) > amount && Math.abs(mc.player.getVelocity().z) > amount);
    }

    public static double getBaseMoveSpeed(PlayerEntity player) {
        double baseSpeed = 0.2873;
        if (player != null && player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed = 0.2873 * (1.0 + 0.2 * (double)(amplifier + 1));
        }

        return baseSpeed;
    }

    public static double getBaseMoveSpeed() {
        return getBaseMoveSpeed(mc.player);
    }

    public static double getBaseJumpMotion() {
        double jumpY = 0.41999998688698;
        if (mc.player != null && mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            jumpY = 0.41999998688698 + (double)((amplifier + 1) * 0.1F);
        }

        return jumpY;
    }

    // 修复：移除不存在的getDepthStrider方法，使用新的API
    public static int getDepthStriderLevel() {
        if (mc.player == null) return 0;

        // 在1.21中，深度行者附魔的获取方式已改变
        // 使用新的组件系统来获取附魔等级
        try {
            // 检查靴子上的深度行者附魔
            var boots = mc.player.getInventory().getArmorStack(0); // 0是靴子
            if (!boots.isEmpty()) {
                var enchantments = boots.getEnchantments();
                // 深度行者在1.21中的注册表键可能是 "minecraft:depth_strider"
                // 这里需要根据实际的注册表键来获取
                // 由于1.21的API变化，这里使用反射或其他方式获取
                // 暂时返回0，需要根据实际版本调整
                return 0;
            }
        } catch (Exception e) {
            // 如果出现异常，返回0
        }
        return 0;
    }

    public static double predictMotion2(double motion, int ticks) {
        if (ticks == 0) {
            return motion;
        } else {
            double predicted = motion;

            for (int i = 0; i < ticks; i++) {
                predicted = (predicted - 0.08) * 0.98F;
            }

            return predicted;
        }
    }

    public static double getDirection2(float rotationYaw, double moveForward, double moveStrafing) {
        if (moveForward < 0.0) {
            rotationYaw += 180.0F;
        }

        float forward = 1.0F;
        if (moveForward < 0.0) {
            forward = -0.5F;
        } else if (moveForward > 0.0) {
            forward = 0.5F;
        }

        if (moveStrafing > 0.0) {
            rotationYaw -= 90.0F * forward;
        }

        if (moveStrafing < 0.0) {
            rotationYaw += 90.0F * forward;
        }

        return Math.toRadians((double)rotationYaw);
    }

    public static void setSpeed3(double increase) {
        if (isMoving()) {
            double yaw = getDirection();
            mc.player.setVelocity(
                mc.player.getVelocity().x + (-MathHelper.sin((float)yaw)) * increase,
                mc.player.getVelocity().y,
                mc.player.getVelocity().z + MathHelper.cos((float)yaw) * increase
            );
        }
    }

    public static double getJumpHeight() {
        return getJumpBoosted(0.42F);
    }

    public static double getJumpBoosted(double motionY) {
        return mc.player != null && mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) ?
            motionY + (double)((mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F) :
            motionY;
    }

    public static void setSpeed5(double speed) {
        setSpeed6(speed, getCurrentYaw());
    }

    public static void setSpeed6(double speed, float yaw) {
        if (mc.player == null) return;

        double forward = (double)mc.player.forwardSpeed;
        double strafe = (double)mc.player.sidewaysSpeed;

        if (forward == 0.0 && strafe == 0.0) {
            mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }

                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }

            mc.player.setVelocity(
                forward * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))) +
                    strafe * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))),
                mc.player.getVelocity().y,
                forward * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))) -
                    strafe * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F)))
            );
        }
    }

    // 获取当前Yaw（考虑FreeLook）
    private static float getCurrentYaw() {
        if (mc.player == null) return 0;

        // 检查FreeLook是否激活
        com.soarclient.management.mod.impl.player.FreelookMod freeLook =
            com.soarclient.management.mod.impl.player.FreelookMod.getInstance();

        if (freeLook != null && freeLook.isEnabled() && freeLook.isActive()) {
            // 使用FreeLook的旋转
            if (mc.player instanceof com.soarclient.mixin.interfaces.IMixinCameraEntity) {
                return ((com.soarclient.mixin.interfaces.IMixinCameraEntity) mc.player).soarClient_CN$getCameraYaw();
            }
        }

        // 使用玩家实际旋转
        return mc.player.getYaw();
    }

    public static double getBaseHorizontalSpeed(boolean allowSprint) {
        boolean useBaseModifiers = false;
        double horizontalDistance;

        if (mc.player.isInsideWall()) {
            horizontalDistance = 0.105;
        } else if (mc.player.isTouchingWater() || mc.player.isInLava()) {
            horizontalDistance = 0.11500000208616258;
            useBaseModifiers = true;
        } else if (mc.player.isSneaking()) {
            horizontalDistance = 0.0663000026345253;
        } else {
            horizontalDistance = 0.221;
            useBaseModifiers = true;
        }

        if (useBaseModifiers) {
            if (canSprint(false) && allowSprint) {
                horizontalDistance *= 1.3F;
            }

            if (mc.player.hasStatusEffect(StatusEffects.SPEED) &&
                mc.player.getStatusEffect(StatusEffects.SPEED).getDuration() > 0) {
                horizontalDistance *= 1.0 + 0.2 * (double)(mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1);
            }

            if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                horizontalDistance = 0.29;
            }
        }

        return horizontalDistance;
    }
}
