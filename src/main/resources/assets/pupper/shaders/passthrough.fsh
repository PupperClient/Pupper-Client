#version 150

precision mediump float;

in vec2 uv;
out vec4 fragColor;

uniform sampler2D Sampler0;

void main() {
    fragColor = texture(Sampler0, uv);
}
