package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleListHUDMod;

import java.util.List;

public class Test extends SimpleListHUDMod {
    public Test() {
        super("Test", "Test", "");
    }

    @EventListener
    public void onSkiaRender(RenderSkiaEvent e) { this.draw(); }

    @Override
    public List<String> getText() {
        return List.of("123", "456", "789");
    }

    @Override
    public String getIcon() {
        return "";
    }
}
