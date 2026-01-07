#version 150

in vec4 fragColor;

out vec4 FinalColor;

void main() {
    // Use the interpolated color directly
    FinalColor = fragColor;
}