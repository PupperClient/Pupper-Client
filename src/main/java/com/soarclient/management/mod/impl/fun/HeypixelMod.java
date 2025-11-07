package com.soarclient.management.mod.impl.fun;

import com.soarclient.event.Event;
import com.soarclient.event.EventBus;
import com.soarclient.event.EventListener;
import com.soarclient.event.mod.AutoAgainEvent;
import com.soarclient.event.server.ChatEvent;
import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.management.mod.settings.impl.BooleanSetting;
import com.soarclient.skia.font.Icon;
import com.soarclient.utils.ChatUtils;
import net.minecraft.client.MinecraftClient;

public class HeypixelMod extends Mod {
    private static HeypixelMod instance;
    public static final String HEYPIXEL_END_MESSAGE = "可以用 /hub 退出观察者模式并返回大厅";

    public final BooleanSetting AutoAgain = new BooleanSetting(
        "mod.heypixel.autoAgain.name",
        "mod.heypixel.autoAgain.description",
        Icon.PLAY_ARROW,
        this,
        true
    );

    public final BooleanSetting Logging = new BooleanSetting(
        "mod.heypixel.logging.name",
        "mod.heypixel.logging.description",
        Icon.PLAY_ARROW,
        this,
        false
    );

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public HeypixelMod() {
        super("mod.heypixel.name", "mod.heypixel.desc", Icon.PLAY_ARROW, ModCategory.FUN);
    }

    @EventListener
    public void onChatMessage(ChatEvent event) {
        if (Logging.isEnabled()) {
            ChatUtils.addChatMessage("check the message: " + event.getMessage());
        }
        if (event.getMessage().contains(HEYPIXEL_END_MESSAGE) && AutoAgain.isEnabled()) {
            if (Logging.isEnabled()) {
                ChatUtils.addChatMessage("checked! try to send /again command");
            }
            EventBus.getInstance().post(new AutoAgainEvent());
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.networkHandler.sendChatCommand("again");
                if (Logging.isEnabled()) {
                    ChatUtils.addChatMessage("Done");
                }
            }
        }
    }

    public static HeypixelMod getInstance() {
        return instance;
    }
}
