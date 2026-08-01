package com.chillpavz.oredetector.forge;

import com.chillpavz.oredetector.Constants;
import com.chillpavz.oredetector.forge.config.OreDetectorConfigData;
import com.chillpavz.oredetector.registry.ModCreativeTabs;
import com.chillpavz.oredetector.registry.ModItems;
import com.chillpavz.oredetector.registry.ModSounds;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Forge entrypoint. Forge 52 predates the EventBus 8 rework used at 1.21.11: the mod bus comes from
 * {@link FMLJavaModLoadingContext}, and BOTH {@link RegisterEvent} and
 * {@link BuildCreativeModeTabContentsEvent} are mod-bus events here — the latter only moved to the
 * game bus later. Cloth Config has a Forge build at this version, so config works exactly as it does
 * on Fabric and NeoForge instead of going through Forge's own config API.
 */
@Mod(Constants.MOD_ID)
public class OreDetectorForge {

    private static final String[] CREATE_MOD_IDS = {"create", "create-fly", "createfly", "create_fly"};

    public OreDetectorForge() {
        // Load config first so items pick up the durability multiplier when they are created.
        // AutoConfig reads the file synchronously right here, which is why this version needs none of
        // the pre-registration TOML reading the 1.21.11 Forge module has to do.
        AutoConfig.register(OreDetectorConfigData.class, GsonConfigSerializer::new);
        ConfigHolder<OreDetectorConfigData> config = AutoConfig.getConfigHolder(OreDetectorConfigData.class);
        config.getConfig().applyToRuntime();
        config.registerSaveListener((holder, data) -> {
            data.applyToRuntime();
            return InteractionResult.SUCCESS;
        });

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(OreDetectorForge::onRegister);
        modBus.addListener(OreDetectorForge::onBuildTabContents);

        // Client-only: the screen classes must never be loaded on a dedicated server.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            OreDetectorConfigScreenRegistrar.register();
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
