package cn.pupperclient.gui;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.pupperclient.PupperClient;
import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.EventDisconnect;
import cn.pupperclient.gui.api.SimpleSoarGui;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.ui.component.api.PressAnimation;
import cn.pupperclient.ui.component.impl.Button;
import cn.pupperclient.ui.component.handler.impl.ButtonHandler;
import cn.pupperclient.ui.component.impl.IconButton;
import cn.pupperclient.utils.ColorUtils;
import cn.pupperclient.utils.language.I18n;
import cn.pupperclient.utils.mouse.MouseUtils;
import cn.pupperclient.utils.mouse.ScrollHelper;

import com.viaversion.viafabricplus.screen.impl.ProtocolSelectionScreen;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.github.humbleui.types.Rect;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;

public class MultiplayerGui extends SimpleSoarGui {

    private IconButton versionButton;
    private final List<ServerButton> serverButtons = new CopyOnWriteArrayList<>();
    private boolean needsListUpdate = false;
    private Button addServerButton;
    private Button directConnectButton;
    private IconButton refreshButton;
    private IconButton backButton;
    private final ScrollHelper scrollHelper = new ScrollHelper();
    private float lastWindowWidth = 0;
    private float lastWindowHeight = 0;
    private boolean wasMinimized = false;
    private final ServerList serverList;
    private float parallaxX = 0;
    private float parallaxY = 0;

    public MultiplayerGui() {
        super(false);
        this.serverList = new ServerList(client);
        this.serverList.loadFile();
    }

    @Override
    public void init() {
        updateLayout();
    }

    private void updateLayout() {
        serverButtons.clear();

        float scaleFactor = calculateScaleFactor();
        float centerX = client.getWindow().getWidth() / 2f;
        float centerY = client.getWindow().getHeight() / 2f;

        float listWidth = Math.min(600 * scaleFactor, client.getWindow().getWidth() - 80 * scaleFactor);
        float listHeight = Math.min(400 * scaleFactor, client.getWindow().getHeight() - 200 * scaleFactor);
        float listX = centerX - listWidth / 2;
        float listY = centerY - listHeight / 2;


        loadServerList(listX, listY, listWidth, 40 * scaleFactor, scaleFactor);

        // 按钮区域
        float buttonWidth = 200 * scaleFactor;
        float buttonSpacing = 15 * scaleFactor;
        float buttonsY = listY + listHeight + 30 * scaleFactor;

        addServerButton = new Button("multiplayer.add_server", centerX - buttonWidth - buttonSpacing, buttonsY, Button.Style.FILLED);
        addServerButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                client.setScreen(new AddServerGui(MultiplayerGui.this).build());
            }
        });

        directConnectButton = new Button("multiplayer.direct_connect", centerX + buttonSpacing, buttonsY, Button.Style.FILLED);
        directConnectButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {

            }
        });

        float topButtonSize = 45 * scaleFactor;
        float topButtonSpacing = 15 * scaleFactor;

        refreshButton = new IconButton(Icon.REFRESH,
            client.getWindow().getWidth() - topButtonSize * 2 - topButtonSpacing - 20 * scaleFactor - 15,
            25 * scaleFactor,
            IconButton.Size.NORMAL,
            IconButton.Style.TERTIARY);
        refreshButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                updateLayout();
            }
        });

        backButton = new IconButton(Icon.ARROW_BACK,
            client.getWindow().getWidth() - topButtonSize - 20 * scaleFactor,
            25 * scaleFactor,
            IconButton.Size.NORMAL,
            IconButton.Style.TERTIARY);
        backButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                client.setScreen(new MainMenuGui().build());
            }
        });

        versionButton = new IconButton(Icon.GAMES,
            client.getWindow().getWidth() - topButtonSize * 3 - topButtonSpacing * 2 - 20 * scaleFactor - 30,
            25 * scaleFactor,
            IconButton.Size.NORMAL,
            IconButton.Style.TERTIARY);
        versionButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                //client.setScreen(new VersionSelectGui(MultiplayerGui.this).build());
                client.setScreen(ProtocolSelectionScreen.INSTANCE);
            }
        });

        lastWindowWidth = client.getWindow().getWidth();
        lastWindowHeight = client.getWindow().getHeight();

        needsListUpdate = false;
    }

    private void loadServerList(float x, float y, float width, float itemHeight, float scaleFactor) {
        float newItemHeight = 75 * scaleFactor; // Increased from 60 to 75

        for (int i = 0; i < serverList.size(); i++) {
            ServerInfo serverInfo = serverList.get(i);
            float itemY = y + i * (newItemHeight + 5 * scaleFactor);
            serverButtons.add(new ServerButton(serverInfo, x, itemY, width, newItemHeight, scaleFactor));
        }

        // 设置滚动范围
        float totalHeight = serverList.size() * (newItemHeight + 5 * scaleFactor);
        scrollHelper.setMaxScroll(totalHeight, 400 * scaleFactor);
    }

    private boolean isWindowMinimized() {
        return client.getWindow().getWidth() < 100 || client.getWindow().getHeight() < 100;
    }

    private float calculateScaleFactor() {
        float currentWidth = client.getWindow().getWidth();
        float currentHeight = client.getWindow().getHeight();

        if (isWindowMinimized()) {
            return 0.5f;
        }

        float windowArea = currentWidth * currentHeight;

        if (windowArea < 800 * 600) {
            return 1.4f;
        } else if (windowArea < 1280 * 720) {
            return 1.2f;
        } else if (windowArea < 1920 * 1080) {
            return 1.0f;
        } else {
            return 0.9f;
        }
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        boolean currentlyMinimized = isWindowMinimized();

        if (client.getWindow().getWidth() != lastWindowWidth ||
            client.getWindow().getHeight() != lastWindowHeight ||
            wasMinimized != currentlyMinimized ||
            needsListUpdate) {
            updateLayout();
            wasMinimized = currentlyMinimized;
        }

        if (currentlyMinimized) {
            return;
        }

        PupperClient instance = PupperClient.getInstance();
        ColorPalette palette = instance.getColorManager().getPalette();

        // 绘制背景
        drawCustomBackground(palette);

        // 绘制标题
        Skia.drawCenteredText(I18n.get("menu.multiplayer"),
            client.getWindow().getWidth() / 2f,
            60 * calculateScaleFactor(),
            palette.getOnSurface(),
            Fonts.getRegular(28 * calculateScaleFactor()));

        String versionText = "ProtocolVersion is " + getProtocolVersion().getName();
        Skia.drawText(versionText,
            20 * calculateScaleFactor(),
            60 * calculateScaleFactor() + 5,
            palette.getOnSurfaceVariant(),
            Fonts.getRegular(15 * calculateScaleFactor()));

        // 绘制服务器列表背景
        float centerX = client.getWindow().getWidth() / 2f;
        float centerY = client.getWindow().getHeight() / 2f;
        float listWidth = Math.min(600 * calculateScaleFactor(), client.getWindow().getWidth() - 80 * calculateScaleFactor());
        float listHeight = Math.min(400 * calculateScaleFactor(), client.getWindow().getHeight() - 200 * calculateScaleFactor());
        float listX = centerX - listWidth / 2;
        float listY = centerY - listHeight / 2;

        Skia.drawRoundedRect(listX, listY, listWidth, listHeight, 15 * calculateScaleFactor(), palette.getSurfaceContainer());

        // 绘制服务器列表
        Skia.save();
        Skia.translate(0, scrollHelper.getValue());

        for (ServerButton serverButton : serverButtons) {
            serverButton.draw((int) mouseX, (int) (mouseY - scrollHelper.getValue()));
        }

        Skia.restore();

        // 绘制按钮
        addServerButton.draw(mouseX, mouseY);
        directConnectButton.draw(mouseX, mouseY);
        refreshButton.draw(mouseX, mouseY);
        backButton.draw(mouseX, mouseY);
        versionButton.draw(mouseX, mouseY);

        Skia.restore();
    }

    @Override
    public void mousePressed(double mouseX, double mouseY, int button) {
        if (isWindowMinimized()) {
            return;
        }

        double adjustedMouseY = mouseY - scrollHelper.getValue();

        for (ServerButton serverButton : serverButtons) {
            serverButton.mousePressed((int) mouseX, (int) adjustedMouseY, button);
        }

        addServerButton.mousePressed(mouseX, mouseY, button);
        directConnectButton.mousePressed(mouseX, mouseY, button);
        refreshButton.mousePressed(mouseX, mouseY, button);
        backButton.mousePressed(mouseX, mouseY, button);
        versionButton.mousePressed(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (isWindowMinimized()) {
            return;
        }

        double adjustedMouseY = mouseY - scrollHelper.getValue();

        for (ServerButton serverButton : serverButtons) {
            serverButton.mouseReleased((int) mouseX, (int) adjustedMouseY, button);
        }

        addServerButton.mouseReleased(mouseX, mouseY, button);
        directConnectButton.mouseReleased(mouseX, mouseY, button);
        refreshButton.mouseReleased(mouseX, mouseY, button);
        backButton.mouseReleased(mouseX, mouseY, button);
        versionButton.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 检查鼠标是否在服务器列表区域内
        float centerX = client.getWindow().getWidth() / 2f;
        float centerY = client.getWindow().getHeight() / 2f;
        float listWidth = Math.min(600 * calculateScaleFactor(), client.getWindow().getWidth() - 80 * calculateScaleFactor());
        float listHeight = Math.min(400 * calculateScaleFactor(), client.getWindow().getHeight() - 200 * calculateScaleFactor());
        float listX = centerX - listWidth / 2;
        float listY = centerY - listHeight / 2;
    
        if (MouseUtils.isInside((float) mouseX, (float) mouseY, listX, listY, listWidth, listHeight)) {
            scrollHelper.onScroll(verticalAmount * 20); // 增加滚动速度
        }
    }

    private class ServerButton {
        private final PressAnimation pressAnimation = new PressAnimation();
        private final SimpleAnimation focusAnimation = new SimpleAnimation();
        private final ServerInfo serverInfo; // 存储服务器信息
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final float scaleFactor;
        private int[] pressedPos;
        private final Runnable action;
        private final Runnable RemoveAction;
        private boolean isPressed = false;

        public ServerButton(ServerInfo serverInfo, float x, float y, float width, float height, float scaleFactor) {
            this.serverInfo = serverInfo; // 保存服务器信息
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.scaleFactor = scaleFactor;
            this.pressedPos = new int[]{0, 0};
            this.action = () -> {
                ServerAddress serverAddress = ServerAddress.parse(serverInfo.address);

                try {
                    ConnectScreen.connect(
                        MultiplayerGui.this.build(),
                        client,
                        serverAddress,
                        serverInfo,
                        false,
                        null
                    );
                } catch (Exception e) {
                    client.setScreen(new MessageScreen(
                        Text.literal("Error connecting to server: " + e.getMessage())
                    ));
                }
            };
            this.RemoveAction = () -> {
                for (int i = 0; i < serverList.size(); i++) {
                    if (serverList.get(i).address.equals(serverInfo.address)) {
                        serverList.remove(serverList.get(i));
                        break;
                    }
                }
                serverList.saveFile();
                needsListUpdate = true;
            };
        }

        public void draw(int mouseX, int mouseY) {
            ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height);

            focusAnimation.onTick(hovered ? 1.0F : 0, 12);

            float radius = 8 * scaleFactor;

            Skia.drawRoundedRect(x, y, width, height, radius, palette.getSurface());

            Color hoverColor = palette.getPrimary();
            Skia.drawRoundedRect(x, y, width, height, radius,
                ColorUtils.applyAlpha(hoverColor, focusAnimation.getValue() * 0.12F));

            Skia.save();
            Skia.clip(x, y, width, height, radius);
            pressAnimation.draw(x + pressedPos[0], y + pressedPos[1], width, height, palette.getPrimary(), 1);
            Skia.restore();

            Color textColor = hovered ?
                ColorUtils.blend(palette.getOnSurfaceVariant(), palette.getPrimary(), focusAnimation.getValue()) :
                palette.getOnSurfaceVariant();

            float fontSize = 16 * scaleFactor;
            float padding = 10 * scaleFactor;
            float iconSize = 32 * scaleFactor;

            if (serverInfo.getFavicon() != null) {
                io.github.humbleui.skija.Image image = io.github.humbleui.skija.Image.makeFromEncoded(serverInfo.getFavicon());

                float scale = Math.min(iconSize / image.getWidth(), iconSize / image.getHeight());
                float scaledWidth = image.getWidth() * scale;
                float scaledHeight = image.getHeight() * scale;

                // 居中绘制
                float drawX = x + (iconSize - scaledWidth) / 2;
                float drawY = y + (iconSize - scaledHeight) / 2;

                Skia.getCanvas().drawImageRect(
                    image,
                    io.github.humbleui.types.Rect.makeXYWH(0, 0, image.getWidth(), image.getHeight()),
                    io.github.humbleui.types.Rect.makeXYWH(drawX, drawY, scaledWidth, scaledHeight),
                    Skia.getPaint(textColor)
                );
            }

            // 绘制服务器名称
            Skia.drawText(serverInfo.name,
                x + padding + (serverInfo.getFavicon() != null ? iconSize + padding : 0),
                y + padding + 5 * scaleFactor, // Added vertical offset
                textColor,
                Fonts.getRegular(fontSize));

            // 绘制服务器地址
            Skia.drawText(serverInfo.address,
                x + padding + (serverInfo.getFavicon() != null ? iconSize + padding : 0),
                y + height - padding - fontSize - 5 * scaleFactor, // Adjusted position
                ColorUtils.applyAlpha(textColor, 0.7f),
                Fonts.getRegular(fontSize * 0.8f));

            // 绘制ping状态
            String statustext = serverInfo.ping > 0 ? "Online" : "Offline";
            Rect status = Skia.getTextBounds(statustext, Fonts.getRegular(fontSize * 0.8f));

            Skia.drawText(statustext,
                x + width - padding - status.getWidth(),
                y + padding,
                ColorUtils.applyAlpha(textColor, 0.7f),
                Fonts.getRegular(fontSize * 0.8f));

            // 绘制ping值
            if (serverInfo.ping > 0) {
                String pingText = serverInfo.ping + "ms";
                Rect pingWidth = Skia.getTextBounds(pingText, Fonts.getRegular(fontSize * 0.8f));
                Skia.drawText(pingText,
                    x + width - padding - pingWidth.getWidth(),
                    y + height - padding - fontSize,
                    ColorUtils.applyAlpha(textColor, 0.7f),
                    Fonts.getRegular(fontSize * 0.8f));
            }
        }

        public void mousePressed(int mouseX, int mouseY, int mouseButton) {
            if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 0) {
                pressedPos = new int[]{mouseX - (int) x, mouseY - (int) y};
                pressAnimation.onPressed(mouseX, mouseY, x, y);
                isPressed = true;
            }
            else if(MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 1){
                pressedPos = new int[]{mouseX - (int) x, mouseY - (int) y};
                pressAnimation.onPressed(mouseX, mouseY, x, y);
                isPressed = true;
            }
        }

        public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
            if (isPressed && MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 0) {
                action.run();
            }
            else if(isPressed && MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 1){
                RemoveAction.run();
            }
            isPressed = false;
            pressAnimation.onReleased(mouseX, mouseY, x, y);
        }
    }

    public void refreshServerList() {
        this.serverList.loadFile();
        needsListUpdate = true;
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
        Skia.drawImage("background.png", -offsetX, -offsetY, scaledWidth, scaledHeight);
    }

    public ProtocolVersion getProtocolVersion() {
        try {
            return PupperClient.getInstance().getProtocolVersion();
        } catch (Exception e) {
            return ProtocolVersion.v1_21_4;
        }
    }

    public void setProtocolVersion(ProtocolVersion version) {
        try {
            PupperClient.getInstance().setProtocolVersion(version);
        } catch (Exception e) {
            PupperClient.LOGGER.error("Failed to set protocol version: {}", e.getMessage());
        }
    }

    private final EventBus.EventListener<EventDisconnect> onDisconnect = event -> {
        client.setScreen(this.build());
    };
}
