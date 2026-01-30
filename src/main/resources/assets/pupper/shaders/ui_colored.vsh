#version 150

in vec2 Position;
in vec4 Color;

uniform mat4 u_Proj;

out vec4 v_Color;

void main() {
    gl_Position = u_Proj * vec4(Position, 0.0, 1.0);
    v_Color = Color;
}
