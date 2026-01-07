#version 150

uniform float U_GameTime;
uniform vec2 ScreenSize;
uniform sampler2D MainDepthSampler;
uniform mat4 U_InverseProjectionMatrix;
uniform mat4 U_InverseViewMatrix;
uniform vec3 U_CameraPosition;

in vec2 texCoord;
out vec4 fragColor;

vec3 clipToView(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = U_InverseProjectionMatrix * clipPos;
    return viewPos.xyz / viewPos.w;
}

void main() {
    float rawDepth = texture(MainDepthSampler, texCoord).r;

    // 如果是极远处（天空），我们可以选择不渲染或者给予特殊的扫描效果
    // 这里简单处理：重建坐标
    vec3 viewPos = clipToView(texCoord, rawDepth);
    float dist = length(viewPos);

    // --- 扫描波逻辑 ---
    // U_GameTime 在这里代表“动画开始后的秒数”
    // 波浪速度：100格/秒
    float waveRadius = U_GameTime * 100.0;
    float waveWidth = 20.0; // 波浪宽度

    // 计算当前像素距离波浪中心的距离
    float distToWave = abs(dist - waveRadius);

    float alpha = 0.0;
    vec3 color = vec3(0.4, 1.0, 0.8); // 青色扫描波

    // 只有在波浪范围内才显示
    if (distToWave < waveWidth) {
        // 距离越近越亮
        float intensity = 1.0 - (distToWave / waveWidth);
        intensity = pow(intensity, 2.0); // 让边缘柔和
        alpha = intensity * 0.5; // 最大透明度 0.5
    }

    // 超过一定时间或距离后淡出
    if (U_GameTime > 3.0) alpha = 0.0;

    fragColor = vec4(color, alpha);
}