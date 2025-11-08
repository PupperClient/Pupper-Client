package cn.pupperclient.gui;

import cn.pupperclient.Soar;
import cn.pupperclient.gui.api.SimpleSoarGui;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.ui.component.impl.Button;
import cn.pupperclient.ui.component.handler.impl.ButtonHandler;
import cn.pupperclient.ui.component.impl.text.TextField;
import cn.pupperclient.utils.language.I18n;
import cn.pupperclient.utils.mouse.ScrollHelper;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.List;

public class AddServerGui extends SimpleSoarGui {

    private final MultiplayerGui parent;
    private TextField serverNameField;
    private TextField serverAddressField;
    private Button addButton;
    private Button cancelButton;
    private final List<MainMenuGui.BackgroundItem> backgroundItems = new ArrayList<>();
    private final ScrollHelper backgroundScrollHelper = new ScrollHelper();
    private float parallaxX = 0;
    private float parallaxY = 0;

    public AddServerGui(MultiplayerGui parent) {
        super(false);
        this.parent = parent;
    }

    @Override
    public void init() {
        float centerX = client.getWindow().getWidth() / 2f;
        float centerY = client.getWindow().getHeight() / 2f;
        float panelWidth = 400;
        float panelHeight = 200;
        float panelX = centerX - panelWidth / 2;
        float panelY = centerY - panelHeight / 2;

        // 服务器名称输入框
        serverNameField = new TextField(
            panelX + 20,
            panelY + 50,
            panelWidth - 40,
            "");

        // 服务器地址输入框
        serverAddressField = new TextField(
            panelX + 20,
            panelY + 100,
            panelWidth - 40,
            ""
        );

        // 添加按钮
        addButton = new Button(
            "addServer.add",
            panelX + 40,
            panelY + panelHeight - 40,
            Button.Style.FILLED
        );
        addButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                addServer();
            }
        });

        // 取消按钮
        cancelButton = new Button(
            "gui.cancel",
            panelX + panelWidth - 120,
            panelY + panelHeight - 40,
            Button.Style.TONAL
        );
        cancelButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                client.setScreen(parent.build());
            }
        });
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        ColorPalette palette = Soar.getInstance().getColorManager().getPalette();
        float centerX = client.getWindow().getWidth() / 2f;
        float centerY = client.getWindow().getHeight() / 2f;
        float panelWidth = 400;
        float panelHeight = 200;
        float panelX = centerX - panelWidth / 2;
        float panelY = centerY - panelHeight / 2;

        // 绘制半透明背景
        drawCustomBackground(palette);

        // 绘制面板
        Skia.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 10, palette.getSurfaceContainer());

        // 绘制标题
        Skia.drawCenteredText(
            I18n.get("addServer.title"),
            centerX,
            panelY + 20,
            palette.getOnSurface(),
            Fonts.getRegular(18)
        );

        // 绘制标签
        Skia.drawText(
            I18n.get("addServer.serverName"),
            panelX + 20,
            panelY + 35,
            palette.getOnSurfaceVariant(),
            Fonts.getRegular(14)
        );

        Skia.drawText(
            I18n.get("addServer.serverAddress"),
            panelX + 20,
            panelY + 85,
            palette.getOnSurfaceVariant(),
            Fonts.getRegular(14)
        );

        // 绘制输入框和按钮
        serverNameField.draw(mouseX, mouseY);
        serverAddressField.draw(mouseX, mouseY);
        addButton.draw(mouseX, mouseY);
        cancelButton.draw(mouseX, mouseY);
    }

    @Override
    public void mousePressed(double mouseX, double mouseY, int button) {
        serverNameField.mousePressed(mouseX, mouseY, button);
        serverAddressField.mousePressed(mouseX, mouseY, button);
        addButton.mousePressed(mouseX, mouseY, button);
        cancelButton.mousePressed(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        serverNameField.mouseReleased(mouseX, mouseY, button);
        serverAddressField.mouseReleased(mouseX, mouseY, button);
        addButton.mouseReleased(mouseX, mouseY, button);
        cancelButton.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        serverNameField.charTyped(chr, modifiers);
        serverAddressField.charTyped(chr, modifiers);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        serverNameField.keyPressed(keyCode, scanCode, modifiers);
        serverAddressField.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == 256) { // ESC key
            client.setScreen(parent.build());
        }
    }

    private void addServer() {
        String name = serverNameField.getText();
        String address = serverAddressField.getText();

        if (name.isEmpty()) {
            name = address;
        }

        if (!address.isEmpty()) {
            ServerList serverList = new ServerList(client);
            serverList.loadFile();

            // 创建服务器信息
            ServerInfo serverInfo = new ServerInfo(name, address, ServerInfo.ServerType.OTHER);

            // 添加服务器到列表
            serverList.add(serverInfo, false);
            serverList.saveFile();

            // 返回 multiplayer 界面并刷新
            parent.refreshServerList();
            client.setScreen(parent.build());
        }
    }

    private void drawCustomBackground(ColorPalette palette) {
        // 计算视差效果
        float parallaxStrength = 40;
        float targetParallaxX = (float) (client.mouse.getX() - client.getWindow().getWidth() / 2) / client.getWindow().getWidth() * parallaxStrength;
        float targetParallaxY = (float) (client.mouse.getY() - client.getWindow().getHeight() / 2) / client.getWindow().getHeight() * parallaxStrength;

        parallaxX += (targetParallaxX - parallaxX) * 0.1f;
        parallaxY += (targetParallaxY - parallaxY) * 0.1f;

        float backgroundScale = 1.2f;
        float scaledWidth = client.getWindow().getWidth() * backgroundScale;
        float scaledHeight = client.getWindow().getHeight() * backgroundScale;

        float offsetX = (scaledWidth - client.getWindow().getWidth()) / 2 - parallaxX;
        float offsetY = (scaledHeight - client.getWindow().getHeight()) / 2 - parallaxY;

        String selectedBackgroundId = "background.png";
        Skia.drawImage(selectedBackgroundId, -offsetX, -offsetY, scaledWidth, scaledHeight);
    }
}
