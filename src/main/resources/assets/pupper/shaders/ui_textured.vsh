#version 150

in vec2 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 u_Proj;
uniform mat4 u_ModelView;

out vec4 v_Color;
out vec2 v_UV;

void main() {
    gl_Position = u_Proj * u_ModelView * vec4(Position, 0.0, 1.0);
    v_Color = Color;
    v_UV = UV0;
}
