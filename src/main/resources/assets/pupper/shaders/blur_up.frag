#version 330 core

precision lowp float;

in vec2 uv;
out vec4 color;

uniform sampler2D uTexture;
uniform vec2 uHalfTexelSize;
uniform float uOffset;

vec4 safeTexture2D(sampler2D tex, vec2 uv) {
    uv = clamp(uv, 0.001, 0.999); // 防止在边缘采样
    return texture(tex, uv);
}

void main() {
    color = (
        texture(uTexture, uv + vec2(- uHalfTexelSize.x * 2, 0) * uOffset) +
        texture(uTexture, uv + vec2(- uHalfTexelSize.x, uHalfTexelSize.y) * uOffset) * 2 +
        texture(uTexture, uv + vec2(0, uHalfTexelSize.y * 2) * uOffset) +
        texture(uTexture, uv + uHalfTexelSize * uOffset) * 2 +
        texture(uTexture, uv + vec2(uHalfTexelSize.x * 2, 0) * uOffset) +
        texture(uTexture, uv + vec2(uHalfTexelSize.x, -uHalfTexelSize.y) * uOffset) * 2 +
        texture(uTexture, uv + vec2(0, -uHalfTexelSize.y * 2) * uOffset) +
        texture(uTexture, uv - uHalfTexelSize * uOffset) * 2
    ) / 12;
    color.a = 1;
}

