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

// 修复点：将 bus = Mod.EventBusSubscriber.Bus.FORGE 改为 Bus.MOD
// 因为配置加载事件 (ModConfigEvent) 是在 MOD 总线上分发的
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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

    // 保持 public，以便 ClientConfig 保存时可以手动调用刷新
    public static void loadConfig() {
        BLOCK_SPECIFIC_COLORS.clear();
        TAG_COLORS.clear();

        // 打印日志以便调试
        System.out.println("ClientBlockHighlight: Loading config colors...");

        for (String entry : ClientConfig.MINERAL_COLORS.get()) {
            try {
                String[] parts = entry.split("\\|");
                ResourceLocation blockId = ResourceLocation.tryParse(parts[0]);
                if (blockId != null) {
                    Integer color = ClientConfig.parseColorHex(parts[1]);
                    BLOCK_SPECIFIC_COLORS.put(blockId, color);
                    // System.out.println("Loaded highlight for: " + blockId); // 调试用
                }
            } catch (Exception e) {
                // Ignored parsing error
            }
        }

        for (String entry : ClientConfig.TAG_COLORS.get()) {
            try {
                String[] parts = entry.split("\\|");
                ResourceLocation tagId = ResourceLocation.tryParse(parts[0]);
                if (tagId != null) {
                    Integer color = ClientConfig.parseColorHex(parts[1]);
                    TagKey<Block> tag = TagKey.create(Registries.BLOCK, tagId);
                    TAG_COLORS.put(tag, color);
                }
            } catch (Exception e) {
                // Ignored parsing error
            }
        }
    }

    public static Integer getColorForBlock(BlockState state) {
        Block block = state.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);

        if (BLOCK_SPECIFIC_COLORS.containsKey(blockId)) {
            return BLOCK_SPECIFIC_COLORS.get(blockId);
        }

        for (Map.Entry<TagKey<Block>, Integer> entry : TAG_COLORS.entrySet()) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}