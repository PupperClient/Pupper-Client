/*
 * Copyright (C) 2024-2026 Pupper Client contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.pupperclient.gui.api.page.impl;

import cn.pupperclient.animation.Animation;
import cn.pupperclient.gui.api.page.GuiTransition;

/**
 * A horizontal slide between two pages.
 *
 * <p>Every horizontal transition is the same interpolation between a screen
 * offset while entering and one while leaving; only those two offsets differ.
 */
public abstract class SlideTransition extends GuiTransition {

    private final float entering;
    private final float leaving;

    /**
     * @param consecutive whether the next transition continues this one
     * @param entering    horizontal offset, in screens, at the start of an entering slide
     * @param leaving     horizontal offset, in screens, at the start of a leaving slide
     */
    protected SlideTransition(boolean consecutive, float entering, float leaving) {
        super(consecutive);
        this.entering = entering;
        this.leaving = leaving;
    }

    @Override
    public float[] onTransition(Animation animation) {
        float progress = animation.getValue();
        float from = animation.getEnd() == 1 ? entering : leaving;
        return new float[] {from + (from < 0 ? progress : -progress), 0};
    }
}
