package com.yourname.client_block_highlight.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.yourname.client_block_highlight.ClientBlockHighlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.io.IOException;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ClientBlockHighlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SynthwaveRenderer {

    private static ShaderInstance synthwaveShader;
    private static long animationStartTime = 0L;
    private static boolean isAnimating = false;

    // 供外部调用：触发全屏扫描
    public static void triggerScan() {
        animationStartTime = System.currentTimeMillis();
        isAnimating = true;
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(
                event.getResourceProvider(),
                new ResourceLocation(ClientBlockHighlight.MOD_ID, "synthwave"),
                DefaultVertexFormat.POSITION), shader -> synthwaveShader = shader);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ClientBlockHighlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onRenderWorld(RenderLevelStageEvent event) {
            // 只在动画激活时渲染
            if (!isAnimating) return;

            // 3秒后自动停止渲染，节省性能
            float timeSeconds = (float)(System.currentTimeMillis() - animationStartTime) / 1000.0F;
            if (timeSeconds > 3.0F) {
                isAnimating = false;
                return;
            }

            // 在半透明物体（水、玻璃）之后渲染，确保覆盖所有物体
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || synthwaveShader == null) return;

            // 1. 设置状态
            RenderSystem.backupProjectionMatrix();
            RenderSystem.disableDepthTest(); // 禁用深度测试，直接覆盖在屏幕上
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            // 2. 绑定 Shader 和 Uniforms
            RenderSystem.setShader(() -> synthwaveShader);

            int depthTextureId = mc.getMainRenderTarget().getDepthTextureId();
            synthwaveShader.setSampler("MainDepthSampler", depthTextureId);

            Matrix4f projectionMatrix = event.getProjectionMatrix();
            Matrix4f modelViewMatrix = event.getProjectionMatrix();

            Matrix4f inverseProj = new Matrix4f(projectionMatrix).invert();
            Matrix4f inverseModelView = new Matrix4f(modelViewMatrix).invert();

            if (synthwaveShader.getUniform("U_InverseProjectionMatrix") != null)
                synthwaveShader.getUniform("U_InverseProjectionMatrix").set(inverseProj);
            if (synthwaveShader.getUniform("U_InverseViewMatrix") != null)
                synthwaveShader.getUniform("U_InverseViewMatrix").set(inverseModelView);

            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            if (synthwaveShader.getUniform("U_CameraPosition") != null)
                synthwaveShader.getUniform("U_CameraPosition").set((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);

            if (synthwaveShader.getUniform("U_GameTime") != null) {
                synthwaveShader.getUniform("U_GameTime").set(timeSeconds);
            }

            if (synthwaveShader.getUniform("ScreenSize") != null) {
                synthwaveShader.getUniform("ScreenSize").set((float)mc.getWindow().getWidth(), (float)mc.getWindow().getHeight());
            }

            // 3. 绘制全屏四边形
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(-1.0D, 1.0D, 0.0D).endVertex();
            bufferbuilder.vertex(-1.0D, -1.0D, 0.0D).endVertex();
            bufferbuilder.vertex(1.0D, -1.0D, 0.0D).endVertex();
            bufferbuilder.vertex(1.0D, 1.0D, 0.0D).endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.restoreProjectionMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
        }
    }
}