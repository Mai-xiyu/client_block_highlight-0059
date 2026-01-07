package com.yourname.client_block_highlight.util;

import com.yourname.client_block_highlight.config.ClientConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockColorManager {
    private static Map<ResourceLocation, Integer> BLOCK_SPECIFIC_COLORS = new HashMap<>();
    private static Map<TagKey<Block>, Integer> TAG_COLORS = new HashMap<>();

    @SubscribeEvent
    public static void onConfigLoading(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ClientConfig.CLIENT_SPEC) {
            loadConfig();
        }
    }

    @SubscribeEvent
    public static void onConfigReloading(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ClientConfig.CLIENT_SPEC) {
            loadConfig();
        }
    }

    // Called when config loads or reloads
    private static void loadConfig() {
        BLOCK_SPECIFIC_COLORS.clear();
        TAG_COLORS.clear();

        // 1. Load Block Specific Colors (Higher Priority)
        for (String entry : ClientConfig.MINERAL_COLORS.get()) {
            try {
                String[] parts = entry.split("\\|");
                ResourceLocation blockId = new ResourceLocation(parts[0]);
                Integer color = ClientConfig.parseColorHex(parts[1]);
                BLOCK_SPECIFIC_COLORS.put(blockId, color);
            } catch (Exception e) {
                // Ignored parsing error
            }
        }

        // 2. Load Tag Colors (Lower Priority)
        for (String entry : ClientConfig.TAG_COLORS.get()) {
            try {
                String[] parts = entry.split("\\|");
                ResourceLocation tagId = new ResourceLocation(parts[0]);
                Integer color = ClientConfig.parseColorHex(parts[1]);
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, tagId);
                TAG_COLORS.put(tag, color);
            } catch (Exception e) {
                // Ignored parsing error
            }
        }
    }

    /**
     * Finds the highlight color for a given block state.
     * Specific block IDs override block tags.
     * @return RRGGBB integer color, or null if no highlight rule matches.
     */
    public static Integer getColorForBlock(BlockState state) {
        Block block = state.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);

        // Check 1: Specific block ID matching
        if (BLOCK_SPECIFIC_COLORS.containsKey(blockId)) {
            return BLOCK_SPECIFIC_COLORS.get(blockId);
        }

        // Check 2: Tag matching
        for (Map.Entry<TagKey<Block>, Integer> entry : TAG_COLORS.entrySet()) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}