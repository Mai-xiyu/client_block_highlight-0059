package com.yourname.client_block_highlight.config;

import com.yourname.client_block_highlight.renderer.ShaderManager;
import com.yourname.client_block_highlight.renderer.SynthwaveRenderer;
import com.yourname.client_block_highlight.util.BlockColorManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ClientConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static ForgeConfigSpec.BooleanValue ENABLED;
    public static ForgeConfigSpec.IntValue RENDER_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> MINERAL_COLORS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> TAG_COLORS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        ENABLED = builder
                .comment("Enable/Disable the Block Highlight ESP feature.")
                .define("enabled", true);

        RENDER_DISTANCE = builder
                .comment("Maximum distance in blocks to search.")
                .defineInRange("renderDistance", 32, 16, 128);

        // 添加了 Deepslate 变种
        List<String> defaultMinerals = List.of(
                ForgeRegistries.BLOCKS.getKey(Blocks.COAL_ORE).toString() + "|333333",
                ForgeRegistries.BLOCKS.getKey(Blocks.DEEPSLATE_COAL_ORE).toString() + "|333333",

                ForgeRegistries.BLOCKS.getKey(Blocks.COPPER_ORE).toString() + "|C35A25",
                ForgeRegistries.BLOCKS.getKey(Blocks.DEEPSLATE_COPPER_ORE).toString() + "|C35A25",

                ForgeRegistries.BLOCKS.getKey(Blocks.IRON_ORE).toString() + "|AAAAAA",
                ForgeRegistries.BLOCKS.getKey(Blocks.DEEPSLATE_IRON_ORE).toString() + "|AAAAAA",

                ForgeRegistries.BLOCKS.getKey(Blocks.LAPIS_ORE).toString() + "|1A3385",
                ForgeRegistries.BLOCKS.getKey(Blocks.DEEPSLATE_LAPIS_ORE).toString() + "|1A3385",

                ForgeRegistries.BLOCKS.getKey(Blocks.REDSTONE_ORE).toString() + "|AA0000",
                ForgeRegistries.BLOCKS.getKey(Blocks.DEEPSLATE_REDSTONE_ORE).toString() + "|AA0000",

                ForgeRegistries.BLOCKS.getKey(Blocks.GOLD_ORE).toString() + "|FFD700",
                ForgeRegistries.BLOCKS.getKey(Blocks.DEEPSLATE_GOLD_ORE).toString() + "|FFD700",

                ForgeRegistries.BLOCKS.getKey(Blocks.EMERALD_ORE).toString() + "|00AA00",
                ForgeRegistries.BLOCKS.getKey(Blocks.DEEPSLATE_EMERALD_ORE).toString() + "|00AA00",

                ForgeRegistries.BLOCKS.getKey(Blocks.DIAMOND_ORE).toString() + "|00FFFF",
                ForgeRegistries.BLOCKS.getKey(Blocks.DEEPSLATE_DIAMOND_ORE).toString() + "|00FFFF",

                ForgeRegistries.BLOCKS.getKey(Blocks.ANCIENT_DEBRIS).toString() + "|6F4A0C"
        );

        MINERAL_COLORS = builder
                .comment("List of specific blocks to highlight (Block ID | RRGGBB).")
                .defineList("mineralHighlights", defaultMinerals, obj -> obj instanceof String && ((String) obj).contains("|") && ((String) obj).split("\\|")[1].length() == 6);

        List<String> defaultTags = List.of(
                "minecraft:needs_iron_tool|FF00FF"
        );

        TAG_COLORS = builder
                .comment("List of Block Tags to highlight.")
                .defineList("tagHighlights", defaultTags, obj -> obj instanceof String && ((String) obj).contains("|") && ((String) obj).split("\\|")[1].length() == 6);

        CLIENT_SPEC = builder.build();
    }

    public static Integer parseColorHex(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    public static void setBlockColor(String blockId, int color) {
        List<String> currentList = new java.util.ArrayList<>(MINERAL_COLORS.get());
        currentList.removeIf(entry -> entry.startsWith(blockId + "|"));
        String hex = String.format("%06X", (0xFFFFFF & color));
        currentList.add(blockId + "|" + hex);
        MINERAL_COLORS.set(currentList);

        BlockColorManager.loadConfig();
        ShaderManager.triggerUpdateAnimation();
        SynthwaveRenderer.triggerScan();
    }

    public static void removeBlockColor(String blockId) {
        List<String> currentList = new java.util.ArrayList<>(MINERAL_COLORS.get());
        boolean changed = currentList.removeIf(entry -> entry.startsWith(blockId + "|"));
        if (changed) {
            MINERAL_COLORS.set(currentList);
            BlockColorManager.loadConfig();
            ShaderManager.triggerUpdateAnimation();
            // 新增：触发全屏扫描
            SynthwaveRenderer.triggerScan();
        }
    }
}