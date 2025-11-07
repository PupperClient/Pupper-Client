package com.soarclient.animation.cubicbezier.impl;

import com.soarclient.animation.cubicbezier.CubicBezier;

public class EaseEmphasizedDecelerate extends CubicBezier {

	public EaseEmphasizedDecelerate(float duration, float start, float end) {
		super(0.05F, 0.7F, 0.1F, 1F, duration, start, end);
	}
}
