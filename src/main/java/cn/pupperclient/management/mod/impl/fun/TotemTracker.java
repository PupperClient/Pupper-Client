package cn.pupperclient.management.mod.impl.fun;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.ReceivePacketEvent;
import cn.pupperclient.event.client.TotemEvent;
import cn.pupperclient.management.mod.Mod;
import cn.pupperclient.management.mod.ModCategory;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.utils.ChatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.HashMap;

public class TotemTracker extends Mod {
    private static TotemTracker instance;
    public final HashMap<String, Integer> popContainer = new HashMap<>();

    public TotemTracker() {
        super("mod.totemtracker.name", "mod.totemtracker.description", Icon.SECURITY, ModCategory.FUN);
        instance = this;
    }

    public static TotemTracker getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTotem(TotemEvent event) {
        PlayerEntity player = event.getPlayer();
        int l_Count = 1;
        if (popContainer.containsKey(player.getName().getString())) {
            l_Count = popContainer.get(player.getName().getString());
        }
        if (l_Count == 1) {
            if (player.equals(mc.player)) {
                ChatUtils.addChatMessage(String.format("¡ìfYou(%s)¡ìr popped ¡ìf%d¡ìr totem.", player.getName().getString(), l_Count));
            } else {
                ChatUtils.addChatMessage(String.format("¡ìf%s¡ìr popped ¡ìf%d¡ìr totem.", player.getName().getString(), l_Count));
            }
        } else {
            if (player.equals(mc.player)) {
                ChatUtils.addChatMessage(String.format("¡ìfYou(%s)¡ìr popped ¡ìf%d¡ìr totem.", player.getName().getString(), l_Count));
            } else {
                ChatUtils.addChatMessage(String.format("¡ìf%s¡ìr has popped ¡ìf%d¡ìr totems.", player.getName().getString(), l_Count));
            }
        }
    }

    @EventListener(priority = 1001)
    public void onPacketReceive(ReceivePacketEvent event) {
        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity entity = packet.getEntity(mc.world);
                if(entity instanceof PlayerEntity player) {
                    onTotemPop(player);
                }
            }
        }
    }

    public void onTotemPop(PlayerEntity player) {
        int l_Count = 1;
        if (popContainer.containsKey(player.getName().getString())) {
            l_Count = popContainer.get(player.getName().getString());
            popContainer.put(player.getName().getString(), ++l_Count);
        } else {
            popContainer.put(player.getName().getString(), l_Count);
        }
        EventBus.getInstance().post(new TotemEvent(player));
    }
}
