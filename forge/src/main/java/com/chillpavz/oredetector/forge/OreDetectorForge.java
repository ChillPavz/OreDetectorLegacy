package com.chillpavz.oredetector.forge;

import com.chillpavz.oredetector.Constants;
import com.chillpavz.oredetector.forge.config.OreDetectorConfigData;
import com.chillpavz.oredetector.registry.ModCreativeTabs;
import com.chillpavz.oredetector.registry.ModItems;
import com.chillpavz.oredetector.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Forge entrypoint. Forge 61 is on EventBus 8: mod-bus events are subscribed through the mod's own
 * {@link BusGroup} via {@code getBus(...)}, while game-bus events expose a static {@code BUS}.
 */
@Mod(Constants.MOD_ID)
public class OreDetectorForge {

    private static final String[] CREATE_MOD_IDS = {"create", "create-fly", "createfly", "create_fly"};

    public OreDetectorForge() {
        // Registering the spec makes Forge load the file and fire ModConfigEvent.Loading before the
        // registry events, so the durability multiplier is in place by the time items are created.
        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, OreDetectorConfigData.SPEC);

        BusGroup modBus = context.getActiveContainer().getModBusGroup();
        ModConfigEvent.Loading.getBus(modBus).addListener(event -> applyIfOurs(event.getConfig()));
        ModConfigEvent.Reloading.getBus(modBus).addListener(event -> applyIfOurs(event.getConfig()));
        RegisterEvent.getBus(modBus).addListener(OreDetectorForge::onRegister);

        BuildCreativeModeTabContentsEvent.BUS.addListener(OreDetectorForge::onBuildTabContents);

        // Client-only: the screen classes must never be loaded on a dedicated server, so the
        // reference lives behind this guard in a separate class.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            OreDetectorConfigScreenRegistrar.register();
        }
    }

    private static void applyIfOurs(ModConfig config) {
        if (config.getSpec() == OreDetectorConfigData.SPEC) {
            OreDetectorConfigData.applyToRuntime();
        }
    }

    private static void onRegister(RegisterEvent event) {
        event.register(Registries.SOUND_EVENT, helper -> ModSounds.SOUND_EVENTS.forEach(helper::register));
        event.register(Registries.ITEM, helper -> {
            ModItems.ITEMS.forEach(helper::register);
            if (isCreateLoaded()) {
                helper.register(ModItems.ZINC_ID, ModItems.createZinc());
            }
        });
        event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(ModCreativeTabs.KEY, ModCreativeTabs.MAIN));
    }

    private static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.KEY) {
            ModItems.ITEMS.values().forEach(item ->
                    event.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
            if (isCreateLoaded()) {
                event.accept(new ItemStack(ModItems.ZINC_DETECTOR), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }

    private static boolean isCreateLoaded() {
        for (String id : CREATE_MOD_IDS) {
            if (ModList.get().isLoaded(id)) {
                return true;
            }
        }
        return false;
    }
}
