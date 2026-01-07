package com.yourname.client_block_highlight.util;

import com.yourname.client_block_highlight.ClientBlockHighlight;
import com.yourname.client_block_highlight.gui.ClientBlockHighlightConfigScreen;
import com.yourname.client_block_highlight.util.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ClientBlockHighlight.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.OPEN_CONFIG_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new ClientBlockHighlightConfigScreen(null));
        }
    }
}