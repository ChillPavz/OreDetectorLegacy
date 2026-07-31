package com.chillpavz.oredetector.forge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chillpavz.oredetector.Constants;
import com.chillpavz.oredetector.config.OreDetectorConfig;
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
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Forge entrypoint. Forge 61 is on EventBus 8: mod-bus events are subscribed through the mod's own
 * {@link BusGroup} via {@code getBus(...)}, while game-bus events expose a static {@code BUS}.
 */
@Mod(Constants.MOD_ID)
public class OreDetectorForge {

    private static final String[] CREATE_MOD_IDS = {"create", "create-fly", "createfly", "create_fly"};

    private static final Pattern DURABILITY_PERCENT =
            Pattern.compile("^\\s*durabilityPercent\\s*=\\s*(\\d+)", Pattern.MULTILINE);

    public OreDetectorForge() {
        // Durability is baked into the items when they are created, during RegisterEvent. Forge has
        // no STARTUP config type (NeoForge added one for exactly this case), and a COMMON config is
        // not guaranteed to have loaded by then, so that one value is read straight out of the file
        // first. Everything else is applied normally from ModConfigEvent below.
        applyDurabilityEarly();

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

    /**
     * Reads {@code durabilityPercent} out of the config TOML before registration. Deliberately a
     * plain text read rather than the config API: the whole point is that this runs before Forge
     * has loaded the spec, so {@code ConfigValue.get()} is not available yet. On the first run the
     * file does not exist and the defaults are already correct.
     */
    private static void applyDurabilityEarly() {
        Path file = FMLPaths.CONFIGDIR.get().resolve(Constants.MOD_ID + "-common.toml");
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            Matcher matcher = DURABILITY_PERCENT.matcher(Files.readString(file));
            if (matcher.find()) {
                OreDetectorConfig.applyDurabilityPercent(Integer.parseInt(matcher.group(1)));
            }
        } catch (IOException | RuntimeException e) {
            Constants.LOG.warn("Could not pre-read the detector durability setting; using the default", e);
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
