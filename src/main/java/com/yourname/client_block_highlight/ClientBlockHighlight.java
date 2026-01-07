package com.yourname.client_block_highlight;

import com.mojang.logging.LogUtils;
import com.yourname.client_block_highlight.config.ClientConfig;
import com.yourname.client_block_highlight.gui.ClientBlockHighlightConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ClientBlockHighlight.MOD_ID)
public class ClientBlockHighlight {
    public static final String MOD_ID = "client_block_highlight";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ClientBlockHighlight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register client configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        
        // Register custom config screen factory for the Mods list GUI
        ModLoadingContext.get().registerExtension(ConfigScreenHandler.class, () -> new ConfigScreenHandler((minecraft, parent) -> new ClientBlockHighlightConfigScreen(parent)));
        
        // We register client event handlers via @Mod.EventBusSubscriber in renderer/manager classes
        MinecraftForge.EVENT_BUS.register(this);
    }
} 