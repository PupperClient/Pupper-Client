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

package cn.pupperclient.management.mod.impl.player;

import org.lwjgl.glfw.GLFW;

import cn.pupperclient.skia.font.Icon;

public class FreelookMod extends PerspectiveMod {

    private static FreelookMod instance;

    public FreelookMod() {
        super("mod.freelook.name", "mod.freelook.description", Icon._360);
        instance = this;
    }

    @Override
    protected String defaultPerspective() {
        return "setting.behind";
    }

    @Override
    protected int defaultKey() {
        return GLFW.GLFW_KEY_B;
    }

    public static FreelookMod getInstance() {
        return instance;
    }
}
