package cn.pupperclient.animation;

public abstract class Animation {
	protected float duration;
	protected float start;
	protected float change;
	protected float timePassed = 0;


	public Animation(float duration, float start, float end) {

		this.duration = duration;

		this.start = start;
		this.change = end - start;
	}

	public float getValue() {

		timePassed += (float) Delta.getDeltaTime();

		if (timePassed >= duration) {
			return start + change;
		}

		return animate(timePassed / duration) * change + start;
	}

	public boolean isFinished() {
		return timePassed >= duration;
	}

	public float getEnd() {
		return start + change;
	}

	protected abstract float animate(float x);

    protected boolean correctOutput() { return false; }
    protected abstract double getEquation(double x);

    public void reset() {
        timePassed = 0;
    }
}
