package com.yourname.client_block_highlight.config;

import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ClientConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static ForgeConfigSpec.BooleanValue ENABLED;
    public static ForgeConfigSpec.IntValue RENDER_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<List<String>> MINERAL_COLORS;
    public static ForgeConfigSpec.ConfigValue<List<String>> TAG_COLORS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        ENABLED = builder
                .comment("Enable/Disable the Block Highlight ESP feature. (启用/禁用方块高亮功能)")
                .define("enabled", true);

        RENDER_DISTANCE = builder
                .comment("Maximum distance in blocks to search for highlighted blocks. (最大高亮搜索距离)")
                .defineInRange("renderDistance", 32, 16, 128);

        // Default Mineral Definitions (Block ID | RRGGBB hex color)
        // Built-in rarity based colors:
        List<String> defaultMinerals = List.of(
            ForgeRegistries.BLOCKS.getKey(Blocks.COAL_ORE).toString() + "|333333", // 煤炭 (灰色)
            ForgeRegistries.BLOCKS.getKey(Blocks.COPPER_ORE).toString() + "|C35A25", // 铜 (棕橙色)
            ForgeRegistries.BLOCKS.getKey(Blocks.IRON_ORE).toString() + "|AAAAAA", // 铁 (浅灰色)
            ForgeRegistries.BLOCKS.getKey(Blocks.LAPIS_ORE).toString() + "|1A3385", // 青金石 (深蓝色)
            ForgeRegistries.BLOCKS.getKey(Blocks.REDSTONE_ORE).toString() + "|AA0000", // 红石 (红色)
            ForgeRegistries.BLOCKS.getKey(Blocks.GOLD_ORE).toString() + "|FFD700", // 金 (金色)
            ForgeRegistries.BLOCKS.getKey(Blocks.EMERALD_ORE).toString() + "|00AA00", // 绿宝石 (绿色)
            ForgeRegistries.BLOCKS.getKey(Blocks.DIAMOND_ORE).toString() + "|00FFFF", // 钻石 (青色)
            ForgeRegistries.BLOCKS.getKey(Blocks.ANCIENT_DEBRIS).toString() + "|6F4A0C" // 远古残骸 (深棕色)
        );

        MINERAL_COLORS = builder
                .comment("List of specific blocks to highlight (Block ID | RRGGBB). Higher priority than tags.")
                .defineList("mineralHighlights", defaultMinerals, obj -> obj instanceof String && ((String) obj).contains("|") && ((String) obj).split("\\|")[1].length() == 6);

        // Custom Tag Definitions (Tag ID | RRGGBB hex color)
        List<String> defaultTags = List.of(
            "minecraft:needs_iron_tool|FF00FF" // Example: Blocks needing iron tool highlighted in magenta
        );

        TAG_COLORS = builder
                .comment("List of Block Tags to highlight (Tag ID | RRGGBB). Lower priority than specific block IDs.")
                .defineList("tagHighlights", defaultTags, obj -> obj instanceof String && ((String) obj).contains("|") && ((String) obj).split("\\|")[1].length() == 6);

        CLIENT_SPEC = builder.build();
    }

    // Helper to parse RRGGBB string to Color integer
    public static Integer parseColorHex(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF; // Default white on error
        }
    }
}