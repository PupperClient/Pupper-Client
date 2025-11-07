package com.soarclient.animation.cubicbezier.impl;

import com.soarclient.animation.cubicbezier.CubicBezier;

public class EaseEmphasizedAccelerate extends CubicBezier {

	public EaseEmphasizedAccelerate(float duration, float start, float end) {
		super(0.3F, 0F, 0.8F, 0.15F, duration, start, end);
	}
}
