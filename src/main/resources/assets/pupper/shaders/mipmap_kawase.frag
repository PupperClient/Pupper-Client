#version 330 core

uniform sampler2D uTexture;
uniform int uMipmapLevel;
uniform int uIterations;
uniform float uOffset;
uniform vec2 uResolution;

in vec2 uv;
out vec4 fragColor;

// Kawase模糊采样
vec4 kawaseSample(sampler2D tex, vec2 uv, float offset, int level) {
    vec2 texelSize = 1.0 / textureSize(tex, level);

    vec4 color = vec4(0.0);
    color += textureLod(tex, uv + vec2(offset, offset) * texelSize, level);
    color += textureLod(tex, uv + vec2(-offset, offset) * texelSize, level);
    color += textureLod(tex, uv + vec2(offset, -offset) * texelSize, level);
    color += textureLod(tex, uv + vec2(-offset, -offset) * texelSize, level);

    return color * 0.25;
}

// Mipmap混合采样
vec4 mipmapBlendSample(sampler2D tex, vec2 uv, int baseLevel) {
    vec4 result = vec4(0.0);
    float totalWeight = 0.0;

    // 采样多个Mipmap级别
    for (int i = 0; i < 4; i++) {
        if (i > uMipmapLevel) break;

        int level = baseLevel + i;
        float weight = 1.0 / (1.0 + float(i) * 0.5); // 高级别权重递减

        // 对每个Mipmap级别应用Kawase模糊
        vec4 levelColor = kawaseSample(tex, uv, uOffset * (1.0 + float(i) * 0.3), level);
        result += levelColor * weight;
        totalWeight += weight;
    }

    return result / totalWeight;
}

vec4 safeTexture2D(sampler2D tex, vec2 uv) {
    uv = clamp(uv, 0.001, 0.999); // 防止在边缘采样
    return texture(tex, uv);
}

void main() {
    // 根据Mipmap级别选择基础采样级别
    int baseLevel = uMipmapLevel;

    // 应用混合模糊
    vec4 color = mipmapBlendSample(uTexture, uv, baseLevel);

    // 边缘柔化
    vec2 coord = gl_FragCoord.xy / uResolution;
    float edge = smoothstep(0.0, 0.05, min(min(coord.x, coord.y), min(1.0 - coord.x, 1.0 - coord.y)));
    color *= edge;

    fragColor = color;
}
