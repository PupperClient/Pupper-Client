#version 150

precision mediump float;

in vec4 v_Color;
out vec4 fragColor;

void main() {
    // Simple color passthrough
    fragColor = v_Color;
}
