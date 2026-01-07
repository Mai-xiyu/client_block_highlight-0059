package com.yourname.client_block_highlight.gui;

import com.yourname.client_block_highlight.ClientBlockHighlight;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientBlockHighlightConfigScreen extends Screen {
    private final Screen parentScreen;
    
    private EditBox searchBox;
    private BlockSelectorList blockList;
    
    public ClientBlockHighlightConfigScreen(Screen parentScreen) {
        super(Component.translatable("gui." + ClientBlockHighlight.MOD_ID + ".config.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int yOffset = 30;

        // 1. Search Box
        // Width 300, centered above the list
        this.searchBox = new EditBox(this.font, centerX - 150, yOffset, 300, 20, Component.translatable("gui.search"));
        this.searchBox.setHint(Component.translatable("搜索方块名称或ID..."));
        this.searchBox.setMaxLength(50);
        this.searchBox.setValue("");
        this.searchBox.setResponder(this::updateSearch);
        this.addWidget(this.searchBox);
        
        // 2. Block List Widget (Starts 24 pixels below search box, ends 40 pixels from bottom)
        this.blockList = new BlockSelectorList(this.minecraft, this.width, this.height, yOffset + 24, this.height - 40, 36, this.font);
        this.addWidget(this.blockList);
        
        // Load all blocks (can be slow, but necessary for comprehensive search)
        this.blockList.reloadList();
        
        // 3. Done Button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            // TODO: Save any temporary config changes
            this.minecraft.setScreen(this.parentScreen);
        }).bounds(centerX - 100, this.height - 30, buttonWidth, buttonHeight).build());
    }
    
    private void updateSearch(String filter) {
        this.blockList.filter(filter);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        
        // Render list first so controls overlay it
        this.blockList.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        // Render title and controls
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
