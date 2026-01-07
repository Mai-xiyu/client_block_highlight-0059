package com.yourname.client_block_highlight;

import com.mojang.logging.LogUtils;
import com.yourname.client_block_highlight.config.ClientConfig;
import com.yourname.client_block_highlight.gui.ClientBlockHighlightConfigScreen;
import com.yourname.client_block_highlight.util.KeyBindings;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent; // 导入
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
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ClientBlockHighlightConfigScreen(screen)));

        // 注册按键绑定
        modEventBus.addListener(this::registerBindings);

        // We register client event handlers via @Mod.EventBusSubscriber in renderer/manager classes
        MinecraftForge.EVENT_BUS.register(this);
    }

    // 新增：注册按键的方法
    private void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_CONFIG_KEY);
    }
}