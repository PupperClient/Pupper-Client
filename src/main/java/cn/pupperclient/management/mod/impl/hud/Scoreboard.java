package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.skia.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleListHUDMod;
import cn.pupperclient.skia.font.Icon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;

public class Scoreboard extends SimpleListHUDMod {

    private static Scoreboard instance;
    private final Minecraft client = Minecraft.getInstance();
    private List<String> displayLines = new ArrayList<>();

    public Scoreboard() {
        super("mod.scoreboard.name", "mod.scoreboard.description", Icon.SCOREBOARD);
        instance = this;
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
    public void onRender(RenderSkiaEvent event) {
        this.draw();
    }

    @Override
    public void draw() {
        updateDisplayLines();
        super.draw();
    }

    private void updateDisplayLines() {
        displayLines.clear();

        Objective scoreboardObjective = client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        if (scoreboardObjective == null) {
            return;
        }

        net.minecraft.world.scores.Scoreboard scoreboard = scoreboardObjective.getScoreboard();
        Collection<PlayerScoreEntry> scores = scoreboard.listPlayerScores(scoreboardObjective);
        if (scores.isEmpty()) {
            return;
        }

        // 添加标题 - 保留格式化字符
        Component titleText = scoreboardObjective.getDisplayName();
        String formattedTitle = convertTextToFormattedString(titleText);
        displayLines.add(formattedTitle);

        // 过滤和排序
        List<PlayerScoreEntry> sortedScores = new ArrayList<>();
        for (PlayerScoreEntry entry : scores) {
            if (!entry.isHidden()) {
                sortedScores.add(entry);
            }
        }
        sortedScores.sort((a, b) -> Integer.compare(b.value(), a.value()));

        // 添加积分项
        int maxLines = Math.min(sortedScores.size(), 199);
        for (int i = 0; i < maxLines; i++) {
            PlayerScoreEntry entry = sortedScores.get(i);
            String line = getFormattedScoreText(entry, scoreboard);
            displayLines.add(line);
        }
    }

    private String getFormattedScoreText(PlayerScoreEntry entry, net.minecraft.world.scores.Scoreboard scoreboard) {
        String playerName = entry.owner();
        PlayerTeam team = scoreboard.getPlayersTeam(playerName);

        String displayName;
        if (team != null) {
            Component formattedName = entry.ownerName();
            // 使用新的格式化方法
            displayName = convertTextToFormattedString(formattedName);
        } else {
            displayName = playerName;
        }

        return displayName + " " + entry.value();
    }

    /**
     * 将 Minecraft Text 对象转换为包含格式化字符的字符串
     * 保留颜色代码和格式化信息
     */
    private String convertTextToFormattedString(Component text) {
        StringBuilder result = new StringBuilder();
        convertTextRecursive(text, result);
        return result.toString();
    }

    /**
     * 递归处理 Text 对象及其子组件
     */
    private void convertTextRecursive(Component text, StringBuilder result) {
        // 处理当前文本的样式
        if (text.getStyle() != null) {
            // 添加颜色代码
            if (text.getStyle().getColor() != null) {
                ChatFormatting formatting = ChatFormatting.getByName(text.getStyle().getColor().serialize());
                if (formatting != null && formatting.isColor()) {
                    result.append("§").append(formatting.getChar());
                }
            }

            // 添加格式化代码（粗体、斜体等）
            if (text.getStyle().isBold()) {
                result.append("§l");
            }
            if (text.getStyle().isItalic()) {
                result.append("§o");
            }
            if (text.getStyle().isUnderlined()) {
                result.append("§n");
            }
            if (text.getStyle().isStrikethrough()) {
                result.append("§m");
            }
            if (text.getStyle().isObfuscated()) {
                result.append("§k");
            }
        }

        // 添加文本内容
        String content = text.getString();
        if (!content.isEmpty()) {
            result.append(content);
        }

        // 递归处理子组件
        for (Component sibling : text.getSiblings()) {
            convertTextRecursive(sibling, result);
        }

        // 在必要时重置格式（当有样式变化时）
        if (text.getStyle() != null && (
            text.getStyle().getColor() != null ||
                text.getStyle().isBold() ||
                text.getStyle().isItalic() ||
                text.getStyle().isUnderlined() ||
                text.getStyle().isStrikethrough() ||
                text.getStyle().isObfuscated())) {
            result.append("§r");
        }
    }

    @Override
    public List<String> getText() {
        return displayLines;
    }

    @Override
    public String getIcon() {
        return Icon.SCOREBOARD;
    }

    public static Scoreboard getInstance() {
        return instance;
    }
}
