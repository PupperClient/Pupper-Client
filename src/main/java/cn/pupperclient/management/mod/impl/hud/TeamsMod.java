package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.management.mod.Mod;
import cn.pupperclient.management.mod.ModCategory;
import cn.pupperclient.management.mod.settings.impl.BooleanSetting;
import cn.pupperclient.management.mod.settings.impl.ColorSetting;
import cn.pupperclient.management.mod.settings.impl.NumberSetting;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.utils.IMinecraft;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class TeamsMod extends Mod implements IMinecraft {
    private static TeamsMod instance;

    // Settings
    private final BooleanSetting showTeamESP = new BooleanSetting("setting.teams.show_team_esp", "setting.teams.show_team_esp.description", Icon.BOX, this, true);
    private final BooleanSetting showTeamNames = new BooleanSetting("setting.teams.show_team_names", "setting.teams.show_team_names.description", Icon.PERSON, this, true);
    private final BooleanSetting highlightInTab = new BooleanSetting("setting.teams.highlight_in_tab", "setting.teams.highlight_in_tab.description", Icon.HIGHLIGHT, this, true);
    private final ColorSetting teamColor = new ColorSetting("setting.teams.team_color", "setting.teams.team_color.description", Icon.COLORS, this, new Color(0, 255, 0, 150), true);
    private final NumberSetting maxDistance = new NumberSetting("setting.teams.max_distance", "setting.teams.max_distance.description", Icon.DISTANCE, this,50, 10, 100, 1);

    // Team detection patterns
    private final List<Pattern> teamPatterns = Arrays.asList(
        Pattern.compile("\\[.*].*"), // [TEAM] PlayerName
        Pattern.compile(".*\\[.*]"), // PlayerName [TEAM]
        Pattern.compile(".*_.*"),      // Team_Player
        Pattern.compile("Team.*"),     // Team1, Team2, etc.
        Pattern.compile(".*Team.*")    // RedTeam, BlueTeam, etc.
    );

    private final Set<String> teammates = new HashSet<>();
    private final Map<String, String> playerTeams = new HashMap<>();

    public TeamsMod() {
        super("mod.teams.name", "mod.teams.description", Icon.TEAM_DASHBOARD, ModCategory.HUD);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        teammates.clear();
        playerTeams.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        teammates.clear();
        playerTeams.clear();
    }

    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        updateTeamMembers();
    }

    private void updateTeamMembers() {
        teammates.clear();
        playerTeams.clear();

        if (mc.world != null && mc.world.getScoreboard() != null && mc.world.getPlayers() != null) {
            detectScoreboardTeams();

            detectPatternTeams();

            detectTabTeams();
        }
    }

    private void detectScoreboardTeams() {
        try {
            // Get player's team
            Team playerTeam = null;
            if (mc.player != null) {
                if (mc.world != null) {
                    playerTeam = mc.world.getScoreboard().getTeam(mc.player.getGameProfile().getName());
                }
            }
            String playerTeamName = playerTeam != null ? playerTeam.getName() : null;

            // Find players in same team
            if (mc.world != null) {
                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (player == mc.player) continue;

                    Team team = mc.world.getScoreboard().getTeam(player.getGameProfile().getName());
                    if (team != null && team.equals(playerTeam)) {
                        String playerName = player.getGameProfile().getName();
                        teammates.add(playerName);
                        playerTeams.put(playerName, team.getName());
                    }
                }
            }
        } catch (Exception e) {
            // Scoreboard might not be available
        }
    }

    private void detectPatternTeams() {
        String playerName = null;
        if (mc.player != null) {
            playerName = mc.player.getGameProfile().getName();
        }

        if (mc.world != null) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;

                String otherPlayerName = player.getGameProfile().getName();

                // Check if names match common team patterns
                for (Pattern pattern : teamPatterns) {
                    if (playerName != null && pattern.matcher(playerName).matches() &&
                        pattern.matcher(otherPlayerName).matches()) {
                        teammates.add(otherPlayerName);
                        playerTeams.put(otherPlayerName, "Pattern: " + pattern.pattern());
                        break;
                    }
                }

                // Check for same prefix/suffix
                if (playerName != null && hasCommonTeamElements(playerName, otherPlayerName)) {
                    teammates.add(otherPlayerName);
                    playerTeams.put(otherPlayerName, "Common Pattern");
                }
            }
        }
    }

    private void detectTabTeams() {
        if (mc.getNetworkHandler() == null) return;

        Collection<PlayerListEntry> playerList = mc.getNetworkHandler().getPlayerList();
        Map<String, List<String>> teamGroups = new HashMap<>();

        for (PlayerListEntry entry : playerList) {
            if (entry.getDisplayName() != null) {
                Text displayName = entry.getDisplayName();
                String formattedName = displayName.getString();

                // Look for team indicators in display name
                String team = extractTeamFromFormattedName(formattedName);
                if (team != null) {
                    teamGroups.computeIfAbsent(team, k -> new ArrayList<>())
                        .add(entry.getProfile().getName());
                }
            }
        }

        // Find which team the player is in
        String playerName = null;
        if (mc.player != null) {
            playerName = mc.player.getGameProfile().getName();
        }
        for (Map.Entry<String, List<String>> entry : teamGroups.entrySet()) {
            if (entry.getValue().contains(playerName)) {
                teammates.addAll(entry.getValue());
                entry.getValue().forEach(name ->
                    playerTeams.put(name, "Tab: " + entry.getKey()));
                break;
            }
        }
    }

    private boolean hasCommonTeamElements(String name1, String name2) {
        // Check for common prefixes (min 3 chars)
        int minLength = Math.min(name1.length(), name2.length());
        int commonPrefix = 0;

        for (int i = 0; i < minLength; i++) {
            if (name1.charAt(i) == name2.charAt(i)) {
                commonPrefix++;
            } else {
                break;
            }
        }

        if (commonPrefix >= 3) return true;

        // Check for common suffixes
        int commonSuffix = 0;
        for (int i = 1; i <= minLength; i++) {
            if (name1.charAt(name1.length() - i) == name2.charAt(name2.length() - i)) {
                commonSuffix++;
            } else {
                break;
            }
        }

        return commonSuffix >= 3;
    }

    private String extractTeamFromFormattedName(String formattedName) {
        // Look for common team indicators
        if (formattedName.contains("[") && formattedName.contains("]")) {
            int start = formattedName.indexOf('[');
            int end = formattedName.indexOf(']');
            if (start < end) {
                return formattedName.substring(start + 1, end);
            }
        }

        // Look for team colors or formatting
        if (formattedName.contains("RED") || formattedName.contains(Formatting.RED.toString())) {
            return "RED";
        } else if (formattedName.contains("BLUE") || formattedName.contains(Formatting.BLUE.toString())) {
            return "BLUE";
        } else if (formattedName.contains("GREEN") || formattedName.contains(Formatting.GREEN.toString())) {
            return "GREEN";
        } else if (formattedName.contains("YELLOW") || formattedName.contains(Formatting.YELLOW.toString())) {
            return "YELLOW";
        }

        return null;
    }

    // Getters for other mods to use
    public boolean isTeammate(PlayerEntity player) {
        return player != null && teammates.contains(player.getGameProfile().getName());
    }

    public boolean isTeammate(String playerName) {
        return teammates.contains(playerName);
    }

    public Set<String> getTeammates() {
        return new HashSet<>(teammates);
    }

    public String getPlayerTeam(String playerName) {
        return playerTeams.get(playerName);
    }

    public Color getTeamColor() {
        return teamColor.getColor();
    }

    public boolean shouldShowESP() {
        return showTeamESP.getDefaultValue();
    }

    public boolean shouldShowNames() {
        return showTeamNames.getDefaultValue();
    }

    public boolean shouldHighlightInTab() {
        return highlightInTab.getDefaultValue();
    }

    public double getMaxDistance() {
        return maxDistance.getDefaultValue();
    }

    public int getTeamSize() {
        return teammates.size();
    }

    public static TeamsMod getInstance() {
        return instance;
    }
}
