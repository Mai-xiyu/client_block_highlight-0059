package com.yourname.client_block_highlight.gui;

import com.yourname.client_block_highlight.util.BlockColorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class BlockSelectorList extends AbstractSelectionList<BlockSelectorList.BlockEntry> {
    
    private final Font font;
    private final List<BlockEntry> allEntries = new ArrayList<>();
    
    public BlockSelectorList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, Font font) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.font = font;
        // Ensure the list width is suitable for the content (320px wide centered)
        this.x0 = (width / 2) - 160;
        this.x1 = (width / 2) + 160;
        this.setRenderBackground(false);
    }

    public void reloadList() {
        allEntries.clear();
        this.clearEntries();

        // Load all registered blocks
        ForgeRegistries.BLOCKS.getValues().stream()
            .filter(block -> block.asItem() instanceof BlockItem) // Only blocks that have items (for visual preview)
            .forEach(block -> allEntries.add(new BlockEntry(this, block)));
        
        // Initial population
        this.replaceEntries(allEntries);
    }
    
    /**
     * Implements multi-dimensional fuzzy search logic based on localized name or resource location ID.
     */
    public void filter(String filter) {
        this.clearEntries();
        String lowerFilter = filter.toLowerCase(Locale.ROOT);
        
        if (lowerFilter.isBlank()) {
            this.replaceEntries(allEntries);
            return;
        }

        List<BlockEntry> filtered = allEntries.stream()
            .filter(entry -> 
                entry.getLocalizedName().getString().toLowerCase(Locale.ROOT).contains(lowerFilter) ||
                entry.getId().toString().toLowerCase(Locale.ROOT).contains(lowerFilter)
            )
            .toList();
            
        this.replaceEntries(filtered);
    }
    
    @Override
    public int getRowWidth() {
        return 320; 
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 + 6;
    }

    
    // --- Block Entry Class ---
    
    @OnlyIn(Dist.CLIENT)
    public class BlockEntry extends AbstractSelectionList.Entry<BlockEntry> {
        private final BlockSelectorList parent;
        private final Block block;
        private final ResourceLocation id;
        private final ItemStack itemStack;
        private final Component localizedName;
        
        private final int configuredColor; // RRGGBB

        public BlockEntry(BlockSelectorList parent, Block block) {
            this.parent = parent;
            this.block = block;
            this.id = ForgeRegistries.BLOCKS.getKey(block);
            this.itemStack = block.asItem().getDefaultInstance();
            this.localizedName = block.getName();
            
            // Check config manager for existing highlight color
            Integer color = BlockColorManager.getColorForBlock(block.defaultBlockState());
            this.configuredColor = color != null ? color : -1; // -1 indicates not configured
        }
        
        public ResourceLocation getId() { return id; }
        public Component getLocalizedName() { return localizedName; }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.localizedName);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            
            // Calculate time offset for fluid animations
            float time = (mc.level != null ? mc.level.getTime() : 0) + partialTick;
            
            // Dynamic hover/selection effect (simulating fluid transition)
            float scaleAdjustment = 1.0F;
            int textAlpha = 0xFF;

            if (isMouseOver) {
                scaleAdjustment = 1.0F + Mth.sin(time / 5.0F) * 0.02F; // Subtle pulsation on hover
                guiGraphics.fill(left, top, left + width, top + height, 0x20FFFFFF); // Soft white background highlight
            }

            // Vertical centering offsets
            int itemX = left + 4;
            int itemY = top + 2;
            int textX = left + 35;
            int textY_Top = top + 5;
            int textY_Bottom = top + 18;

            // 1. Block Preview (Item Icon as proxy for 3D visual)
            guiGraphics.pose().pushPose();
            
            // Apply dynamic scaling based on hover/time
            float scaledSize = 1.5F * scaleAdjustment;
            guiGraphics.pose().translate(itemX + 12 * (1 - scaleAdjustment), itemY + 12 * (1 - scaleAdjustment), 0); // Center translation adjustment
            guiGraphics.pose().scale(scaledSize, scaledSize, scaledSize); 
            
            mc.getItemRenderer().renderAndDecorateItem(itemStack, 0, 0);
            
            guiGraphics.pose().popPose();

            // 2. Localized Name (Right side, Primary)
            int primaryColor = 0xFFFFFF | (textAlpha << 24);
            guiGraphics.drawString(parent.font, this.localizedName, textX, textY_Top, primaryColor, false);

            // 3. Resource ID (Secondary, lower, grayed out)
            int secondaryColor = 0x808080 | (textAlpha << 24);
            guiGraphics.drawString(parent.font, this.id.toString(), textX, textY_Bottom, secondaryColor, false);
            
            // 4. Highlight Indicator / Configuration Status (Right edge)
            if (configuredColor != -1) {
                // Draw a colored square indicating the configured color
                int colorX = left + width - 15; 
                int colorY = top + 10;
                // Add alpha to the configured RRGGBB color for rendering (using 0xFF alpha)
                guiGraphics.fill(colorX, colorY, colorX + 10, colorY + 10, 0xFF000000 | configuredColor);
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // TODO: Implement logic to select block for configuration/color setting
            if (button == 0) {
                System.out.println("Selected block for configuration: " + id);
                // Returning true consumes the click
                return true;
            }
            return false;
        }
    }
}