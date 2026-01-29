#version 150

precision mediump float;

in vec2 uv;
out vec4 fragColor;

uniform sampler2D Sampler0;
uniform vec2 uHalfTexelSize;
uniform float uOffset;

void main() {
    vec4 result = (
        texture(Sampler0, uv + vec2(-uHalfTexelSize.x * 2.0, 0.0) * uOffset) +
        texture(Sampler0, uv + vec2(-uHalfTexelSize.x, uHalfTexelSize.y) * uOffset) * 2.0 +
        texture(Sampler0, uv + vec2(0.0, uHalfTexelSize.y * 2.0) * uOffset) +
        texture(Sampler0, uv + uHalfTexelSize * uOffset) * 2.0 +
        texture(Sampler0, uv + vec2(uHalfTexelSize.x * 2.0, 0.0) * uOffset) +
        texture(Sampler0, uv + vec2(uHalfTexelSize.x, -uHalfTexelSize.y) * uOffset) * 2.0 +
        texture(Sampler0, uv + vec2(0.0, -uHalfTexelSize.y * 2.0) * uOffset) +
        texture(Sampler0, uv - uHalfTexelSize * uOffset) * 2.0
    ) / 12.0;

    fragColor = vec4(result.rgb, 1.0);
}
