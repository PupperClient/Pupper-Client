package com.soarclient.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatUtils {
    private static final String PREFIX = "§7[§bSoar§7] ";
    private static final String PREFIX_FORMATTED = Formatting.GRAY + "[" + Formatting.AQUA + "Soar" + Formatting.GRAY + "] ";
    private static final Text PREFIX_TEXT = Text.literal("[")
        .formatted(Formatting.GRAY)
        .append(Text.literal("Soar").formatted(Formatting.AQUA))
        .append(Text.literal("] ").formatted(Formatting.GRAY));


    public static void component(Text component) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        client.execute(() -> {
            ChatHud chat = client.inGameHud.getChatHud();
            if (chat != null) {
                chat.addMessage(component);
            }
        });
    }

    public static void addChatMessage(String message) {
        addChatMessage(true, message);
    }

    public static void addChatMessage(Text message) {
        if (message == null || message.getString().isEmpty()) return;

        MutableText fullMessage = Text.empty()
            .append(PREFIX_TEXT)
            .append(message);

        component(fullMessage);
    }

    public static void addChatMessage(boolean prefix, String message) {
        if (message == null || message.isEmpty()) return;

        String formattedMessage = (prefix ? PREFIX : "") + message;
        component(Text.literal(formattedMessage));
    }

    public static void addFormattedMessage(String message, Formatting... formattings) {
        MutableText text = Text.literal(PREFIX_FORMATTED).formatted(Formatting.GRAY);
        text.append(Text.literal(message).formatted(formattings));
        component(text);
    }

    public static void error(String message) {
        addFormattedMessage(message, Formatting.RED);
    }

    public static void success(String message) {
        addFormattedMessage(message, Formatting.GREEN);
    }

    public static void warning(String message) {
        addFormattedMessage(message, Formatting.YELLOW);
    }

    public static void info(String message) {
        addFormattedMessage(message, Formatting.BLUE);
    }

}
