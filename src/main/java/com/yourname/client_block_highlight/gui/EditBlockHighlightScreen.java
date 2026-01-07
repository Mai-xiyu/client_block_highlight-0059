package com.yourname.client_block_highlight.gui;

import com.yourname.client_block_highlight.config.ClientConfig;
import com.yourname.client_block_highlight.util.BlockColorManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditBlockHighlightScreen extends Screen {
    private final Screen parentScreen;
    private final Block block;
    private final ResourceLocation blockId;

    private EditBox colorEditBox;
    private int currentColor;

    public EditBlockHighlightScreen(Screen parentScreen, Block block, ResourceLocation blockId) {
        super(Component.translatable("gui.client_block_highlight.edit.title", block.getName()));
        this.parentScreen = parentScreen;
        this.block = block;
        this.blockId = blockId;

        // 获取当前颜色，如果没有配置则默认白色
        Integer existingColor = BlockColorManager.getColorForBlock(block.defaultBlockState());
        this.currentColor = existingColor != null ? existingColor : 0xFFFFFF;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 1. 颜色输入框
        this.colorEditBox = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.literal("Color Hex"));
        this.colorEditBox.setMaxLength(6);
        // 填入当前颜色的 Hex 值
        this.colorEditBox.setValue(String.format("%06X", this.currentColor));
        this.colorEditBox.setResponder(this::onColorChanged);
        this.addWidget(this.colorEditBox);

        // 2. 保存按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            save();
            this.minecraft.setScreen(this.parentScreen);
        }).bounds(centerX - 105, centerY + 20, 100, 20).build());

        // 3. 取消/返回按钮
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (btn) -> {
            this.minecraft.setScreen(this.parentScreen);
        }).bounds(centerX + 5, centerY + 20, 100, 20).build());

        // 4. 删除/重置按钮
        this.addRenderableWidget(Button.builder(Component.literal("清除配置"), (btn) -> {
            ClientConfig.removeBlockColor(this.blockId.toString());
            this.minecraft.setScreen(this.parentScreen);
        }).bounds(centerX - 50, centerY + 50, 100, 20).build());
    }

    private void onColorChanged(String newHex) {
        try {
            this.currentColor = Integer.parseInt(newHex, 16);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void save() {
        ClientConfig.setBlockColor(this.blockId.toString(), this.currentColor);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 0xFFFFFF);

        // 说明文字
        guiGraphics.drawCenteredString(this.font, "输入十六进制颜色 (例如: FF0000 为红色)", this.width / 2, this.height / 2 - 40, 0xAAAAAA);

        // 渲染输入框
        this.colorEditBox.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 颜色预览方块
        int previewSize = 20;
        int previewX = this.width / 2 + 110;
        int previewY = this.height / 2 - 20;

        // 绘制边框
        guiGraphics.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, 0xFFAAAAAA);
        // 绘制颜色 (不透明)
        guiGraphics.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | this.currentColor);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}