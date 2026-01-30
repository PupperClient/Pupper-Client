#version 150

in vec2 Position;
uniform mat4 u_Proj;
out vec2 uv;

void main() {
    gl_Position = u_Proj * vec4(Position, 0.0, 1.0);
    uv = Position * 0.5 + 0.5;
}
