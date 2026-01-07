package com.yourname.client_block_highlight.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.yourname.client_block_highlight.ClientBlockHighlight;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.OptionalDouble;

@Mod.EventBusSubscriber(modid = ClientBlockHighlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ShaderManager {
    private static ShaderInstance highlightShader;
    private static float lastUpdateTime = -1000.0f;

    private static float getShaderTime() {
        return (float) (System.currentTimeMillis() % 3600000) / 1000.0F;
    }

    public static void triggerUpdateAnimation() {
        lastUpdateTime = getShaderTime();
    }

    private static class CustomRenderType extends RenderType {
        private CustomRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        // 1. 填充块 (透视 + 扫描线着色器)
        public static final RenderType BLOCK_HIGHLIGHT_TRANSLUCENT = RenderType.create(
                "block_highlight_translucent",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> highlightShader))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setDepthTestState(NO_DEPTH_TEST) // 穿墙关键
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)
        );

        // 2. 线条 (透视 + 原版着色器 + 必须带法线)
        public static final RenderType BLOCK_HIGHLIGHT_LINE = RenderType.create(
                "block_highlight_line",
                DefaultVertexFormat.POSITION_COLOR_NORMAL, // <--- 修复：改为 NORMAL
                VertexFormat.Mode.LINES,
                256,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0D)))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setDepthTestState(NO_DEPTH_TEST) // 穿墙关键
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)
        );
    }

    public static final RenderType BLOCK_HIGHLIGHT_TRANSLUCENT = CustomRenderType.BLOCK_HIGHLIGHT_TRANSLUCENT;
    public static final RenderType BLOCK_HIGHLIGHT_LINE = CustomRenderType.BLOCK_HIGHLIGHT_LINE;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation(ClientBlockHighlight.MOD_ID, "highlight_block"),
                        DefaultVertexFormat.POSITION_COLOR_NORMAL),
                shaderInstance -> highlightShader = shaderInstance
        );
    }

    public static void updateShaderUniforms() {
        if (highlightShader == null) return;

        float currentTime = getShaderTime();

        if (highlightShader.getUniform("GameTime") != null) {
            highlightShader.getUniform("GameTime").set(currentTime);
        }

        if (highlightShader.getUniform("LastUpdateTime") != null) {
            highlightShader.getUniform("LastUpdateTime").set(lastUpdateTime);
        }
    }
}