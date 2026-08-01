package com.chillpavz.oredetector.forge;

import com.chillpavz.oredetector.forge.config.OreDetectorConfigData;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-only: wires Cloth Config's generated screen into Forge's mod-list "Config" button. Kept in
 * its own class so its client-only references are never loaded on a dedicated server (see the Dist
 * guard in {@link OreDetectorForge}). Unlike the 1.21.11 Forge module, no screen is hand-written
 * here — Cloth Config ships a Forge build at this Minecraft version.
 */
final class OreDetectorConfigScreenRegistrar {

    private OreDetectorConfigScreenRegistrar() {
    }

    static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (Screen parent) -> AutoConfig.getConfigScreen(OreDetectorConfigData.class, parent).get()));
    }
}
