package cn.pupperclient.gui.api;

import java.util.ArrayList;
import java.util.List;

import cn.pupperclient.PupperClient;
import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.RenderSkiaEvent;
import org.lwjgl.glfw.GLFW;

import cn.pupperclient.animation.Animation;
import cn.pupperclient.animation.Duration;
import cn.pupperclient.animation.cubicbezier.impl.EaseEmphasizedDecelerate;
import cn.pupperclient.gui.api.page.GuiTransition;
import cn.pupperclient.gui.api.page.SimplePage;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.management.config.ConfigType;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.ui.component.Component;
import cn.pupperclient.utils.concurrent.Multithreading;

import net.minecraft.client.gui.screen.Screen;

public abstract class PupperGui extends SimplePupperGui {

	protected List<Component> components = new ArrayList<>();
	protected List<SimplePage> pages;

	protected SimplePage currentPage;
	protected SimplePage lastPage;

	private Animation inOutAnimation;
	private boolean closable;
	private Screen nextScreen;

	public PupperGui(boolean mcScale) {
		super(mcScale);

		this.pages = createPages();

		if (!pages.isEmpty()) {
			this.currentPage = pages.getFirst();
		}
	}

	@Override
	public void init() {
        if (!EventBus.getInstance().isregister(this)) {
            EventBus.getInstance().register(this);
        }
        inOutAnimation = new EaseEmphasizedDecelerate(Duration.EXTRA_LONG_1, 0, 1);
        closable = true;

        if (currentPage != null) {
            setPageSize(currentPage);
            currentPage.init();
        }
        super.init();
	}

    @Override
    public void draw(double mouseX, double mouseY) {
        if (inOutAnimation == null || client.currentScreen == null) {
            return;
        }

        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        if (inOutAnimation.getEnd() == 0 && inOutAnimation.isFinished()) {
            client.setScreen(nextScreen);
            nextScreen = null;
            return;
        }

        Skia.save();

        float alpha = inOutAnimation.getValue();
        Skia.setAlpha((int) (alpha * 255));

        Skia.scale(getX(), getY(), getWidth(), getHeight(), 2 - alpha);

        Skia.clip(getX(), getY(), getWidth(), getHeight(), 35);
        Skia.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 35, palette.getSurfaceContainer());

        renderPages(mouseX, mouseY);

        for (Component c : components) {
            c.draw(mouseX, mouseY);
        }

        Skia.restore();
    }

	@Override
	public void mousePressed(double mouseX, double mouseY, int button) {

		if (currentPage != null) {
			currentPage.mousePressed(mouseX, mouseY, button);
		}

		for (Component c : components) {
			c.mousePressed(mouseX, mouseY, button);
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {

		if (currentPage != null) {
			currentPage.mouseReleased(mouseX, mouseY, button);
		}

		for (Component c : components) {
			c.mouseReleased(mouseX, mouseY, button);
		}
	}

	@Override
	public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (currentPage != null) {
			currentPage.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
	}

	@Override
	public void charTyped(char chr, int modifiers) {

		if (currentPage != null) {
			currentPage.charTyped(chr, modifiers);
		}

		for (Component c : components) {
			c.charTyped(chr, modifiers);
		}
	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {

		if (keyCode == GLFW.GLFW_KEY_ESCAPE && inOutAnimation.getEnd() == 1 && closable) {
			close();
		}

		if (currentPage != null) {
			currentPage.keyPressed(keyCode, scanCode, modifiers);
		}

		for (Component c : components) {
			c.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	public void close(Screen nextScreen) {
		if (inOutAnimation.getEnd() == 1) {
			this.nextScreen = nextScreen;
			inOutAnimation = new EaseEmphasizedDecelerate(Duration.EXTRA_LONG_1, 1, 0);
			client.execute(() -> PupperClient.getInstance().getConfigManager().save(ConfigType.MOD));
		}
	}

    @Override
	public void close() {
		close(null);
	}

    @Override
    public void removed() {
        if (EventBus.getInstance().isregister(this)) {
            EventBus.getInstance().unregister(this);
        }
        if (pages != null) {
            pages.forEach(SimplePage::onClosed);
        }
        super.removed();
    }

    public void setPageSize(SimplePage p) {
        p.setX(getX());
        p.setY(getY());
        p.setWidth(getWidth());
        p.setHeight(getHeight());
    }

	public SimplePage getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(SimplePage page) {

		if (currentPage != null) {
			lastPage = currentPage;
			currentPage.onClosed();
		}

		this.currentPage = page;
		currentPage.setAnimation(new EaseEmphasizedDecelerate(Duration.MEDIUM_1, 0, 1));
		lastPage.setAnimation(new EaseEmphasizedDecelerate(Duration.MEDIUM_1, 1, 0));

		if (currentPage != null) {
			setPageSize(currentPage);
			currentPage.init();
		}
	}

	public void setCurrentPage(Class<? extends SimplePage> clazz) {

		SimplePage page = getPage(clazz);

		if (page != null) {
			setCurrentPage(page);
		}
	}

	public SimplePage getPage(Class<? extends SimplePage> clazz) {

		SimplePage page = null;

		for (SimplePage p : pages) {
			if (p.getClass().equals(clazz)) {
				page = p;
				break;
			}
		}

		return page;
	}

    private void renderPages(double mouseX, double mouseY) {
        if (currentPage != null && lastPage == null) {
            currentPage.draw(mouseX, mouseY);
        }

        if (lastPage != null) {
            renderPageWithTransition(lastPage, mouseX, mouseY);

            renderPageWithTransition(currentPage, mouseX, mouseY);

            if (lastPage.getAnimation().isFinished()) {
                lastPage = null;
            }
        }
    }

    private void renderPageWithTransition(SimplePage page, double mx, double my) {
        if (page == null) return;
        GuiTransition transition = page.getTransition();
        Skia.save();
        if (transition != null && page.getAnimation() != null) {
            float[] result = transition.onTransition(page.getAnimation());
            Skia.translate(result[0] * getWidth(), result[1] * getHeight());
        }
        page.draw(mx, my);
        Skia.restore();
    }

    @EventListener
    public void onSkiaRender(RenderSkiaEvent event) {
        if (isVisible && client.currentScreen != null) {
            draw(client.mouse.getX(), client.mouse.getY());
        }
    }

	public List<SimplePage> getPages() {
		return pages;
	}

	public boolean isClosable() {
		return closable;
	}

	public void setClosable(boolean closable) {
		this.closable = closable;
	}

	public abstract List<SimplePage> createPages();

	public abstract float getX();

	public abstract float getY();

	public abstract float getWidth();

	public abstract float getHeight();
}
