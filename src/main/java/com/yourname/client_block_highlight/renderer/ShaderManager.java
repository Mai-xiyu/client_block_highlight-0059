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

@Mod.EventBusSubscriber(modid = ClientBlockHighlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ShaderManager {
    private static ShaderInstance highlightShader;
    
    // Define custom RenderType using the shader
    public static final RenderType BLOCK_HIGHLIGHT_TRANSLUCENT = RenderType.create(
            "block_highlight_translucent", 
            DefaultVertexFormat.POSITION_COLOR, 
            VertexFormat.Mode.QUADS, 
            256, 
            true, // Use query
            true, // Use sort
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> highlightShader))
                    // FIX: Use public static final fields suffixed with _STATE
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY_STATE)
                    .setCullState(RenderStateShard.NO_CULL_STATE)
                    // Use LEQUAL depth test to allow depth sorting but prevent Z-fighting artifacts
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST_STATE)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE_STATE)
                    .createCompositeState(true)
    );

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(), 
                        new ResourceLocation(ClientBlockHighlight.MOD_ID, "highlight_block"), 
                        DefaultVertexFormat.POSITION_COLOR),
                shaderInstance -> highlightShader = shaderInstance
        );
    }

    public static void updateShaderUniforms() {
        if (highlightShader == null) return;
        
        // Update GameTime for dynamic effects in VSH
        if (highlightShader.getUniform("GameTime") != null) {
            float gameTime = (float) (System.currentTimeMillis() % 100000) / 1000.0F;
            highlightShader.getUniform("GameTime").set(gameTime);
        }
        // Note: ModelViewMat and ProjMat are typically managed by the rendering pipeline for this RenderType.
    }
}
