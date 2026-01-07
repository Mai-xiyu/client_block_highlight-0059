package com.yourname.client_block_highlight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yourname.client_block_highlight.config.ClientConfig;
import com.yourname.client_block_highlight.util.BlockColorManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HighlightRenderer {

    // 缓存对象，避免重复计算
    private static class RenderTarget {
        final BlockPos pos;
        final AABB box;
        final float r, g, b;

        RenderTarget(BlockPos pos, AABB box, float r, float g, float b) {
            this.pos = pos;
            this.box = box;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (!ClientConfig.ENABLED.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ShaderManager.updateShaderUniforms();

        Level level = mc.level;
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        BlockPos playerPos = mc.player.blockPosition();
        int renderDistance = ClientConfig.RENDER_DISTANCE.get();

        List<RenderTarget> targets = new ArrayList<>();
        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();

        // 1. 收集阶段
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int y = -renderDistance; y <= renderDistance; y++) {
                for (int z = -renderDistance; z <= renderDistance; z++) {
                    searchPos.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                    if (searchPos.getY() < level.getMinBuildHeight() || searchPos.getY() >= level.getMaxBuildHeight()) continue;
                    if (!level.isLoaded(searchPos)) continue;

                    BlockState state = level.getBlockState(searchPos);
                    Integer colorInt = BlockColorManager.getColorForBlock(state);

                    if (colorInt != null) {
                        float r = ((colorInt >> 16) & 0xFF) / 255.0F;
                        float g = ((colorInt >> 8) & 0xFF) / 255.0F;
                        float b = (colorInt & 0xFF) / 255.0F;

                        AABB box = state.getShape(level, searchPos).bounds();
                        if (box.getSize() < 0.01) continue;

                        targets.add(new RenderTarget(searchPos.immutable(), box, r, g, b));
                    }
                }
            }
        }

        if (targets.isEmpty()) return;

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        // 2. 绘制阶段 - Pass 1: 填充 (Fill)
        VertexConsumer highlightConsumer = bufferSource.getBuffer(ShaderManager.BLOCK_HIGHLIGHT_TRANSLUCENT);
        for (RenderTarget target : targets) {
            poseStack.pushPose();
            poseStack.translate(target.pos.getX() - cameraPos.x, target.pos.getY() - cameraPos.y, target.pos.getZ() - cameraPos.z);
            renderFilledBoxQuads(poseStack, highlightConsumer, target.box, target.r, target.g, target.b, 1.0F);
            poseStack.popPose();
        }
        bufferSource.endBatch(ShaderManager.BLOCK_HIGHLIGHT_TRANSLUCENT);

        // 3. 绘制阶段 - Pass 2: 线条 (Lines)
        VertexConsumer outlineConsumer = bufferSource.getBuffer(ShaderManager.BLOCK_HIGHLIGHT_LINE);
        for (RenderTarget target : targets) {
            poseStack.pushPose();
            poseStack.translate(target.pos.getX() - cameraPos.x, target.pos.getY() - cameraPos.y, target.pos.getZ() - cameraPos.z);
            LevelRenderer.renderLineBox(poseStack, outlineConsumer,
                    target.box.minX, target.box.minY, target.box.minZ,
                    target.box.maxX, target.box.maxY, target.box.maxZ,
                    target.r, target.g, target.b, 1.0F);
            poseStack.popPose();
        }
        bufferSource.endBatch(ShaderManager.BLOCK_HIGHLIGHT_LINE);
    }

    private static void renderFilledBoxQuads(PoseStack poseStack, VertexConsumer consumer, AABB box, float r, float g, float b, float alpha) {
        final var matrix = poseStack.last().pose();
        final var normal = poseStack.last().normal();

        float minX = (float)box.minX; float minY = (float)box.minY; float minZ = (float)box.minZ;
        float maxX = (float)box.maxX; float maxY = (float)box.maxY; float maxZ = (float)box.maxZ;

        // 显式写入法线 (Normal) 以配合 Shader
        // 下 (-Y)
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha).normal(normal, 0, -1, 0).endVertex();
        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha).normal(normal, 0, -1, 0).endVertex();
        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha).normal(normal, 0, -1, 0).endVertex();
        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha).normal(normal, 0, -1, 0).endVertex();
        // 上 (+Y)
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha).normal(normal, 0, 1, 0).endVertex();
        // 北 (-Z)
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha).normal(normal, 0, 0, -1).endVertex();
        // 南 (+Z)
        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha).normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha).normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha).normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha).normal(normal, 0, 0, 1).endVertex();
        // 西 (-X)
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha).normal(normal, -1, 0, 0).endVertex();
        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha).normal(normal, -1, 0, 0).endVertex();
        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha).normal(normal, -1, 0, 0).endVertex();
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha).normal(normal, -1, 0, 0).endVertex();
        // 东 (+X)
        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha).normal(normal, 1, 0, 0).endVertex();
        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha).normal(normal, 1, 0, 0).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha).normal(normal, 1, 0, 0).endVertex();
        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha).normal(normal, 1, 0, 0).endVertex();
    }
}