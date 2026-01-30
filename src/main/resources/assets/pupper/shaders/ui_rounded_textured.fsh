#version 150

precision mediump float;

in vec4 v_Color;
in vec2 v_UV;

uniform sampler2D Sampler0;
uniform vec2 uSize;      // 矩形宽高
uniform float uRadius;   // 圆角半径

out vec4 fragColor;

// 计算点到圆角矩形边界的距离
float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {
    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;
}

void main() {
    // 将 UV 转换为以中心为原点的局部坐标
    vec2 localPos = (v_UV - 0.5) * uSize;

    // 计算 SDF
    float distance = roundedBoxSDF(localPos, uSize * 0.5, uRadius);

    // 使用 smoothstep 进行抗锯齿边缘裁切
    float alpha = 1.0 - smoothstep(-1.0, 1.0, distance);

    vec4 texColor = texture(Sampler0, v_UV);
    fragColor = texColor * v_Color * alpha;

    if (fragColor.a < 0.001) discard;
}
