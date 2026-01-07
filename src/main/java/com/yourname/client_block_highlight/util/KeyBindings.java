package com.yourname.client_block_highlight.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.yourname.client_block_highlight.ClientBlockHighlight;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String KEY_CATEGORY = "key.categories." + ClientBlockHighlight.MOD_ID;
    public static final String KEY_OPEN_CONFIG = "key." + ClientBlockHighlight.MOD_ID + ".open_config";

    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            KEY_OPEN_CONFIG,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H, // 默认按键 H
            KEY_CATEGORY
    );
}