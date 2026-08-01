package com.chillpavz.oredetector.registry;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Function;

import com.chillpavz.oredetector.Constants;
import com.chillpavz.oredetector.config.OreDetectorConfig;
import com.chillpavz.oredetector.item.AmethystDetector;
import com.chillpavz.oredetector.item.CoalDetector;
import com.chillpavz.oredetector.item.CopperDetector;
import com.chillpavz.oredetector.item.DiamondDetector;
import com.chillpavz.oredetector.item.EmeraldDetector;
import com.chillpavz.oredetector.item.GoldDetector;
import com.chillpavz.oredetector.item.IronDetector;
import com.chillpavz.oredetector.item.LapisDetector;
import com.chillpavz.oredetector.item.NetheriteDetector;
import com.chillpavz.oredetector.item.OreDetectorItem;
import com.chillpavz.oredetector.item.QuartzDetector;
import com.chillpavz.oredetector.item.RedstoneDetector;
import com.chillpavz.oredetector.item.ZincDetector;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Defines the mod's items. Instances are created here (loader-agnostic) with their registry id,
 * durability and repair material baked into the properties; each loader module registers the
 * {@link #ITEMS} entries. Durability is deliberately INVERSE to ore value (rarer ore -> fewer
 * scans) so the netherite detector can't be used to farm netherite cheaply.
 */
public final class ModItems {

    public static final Map<ResourceLocation, Item> ITEMS = new LinkedHashMap<>();

    /**
     * Repair ingredient per detector. On 1.21.2+ this is declared with {@code Properties.repairable},
     * which does not exist here, so it is looked up from {@link OreDetectorItem#isValidRepairItem}
     * instead. Keeping it in a map avoids threading the material through all twelve subclasses.
     *
     * <p>MUST be declared before the detector fields below: static initialisers run in source order,
     * and each {@code create(...)} call writes into this map.
     */
    private static final Map<Item, Predicate<ItemStack>> REPAIR_INGREDIENTS = new IdentityHashMap<>();

    // Durability is tuned inverse to ore rarity/value: abundant, big-vein ores (coal/copper/iron) get
    // the most scans; rare, high-value ores (diamond/emerald/netherite) get the fewest so a detector
    // can't cheaply farm them. See CHANGELOG for the reasoning.
    public static final Item COAL_DETECTOR = create("coal_detector", 260, Items.COAL, CoalDetector::new);
    public static final Item COPPER_DETECTOR = create("copper_detector", 240, Items.COPPER_INGOT, CopperDetector::new);
    public static final Item IRON_DETECTOR = create("iron_detector", 220, Items.IRON_INGOT, IronDetector::new);
    public static final Item REDSTONE_DETECTOR = create("redstone_detector", 200, Items.REDSTONE, RedstoneDetector::new);
    public static final Item QUARTZ_DETECTOR = create("quartz_detector", 200, Items.QUARTZ, QuartzDetector::new);
    public static final Item LAPIS_DETECTOR = create("lapis_detector", 180, Items.LAPIS_LAZULI, LapisDetector::new);
    public static final Item AMETHYST_DETECTOR = create("amethyst_detector", 160, Items.AMETHYST_SHARD, AmethystDetector::new);
    public static final Item GOLD_DETECTOR = create("gold_detector", 150, Items.GOLD_INGOT, GoldDetector::new);
    public static final Item DIAMOND_DETECTOR = create("diamond_detector", 120, Items.DIAMOND, DiamondDetector::new);
    public static final Item EMERALD_DETECTOR = create("emerald_detector", 110, Items.EMERALD, EmeraldDetector::new);
    public static final Item NETHERITE_DETECTOR = create("netherite_detector", 80, Items.NETHERITE_INGOT, NetheriteDetector::new);

    // Optional Create integration. Created LAZILY and only when Create is installed, matching the
    // newer ports; here there is no id baked into the properties, so the item is simply registered
    // under ZINC_ID by the loader entrypoints. Kept OUT of ITEMS.
    public static final ResourceLocation ZINC_ID = new ResourceLocation(Constants.MOD_ID, "zinc_detector");
    public static Item ZINC_DETECTOR = null;

    private ModItems() {
    }

    /** Builds the zinc detector on demand; call ONLY when Create is present, then register it. */
    public static Item createZinc() {
        // THREE different zinc tag spellings are in play, so don't rely on tags alone. Verified by
        // reading each jar:
        //   Create (Forge, 1.20.1)  -> forge:ingots/zinc
        //   Create Fabric (1.20.1)  -> c:zinc_ingots      (the older Fabric plural form)
        //   Create / Create Fly on 1.21.x and 26.x -> c:ingots/zinc
        // Matching the ingot by REGISTRY ID as well - the same trick ModdedOres uses for blocks -
        // makes anvil repair work whichever build is installed, and keeps working if the convention
        // shifts again. The tags stay as a bonus so any other mod's zinc ingot repairs it too.
        ResourceLocation zincIngotId = new ResourceLocation("create", "zinc_ingot");
        List<TagKey<Item>> zincTags = List.of(
                TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ingots/zinc")),
                TagKey.create(Registries.ITEM, new ResourceLocation("c", "zinc_ingots")),
                TagKey.create(Registries.ITEM, new ResourceLocation("c", "ingots/zinc")));
        ZINC_DETECTOR = new ZincDetector(new Item.Properties().durability(OreDetectorConfig.scaleDurability(200)));
        REPAIR_INGREDIENTS.put(ZINC_DETECTOR, stack -> {
            if (zincIngotId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                return true;
            }
            for (TagKey<Item> tag : zincTags) {
                if (stack.is(tag)) {
                    return true;
                }
            }
            return false;
        });
        return ZINC_DETECTOR;
    }

    /** Whether {@code ingredient} repairs {@code detector} in an anvil. */
    public static boolean isRepairIngredient(Item detector, ItemStack ingredient) {
        Predicate<ItemStack> predicate = REPAIR_INGREDIENTS.get(detector);
        return predicate != null && predicate.test(ingredient);
    }

    private static Item create(String name, int durability, Item repairMaterial, Function<Item.Properties, Item> factory) {
        ResourceLocation id = new ResourceLocation(Constants.MOD_ID, name);
        Item.Properties properties = new Item.Properties()
                .durability(OreDetectorConfig.scaleDurability(durability));
        Item item = factory.apply(properties);
        ITEMS.put(id, item);
        REPAIR_INGREDIENTS.put(item, stack -> stack.is(repairMaterial));
        return item;
    }

    /** Forces class initialization so the static fields populate {@link #ITEMS}. */
    public static void bootstrap() {
    }
}
