package cn.pupperclient.animation;

public class SimpleAnimation {

	private float currentValue;
	private boolean firstTick;
    private float targetValue;
    private long lastUpdateTime;

	public SimpleAnimation() {
		this.firstTick = true;
        this.currentValue = 0;
        this.targetValue = 0;
        this.lastUpdateTime = System.currentTimeMillis();
	}

    public void setValue(float value) {
        this.currentValue = value;
        this.targetValue = value;
    }
    public void setTarget(float target) {
        this.targetValue = target;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void update(float speed) {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        if (deltaTime <= 0) return;
        float delta = (targetValue - currentValue) * (speed * deltaTime / 1000.0f);
        currentValue += delta;

        if (Math.abs(targetValue - currentValue) < 0.01f) {
            currentValue = targetValue;
        }
    }

	public void onTick(float value, float speed) {
		if (firstTick) {
			currentValue = value;
			firstTick = false;
		} else {
			float delta = (float) (((value - currentValue) * (speed / 1000)) * Delta.getDeltaTime());
			currentValue += delta;
		}
	}

	public float getValue() {
		return currentValue;
	}

	public void setFirstTick(boolean firstTick) {
		this.firstTick = firstTick;
	}
}
