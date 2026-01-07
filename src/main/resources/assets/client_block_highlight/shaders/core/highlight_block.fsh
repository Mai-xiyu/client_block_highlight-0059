#version 150

in vec4 vertexColor;
in vec3 vertexNormal;
in vec3 viewPos;
in vec3 localPos;

uniform float GameTime;
uniform float LastUpdateTime; // 新增：上次更新时间

out vec4 fragColor;

void main() {
    vec3 N = normalize(vertexNormal);
    vec3 V = normalize(-viewPos);

    // --- 1. 基础效果 (扫描线 + Fresnel) ---
    float scanSpeed = 3.0;
    float scan = sin((localPos.y * 10.0) - (GameTime * scanSpeed));
    float scanFactor = smoothstep(0.4, 0.6, scan);

    float baseFresnel = 1.0 - max(0.0, dot(N, V));
    float rim = pow(baseFresnel, 2.0);

    // --- 2. 更新冲击波 (Update Shockwave) ---
    // 计算距离上次更新过去了多久
    float timeSinceUpdate = GameTime - LastUpdateTime;
    float waveIntensity = 0.0;

    // 如果在 2.5 秒内，则显示波浪
    if (timeSinceUpdate >= 0.0 && timeSinceUpdate < 2.5) {
        // 计算当前像素距离相机的距离 (0 ~ RenderDistance)
        float dist = length(viewPos);

        // 波浪扩散速度 (例如 50 格/秒)
        float waveSpeed = 50.0;
        // 当前波浪的半径
        float currentRadius = timeSinceUpdate * waveSpeed;

        // 计算波浪的宽度 (5格宽)
        float distToWave = abs(dist - currentRadius);
        // 如果距离波浪中心小于 5 格，则产生高亮 (距离越近越亮)
        if (distToWave < 5.0) {
            waveIntensity = 1.0 - (distToWave / 5.0);
            waveIntensity = pow(waveIntensity, 2.0); // 让波峰更尖锐
        }
    }

    // --- 3. 组合颜色 ---
    vec3 baseRGB = vertexColor.rgb;
    vec3 scanColor = mix(baseRGB, vec3(1.0), 0.7) * scanFactor;

    // 基础颜色 + 边缘光
    vec3 finalRGB = baseRGB + scanColor + (rim * baseRGB * 0.8);

    // 叠加冲击波 (纯白色高亮)
    finalRGB += vec3(1.0, 1.0, 1.0) * waveIntensity * 0.8;

    // --- 4. Alpha 计算 ---
    float glowAlpha = max(scanFactor * 0.8, rim * 0.8);
    // 冲击波区域不透明度增加
    glowAlpha = max(glowAlpha, waveIntensity);

    float finalAlpha = clamp(0.3 + glowAlpha, 0.3, 0.95);

    fragColor = vec4(finalRGB, finalAlpha);
}