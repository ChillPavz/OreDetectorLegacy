package com.chillpavz.oredetector.registry;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
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
    public static final ResourceLocation ZINC_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "zinc_detector");
    public static Item ZINC_DETECTOR = null;

    /**
     * Repair ingredient per detector. On 1.21.2+ this is declared with {@code Properties.repairable},
     * which does not exist here, so it is looked up from {@link OreDetectorItem#isValidRepairItem}
     * instead. Keeping it in a map avoids threading the material through all twelve subclasses.
     */
    private static final Map<Item, Predicate<ItemStack>> REPAIR_INGREDIENTS = new IdentityHashMap<>();

    private ModItems() {
    }

    /** Builds the zinc detector on demand; call ONLY when Create is present, then register it. */
    public static Item createZinc() {
        TagKey<Item> zincIngots = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/zinc"));
        ZINC_DETECTOR = new ZincDetector(new Item.Properties().durability(OreDetectorConfig.scaleDurability(200)));
        REPAIR_INGREDIENTS.put(ZINC_DETECTOR, stack -> stack.is(zincIngots));
        return ZINC_DETECTOR;
    }

    /** Whether {@code ingredient} repairs {@code detector} in an anvil. */
    public static boolean isRepairIngredient(Item detector, ItemStack ingredient) {
        Predicate<ItemStack> predicate = REPAIR_INGREDIENTS.get(detector);
        return predicate != null && predicate.test(ingredient);
    }

    private static Item create(String name, int durability, Item repairMaterial, Function<Item.Properties, Item> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
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
