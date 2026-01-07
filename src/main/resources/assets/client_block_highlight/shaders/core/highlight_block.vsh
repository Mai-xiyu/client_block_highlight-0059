#version 150

in vec3 Position;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;

out vec4 vertexColor;
out vec3 vertexNormal;
out vec3 viewPos;
out vec3 localPos; // 用于扫描线效果

void main() {
    localPos = Position;

    vec4 viewPositionVec4 = ModelViewMat * vec4(Position, 1.0);
    viewPos = viewPositionVec4.xyz;

    gl_Position = ProjMat * viewPositionVec4;

    vertexColor = Color;
    vertexNormal = mat3(ModelViewMat) * Normal;
}