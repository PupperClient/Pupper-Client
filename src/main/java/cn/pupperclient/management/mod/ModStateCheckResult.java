package cn.pupperclient.management.mod;

public record ModStateCheckResult(Mod mod, boolean enabled, boolean hasRecentChange) {

    public String getModName() {
        return mod != null ? mod.getName() : null;
    }
}
