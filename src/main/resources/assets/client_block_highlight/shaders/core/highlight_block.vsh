#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;

out vec4 fragColor;

void main() {
    // Subtle vertical pulsation enhancement
    float offset = sin(GameTime * 5.0) * 0.005; 
    
    vec3 animatedPosition = Position;
    
    gl_Position = ProjMat * ModelViewMat * vec4(animatedPosition, 1.0);
    
    // Pulsating brightness/alpha for visual impact (0.8 to 1.0)
    float pulsatingAlpha = 0.9 + sin(GameTime * 8.0) * 0.1;
    fragColor = vec4(Color.rgb, Color.a * pulsatingAlpha);
}