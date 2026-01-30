#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 u_Proj;
uniform mat4 u_ModelView;

out vec4 v_Color;

void main() {
    gl_Position = u_Proj * u_ModelView * vec4(Position, 1.0);
    v_Color = Color;
}
