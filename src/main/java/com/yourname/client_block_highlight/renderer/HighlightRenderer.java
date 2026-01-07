package com.yourname.client_block_highlight.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yourname.client_block_highlight.config.ClientConfig;
import com.yourname.client_block_highlight.util.BlockColorManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HighlightRenderer {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        if (!ClientConfig.ENABLED.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Update shader uniforms before binding any context using the custom shader
        ShaderManager.updateShaderUniforms();

        Level level = mc.level;
        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = camera.getPosition();
        
        poseStack.pushPose();
        
        // Translate to camera position for rendering offsets
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        
        // Setup for rendering outlines (RenderType.LINES handles depth/blend state management often)
        RenderSystem.disableDepthTest(); // Temporarily disable for the outline pass for visibility, although custom RenderType may re-enable
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer outlineConsumer = bufferSource.getBuffer(RenderType.lines());
        VertexConsumer highlightConsumer = bufferSource.getBuffer(ShaderManager.BLOCK_HIGHLIGHT_TRANSLUCENT);

        // Define the area to search
        int renderDistance = ClientConfig.RENDER_DISTANCE.get();
        BlockPos playerPos = mc.player.blockPosition();

        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();
        
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int y = -renderDistance; y <= renderDistance; y++) {
                for (int z = -renderDistance; z <= renderDistance; z++) {
                    searchPos.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);

                    // Simple bounds check to prevent excessive checking outside vertical range
                    if (searchPos.getY() < level.getMinBuildHeight() || searchPos.getY() >= level.getMaxBuildHeight()) continue;

                    if (!level.isLoaded(searchPos)) continue;
                    
                    BlockState state = level.getBlockState(searchPos);

                    Integer colorInt = BlockColorManager.getColorForBlock(state);

                    if (colorInt != null) {
                        float r = (float) ((colorInt >> 16) & 0xFF) / 255.0F;
                        float g = (float) ((colorInt >> 8) & 0xFF) / 255.0F;
                        float b = (float) (colorInt & 0xFF) / 255.0F;
                        // High base alpha for impact, shader handles dynamic pulsing
                        float alpha = 0.5F;
                        
                        AABB box = state.getShape(level, searchPos).bounds().move(searchPos);

                        // 1. Draw Outline (using RenderType.LINES)
                        LevelRenderer.renderLineBox(
                            poseStack, outlineConsumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ,
                            r, g, b, 1.0F
                        );
                        
                        // 2. Draw Filled Box (using custom shader RenderType)
                        renderFilledBoxQuads(poseStack, highlightConsumer, box, r, g, b, alpha);
                    }
                }
            }
        }

        // Finish rendering batches
        bufferSource.endBatch(RenderType.lines());
        bufferSource.endBatch(ShaderManager.BLOCK_HIGHLIGHT_TRANSLUCENT);
        
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
    
    // Helper function to draw a filled cuboid using quads
    private static void renderFilledBoxQuads(PoseStack poseStack, VertexConsumer consumer, AABB box, float r, float g, float b, float alpha) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        
        final var matrix = poseStack.last().pose();

        // 1. Bottom Face (-Y)
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).endVertex();

        // 2. Top Face (+Y)
        consumer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).endVertex();

        // 3. North Face (-Z)
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).endVertex();

        // 4. South Face (+Z)
        consumer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).endVertex();

        // 5. West Face (-X)
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).endVertex();

        // 6. East Face (+X)
        consumer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).endVertex();
    }
}