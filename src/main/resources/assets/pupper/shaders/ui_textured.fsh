#version 150

precision mediump float;

in vec4 v_Color;
in vec2 v_UV;

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    fragColor = texture(Sampler0, v_UV) * v_Color;
}
