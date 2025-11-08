package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class FallDamageHelp extends SimpleHUDMod {
    public FallDamageHelp() {
        super("mod.fallDamageHelp.name", "mod.fallDamageHelp.description", Icon.FALLING);
    }

    public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = e -> {
        this.draw();
    };

    public static boolean willFallToDeath() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return false;
        }

        World world = player.getEntityWorld();
        double currentY = player.getY();
        BlockPos playerBlockPos = player.getBlockPos();

        for (int offset = 1; offset < 256; offset++) {
            int y = playerBlockPos.getY() - offset;
            if (y < world.getBottomY()) {
                break;
            }

            BlockPos checkPos = new BlockPos(playerBlockPos.getX(), y, playerBlockPos.getZ());
            BlockState state = world.getBlockState(checkPos);
            VoxelShape collisionShape = state.getCollisionShape(world, checkPos);

            if (!collisionShape.isEmpty() && collisionShape != VoxelShapes.empty()) {
                double blockTopY = y + collisionShape.getMax(Direction.Axis.Y);
                double fallDistance = currentY - blockTopY;
                return fallDistance >= 23;
            }
        }

        double fallDistance = currentY - world.getBottomY();
        return fallDistance >= 23;
    }

    @Override
    public String getText() {
        return willFallToDeath() ? "is falling to death" : "is not falling to death";
    }

    @Override
    public String getIcon() {
        return Icon.FALLING;
    }
}
