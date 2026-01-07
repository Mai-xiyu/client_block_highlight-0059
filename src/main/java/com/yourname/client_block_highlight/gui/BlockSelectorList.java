package com.yourname.client_block_highlight.gui;

import com.yourname.client_block_highlight.util.BlockColorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents; // 新增导入
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

    @Override
    public void updateNarration(NarrationElementOutput output) {
        if (this.getSelected() != null) {
            this.getSelected().updateNarration(output);
        }
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

            Integer color = BlockColorManager.getColorForBlock(block.defaultBlockState());
            this.configuredColor = color != null ? color : -1;
        }

        public ResourceLocation getId() { return id; }
        public Component getLocalizedName() { return localizedName; }

        public void updateNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, Component.translatable("narrator.select", this.localizedName));
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            float time = (mc.level != null ? (float) mc.level.getGameTime() : 0) + partialTick;

            float scaleAdjustment = 1.0F;
            int textAlpha = 0xFF;

            if (isMouseOver) {
                scaleAdjustment = 1.0F + Mth.sin(time / 5.0F) * 0.02F;
                guiGraphics.fill(left, top, left + width, top + height, 0x20FFFFFF);
            }

            int itemX = left + 4;
            int itemY = top + 2;
            int textX = left + 35;
            int textY_Top = top + 5;
            int textY_Bottom = top + 18;

            guiGraphics.pose().pushPose();

            float scaledSize = 1.5F * scaleAdjustment;
            guiGraphics.pose().translate(itemX + 12 * (1 - scaleAdjustment), itemY + 12 * (1 - scaleAdjustment), 0);
            guiGraphics.pose().scale(scaledSize, scaledSize, scaledSize);

            guiGraphics.renderItem(itemStack, 0, 0);

            guiGraphics.pose().popPose();

            int primaryColor = 0xFFFFFF | (textAlpha << 24);
            guiGraphics.drawString(parent.font, this.localizedName, textX, textY_Top, primaryColor, false);

            int secondaryColor = 0x808080 | (textAlpha << 24);
            guiGraphics.drawString(parent.font, this.id.toString(), textX, textY_Bottom, secondaryColor, false);

            if (configuredColor != -1) {
                int colorX = left + width - 15;
                int colorY = top + 10;
                guiGraphics.fill(colorX, colorY, colorX + 10, colorY + 10, 0xFF000000 | configuredColor);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                // 修复：直接使用 Minecraft.getInstance().screen 获取当前屏幕作为父屏幕
                Minecraft.getInstance().setScreen(new EditBlockHighlightScreen(Minecraft.getInstance().screen, this.block, this.id));

                // 播放点击音效
                Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }
}