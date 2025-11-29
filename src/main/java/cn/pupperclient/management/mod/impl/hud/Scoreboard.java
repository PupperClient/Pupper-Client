package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleListHUDMod;
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

public class Scoreboard extends SimpleListHUDMod {

    private static Scoreboard instance;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private List<String> displayLines = new ArrayList<>(); // 改为存储行的列表

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
        updateDisplayLines(); // 更新行列表
        super.draw(); // 调用父类的绘制方法
    }

    private void updateDisplayLines() {
        displayLines.clear(); // 清空之前的行

        ScoreboardObjective scoreboardObjective = client.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (scoreboardObjective == null) {
            return;
        }

        net.minecraft.scoreboard.Scoreboard scoreboard = scoreboardObjective.getScoreboard();
        Collection<ScoreboardEntry> scores = scoreboard.getScoreboardEntries(scoreboardObjective);
        if (scores.isEmpty()) {
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

        // 添加标题
        String title = scoreboardObjective.getDisplayName().getString();
        displayLines.add(title);

        // 添加分隔线
        displayLines.add("---");

        // 添加积分项
        int maxLines = Math.min(sortedScores.size(), 199);
        for (int i = 0; i < maxLines; i++) {
            ScoreboardEntry entry = sortedScores.get(i);
            String line = getFormattedScoreText(entry, scoreboard);
            displayLines.add(line);
        }
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
    public List<String> getText() {
        return displayLines; // 直接返回行列表
    }

    @Override
    public String getIcon() {
        return Icon.SCOREBOARD;
    }

    public static Scoreboard getInstance() {
        return instance;
    }
}
