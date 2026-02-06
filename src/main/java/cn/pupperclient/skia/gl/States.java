/*
 * This file is part of https://github.com/Lyzev/Skija.
 *
 * Copyright (c) 2025. Lyzev
 *
 * Skija is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * (at your option) any later version.
 */

package cn.pupperclient.skia.gl;

import java.util.Stack;

import static org.lwjgl.opengl.GL30.*;

/*
 *  Converted to Java by oneachina
 */
public class States {

    /**
     * The single instance of States.
     */
    public static final States INSTANCE = new States();

    /**
     * The current OpenGL version.
     */
    private final int glVersion;

    /**
     * The stack of OpenGL states.
     */
    private final Stack<State> states = new Stack<>();

    /**
     * Pushes the current OpenGL state onto the stack.
     */
    public static void push() {
        INSTANCE.states.push(new State(INSTANCE.glVersion).push());
    }

    /**
     * Pops the last OpenGL state from the stack and restores it.
     */
    public static void pop() {
        if (INSTANCE.states.isEmpty()) {
            throw new IllegalStateException("No state to restore.");
        }
        INSTANCE.states.pop().pop();
    }

    /**
     * Gets the current OpenGL version.
     * * This code was inspired by imgui-java and modified to fit the project's codebase.
     */
    private States() {
        int[] major = new int[1];
        int[] minor = new int[1];
        glGetIntegerv(GL_MAJOR_VERSION, major);
        glGetIntegerv(GL_MINOR_VERSION, minor);
        this.glVersion = major[0] * 100 + minor[0] * 10;
    }

    /**
     * Returns the glVersion if needed elsewhere.
     */
    @SuppressWarnings("unused")
    public int getGlVersion() {
        return glVersion;
    }
}
