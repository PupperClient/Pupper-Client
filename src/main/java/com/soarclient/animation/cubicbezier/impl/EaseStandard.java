package com.soarclient.animation.cubicbezier.impl;

import com.soarclient.animation.cubicbezier.CubicBezier;

public class EaseStandard extends CubicBezier {

	public EaseStandard(float duration, float start, float end) {
		super(0.2F, 0.0F, 0F, 1F, duration, start, end);
	}
}
