package cn.pupperclient.utils.mouse;

import cn.pupperclient.animation.SimpleAnimation;

public class ScrollHelper {

	private final SimpleAnimation animation = new SimpleAnimation();
	private float scroll;
    private float maxScroll;
    private final float minScroll;

	public ScrollHelper() {
		maxScroll = Float.MAX_VALUE;
		minScroll = 0;
	}

	public void onScroll(double amount) {
		scroll += (float) (amount * 60);
	}

	public void onUpdate() {
		animation.onTick(scroll, 18);
		scroll = Math.max(Math.min(minScroll, scroll), -maxScroll);
	}

	public float getValue() {
		return animation.getValue();
	}

	public void setMaxScroll(float itemHeight, float itemSpace, int itemSize, int row, float height) {
        int rows = (int) Math.ceil((double) itemSize / row);
		float totalHeight = rows * (itemHeight + itemSpace);

        this.maxScroll = Math.max(0, totalHeight - height);
	}

	public void setMaxScroll(float totalHeight, float height) {
        this.maxScroll = Math.max(0, totalHeight - height);
	}

	public void reset() {
		this.scroll = 0;
	}
}
