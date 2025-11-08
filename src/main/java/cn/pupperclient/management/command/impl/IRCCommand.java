package cn.pupperclient.management.command.impl;

import cn.pupperclient.PupperClient;
import cn.pupperclient.management.mod.impl.misc.IRCChatMod;
import cn.pupperclient.utils.IMinecraft;
import net.minecraft.text.Text;

public class IRCCommand implements IMinecraft {

    public static void handleCommand(String[] args) {
        if (args.length == 1) {
            sendHelp();
            return;
        }

        IRCChatMod ircMod = (IRCChatMod) PupperClient.getInstance().getModManager().getModByName("mod.irc.name");

        if (ircMod == null) {
            sendMessage("§cIRC Chat mod not found!");
            return;
        } else if (!ircMod.isEnabled()) {
            sendMessage("§cIRC Chat mod is not enabled!");
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "connect":
                ircMod.connectToIRC();
                sendMessage("§6Connecting to IRC server...");
                break;

            case "disconnect":
                ircMod.disconnectFromIRC();
                break;

            case "status":
                sendMessage("§6IRC Status: " + ircMod.getConnectionStatus());
                sendMessage("§6Server: " + ircMod.getServerInfo());
                break;

            case "help":
                sendHelp();
                break;

            default:
                // 如果没有子命令，则发送消息到IRC
                String message = args[1];
                ircMod.sendIRCMessage(message);
                break;
        }

    }

    private static void sendHelp() {
        sendMessage("§6=== IRC Commands ===");
        sendMessage("§b.irc <message>§f - Send message to IRC");
        sendMessage("§b.irc connect§f - Connect to IRC server");
        sendMessage("§b.irc disconnect§f - Disconnect from IRC");
        sendMessage("§b.irc status§f - Show connection status");
        sendMessage("§b.irc help§f - Show this help");
        sendMessage("§7Configure IRC settings in Mod Menu -> Misc -> IRC Chat");
    }

    private static void sendMessage(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.of(message), false);
        }
    }
}
