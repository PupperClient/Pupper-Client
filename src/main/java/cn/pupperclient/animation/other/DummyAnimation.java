package cn.pupperclient.animation.other;

import cn.pupperclient.animation.Animation;
import cn.pupperclient.animation.Delta;

public class DummyAnimation extends Animation {

	private float value;

	public DummyAnimation(float duration, float value) {
		super(duration, value, value);
		this.value = value;
	}

	public DummyAnimation(float value) {
		this(1, value);
	}

	public DummyAnimation() {
		this(1, 0);
	}

	@Override
	public float getValue() {
		timePassed += (float) Delta.getDeltaTime();
		return value;
	}

	@Override
	protected float animate(float x) {
		return x;
	}

    @Override
    protected double getEquation(double x) {
        return 0;
    }
}
