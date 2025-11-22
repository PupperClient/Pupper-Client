package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Scoreboard extends SimpleHUDMod {

    private static Scoreboard instance;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private String displayText = "";

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
        // 更新显示文本
        updateDisplayText();
        super.draw();
    }

    private void updateDisplayText() {
        ScoreboardObjective scoreboardObjective = client.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (scoreboardObjective == null) {
            displayText = "";
            return;
        }

        net.minecraft.scoreboard.Scoreboard scoreboard = scoreboardObjective.getScoreboard();
        Collection<ScoreboardEntry> scores = scoreboard.getScoreboardEntries(scoreboardObjective);
        if (scores.isEmpty()) {
            displayText = "";
            return;
        }

        // 过滤和排序
        List<ScoreboardEntry> sortedScores = new ArrayList<>();
        for (ScoreboardEntry entry : scores) {
            if (!entry.hidden()) {
                sortedScores.add(entry);
            }
        }
        sortedScores.sort((a, b) -> Integer.compare(b.value(), a.value()));

        // 构建显示文本
        StringBuilder textBuilder = new StringBuilder();

        // 添加标题（居中显示）
        String title = scoreboardObjective.getDisplayName().getString();
        textBuilder.append(title).append("\n");

        // 添加分隔线
        textBuilder.append("---").append("\n");

        // 添加积分项
        for (int i = 0; i < Math.min(sortedScores.size(), 199); i++) { // 限制最多199行
            ScoreboardEntry entry = sortedScores.get(i);
            String line = getFormattedScoreText(entry, scoreboard);
            textBuilder.append(line);

            if (i < Math.min(sortedScores.size(), 10) - 1) {
                textBuilder.append("\n");
            }
        }

        displayText = textBuilder.toString();
    }

    private String getFormattedScoreText(ScoreboardEntry entry, net.minecraft.scoreboard.Scoreboard scoreboard) {
        String playerName = entry.owner();
        Team team = scoreboard.getScoreHolderTeam(playerName);

        String displayName;
        if (team != null) {
            // 使用团队的格式化名称
            Text formattedName = entry.name();
            displayName = formattedName.getString();
        } else {
            displayName = playerName;
        }

        // 返回格式化的文本：玩家名 + 空格 + 分数
        return displayName + " " + entry.value();
    }

    @Override
    public String getText() {
        return displayText;
    }

    @Override
    public String getIcon() {
        return Icon.SCOREBOARD;
    }

    public static Scoreboard getInstance() {
        return instance;
    }
}
