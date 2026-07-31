package com.chillpavz.oredetector.forge;

import com.chillpavz.oredetector.forge.config.OreDetectorConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-only: wires the config screen into Forge's mod-list "Config" button. Kept in its own class
 * so its client-only references are never loaded on a dedicated server (see the Dist guard in
 * {@link OreDetectorForge}).
 */
final class OreDetectorConfigScreenRegistrar {

    private OreDetectorConfigScreenRegistrar() {
    }

    static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(OreDetectorConfigScreen::new));
    }
}
