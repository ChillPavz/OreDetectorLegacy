package com.chillpavz.oredetector.forge.config;

import com.chillpavz.oredetector.config.OreDetectorConfig;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Forge config model. Cloth Config has no Forge build past 1.21.3, so unlike the Fabric and NeoForge
 * modules this one is built on Forge's own {@link ForgeConfigSpec} (classic Forge kept that name;
 * only NeoForge renamed it to ModConfigSpec). Same six options and the same bounds, pushed into the
 * shared {@link OreDetectorConfig} by {@link #applyToRuntime}.
 */
public final class OreDetectorConfigData {

    public static final ForgeConfigSpec SPEC;

    static final ForgeConfigSpec.IntValue DOWN_REACH;
    static final ForgeConfigSpec.IntValue SIDE_REACH;
    static final ForgeConfigSpec.IntValue COLUMN_RADIUS;
    static final ForgeConfigSpec.IntValue COOLDOWN_TICKS;
    static final ForgeConfigSpec.IntValue DURABILITY_PERCENT;
    static final ForgeConfigSpec.IntValue SOUND_VOLUME_PERCENT;

    public static final int DURABILITY_PERCENT_MIN = 25;
    public static final int DURABILITY_PERCENT_MAX = 400;
    public static final int SOUND_VOLUME_PERCENT_MIN = 0;
    public static final int SOUND_VOLUME_PERCENT_MAX = 100;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Ore Detector Reborn").push("general");

        DOWN_REACH = builder
                .comment("How far a detector scans when pointed at the ground, in blocks.")
                .defineInRange("downReach", OreDetectorConfig.DEFAULT_DOWN_REACH,
                        OreDetectorConfig.REACH_MIN, OreDetectorConfig.REACH_MAX);
        SIDE_REACH = builder
                .comment("How far a detector scans when pointed at a wall or ceiling, in blocks.")
                .defineInRange("sideReach", OreDetectorConfig.DEFAULT_SIDE_REACH,
                        OreDetectorConfig.REACH_MIN, OreDetectorConfig.REACH_MAX);
        COLUMN_RADIUS = builder
                .comment("Radius of the scanned column: 0 = 1x1, 1 = 3x3, 2 = 5x5, 3 = 7x7.")
                .defineInRange("columnRadius", OreDetectorConfig.DEFAULT_COLUMN_RADIUS,
                        OreDetectorConfig.COLUMN_RADIUS_MIN, OreDetectorConfig.COLUMN_RADIUS_MAX);
        COOLDOWN_TICKS = builder
                .comment("Cooldown between scans, in ticks (20 ticks = 1 second).")
                .defineInRange("cooldownTicks", OreDetectorConfig.DEFAULT_COOLDOWN,
                        OreDetectorConfig.COOLDOWN_MIN, OreDetectorConfig.COOLDOWN_MAX);
        DURABILITY_PERCENT = builder
                .comment("Detector durability as a percentage of the default. Requires a restart to apply.")
                .defineInRange("durabilityPercent", 100, DURABILITY_PERCENT_MIN, DURABILITY_PERCENT_MAX);
        SOUND_VOLUME_PERCENT = builder
                .comment("Detector beep volume as a percentage. 0 disables the beep, which also stops",
                        "it from triggering sculk sensors.")
                .defineInRange("soundVolumePercent", 40, SOUND_VOLUME_PERCENT_MIN, SOUND_VOLUME_PERCENT_MAX);

        builder.pop();
        SPEC = builder.build();
    }

    private OreDetectorConfigData() {
    }

    /** Persists the config file. Saving any one value writes the whole spec, so one call is enough. */
    public static void save() {
        DOWN_REACH.save();
    }

    public static void applyToRuntime() {
        OreDetectorConfig.apply(DOWN_REACH.get(), SIDE_REACH.get(), COLUMN_RADIUS.get(), COOLDOWN_TICKS.get(),
                DURABILITY_PERCENT.get() / 100.0, SOUND_VOLUME_PERCENT.get() / 100.0);
    }
}
