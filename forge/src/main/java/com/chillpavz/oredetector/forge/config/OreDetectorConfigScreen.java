package com.chillpavz.oredetector.forge.config;

import java.util.ArrayList;
import java.util.List;

import com.chillpavz.oredetector.config.OreDetectorConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client-only config screen for the Forge build, reached from the mods list. Cloth Config has no
 * Forge build past 1.21.3 and neither Configured nor Forge Config Screens has a 1.21.11 Forge
 * build, so where Fabric and NeoForge get a generated Cloth screen, Forge needs this hand-written
 * one. It deliberately reuses the same lang keys as the Cloth screen so the wording matches, and
 * offers the same per-option reset that Cloth does.
 */
public class OreDetectorConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int ROW_WIDTH = 310;
    private static final int WIDGET_HEIGHT = 20;
    private static final int RESET_WIDTH = 50;
    private static final int GAP = 4;
    private static final int SLIDER_WIDTH = ROW_WIDTH - RESET_WIDTH - GAP;

    private final Screen parent;
    private final List<IntSlider> sliders = new ArrayList<>();

    public OreDetectorConfigScreen(Screen parent) {
        super(Component.translatable("text.autoconfig.oredetector.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        sliders.clear();
        int x = this.width / 2 - ROW_WIDTH / 2;
        int y = Math.max(40, this.height / 2 - (8 * ROW_HEIGHT) / 2);

        y = addRow(x, y, "downReach", OreDetectorConfigData.DOWN_REACH,
                OreDetectorConfig.REACH_MIN, OreDetectorConfig.REACH_MAX, OreDetectorConfig.DEFAULT_DOWN_REACH);
        y = addRow(x, y, "sideReach", OreDetectorConfigData.SIDE_REACH,
                OreDetectorConfig.REACH_MIN, OreDetectorConfig.REACH_MAX, OreDetectorConfig.DEFAULT_SIDE_REACH);
        y = addRow(x, y, "columnRadius", OreDetectorConfigData.COLUMN_RADIUS,
                OreDetectorConfig.COLUMN_RADIUS_MIN, OreDetectorConfig.COLUMN_RADIUS_MAX,
                OreDetectorConfig.DEFAULT_COLUMN_RADIUS);
        y = addRow(x, y, "cooldownTicks", OreDetectorConfigData.COOLDOWN_TICKS,
                OreDetectorConfig.COOLDOWN_MIN, OreDetectorConfig.COOLDOWN_MAX, OreDetectorConfig.DEFAULT_COOLDOWN);
        y = addRow(x, y, "durabilityPercent", OreDetectorConfigData.DURABILITY_PERCENT,
                OreDetectorConfigData.DURABILITY_PERCENT_MIN, OreDetectorConfigData.DURABILITY_PERCENT_MAX,
                OreDetectorConfigData.DURABILITY_PERCENT_DEFAULT);
        y = addRow(x, y, "soundVolumePercent", OreDetectorConfigData.SOUND_VOLUME_PERCENT,
                OreDetectorConfigData.SOUND_VOLUME_PERCENT_MIN, OreDetectorConfigData.SOUND_VOLUME_PERCENT_MAX,
                OreDetectorConfigData.SOUND_VOLUME_PERCENT_DEFAULT);

        int buttonWidth = (ROW_WIDTH - GAP) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.oredetector.reset_all"),
                        button -> sliders.forEach(IntSlider::resetToDefault))
                .bounds(x, y + GAP, buttonWidth, WIDGET_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(x + buttonWidth + GAP, y + GAP, buttonWidth, WIDGET_HEIGHT)
                .build());
    }

    private int addRow(int x, int y, String option, ForgeConfigSpec.IntValue value, int min, int max, int defaultValue) {
        IntSlider slider = new IntSlider(x, y, option, value, min, max, defaultValue);
        sliders.add(slider);
        addRenderableWidget(slider);
        addRenderableWidget(Button.builder(Component.translatable("gui.oredetector.reset"),
                        button -> slider.resetToDefault())
                .bounds(x + SLIDER_WIDTH + GAP, y, RESET_WIDTH, WIDGET_HEIGHT)
                .build());
        return y + ROW_HEIGHT;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        // Write every slider back, persist once, then push the values into the running game so the
        // changes take effect without a reload (durability still needs a restart, as on the other loaders).
        sliders.forEach(IntSlider::commit);
        OreDetectorConfigData.save();
        OreDetectorConfigData.applyToRuntime();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    /** Slider over an integer config option, showing "Label: value". */
    private static class IntSlider extends AbstractSliderButton {

        private final ForgeConfigSpec.IntValue config;
        private final String option;
        private final int min;
        private final int max;
        private final int defaultValue;

        IntSlider(int x, int y, String option, ForgeConfigSpec.IntValue config, int min, int max, int defaultValue) {
            super(x, y, SLIDER_WIDTH, WIDGET_HEIGHT, Component.empty(), toSliderValue(config.get(), min, max));
            this.config = config;
            this.option = option;
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
            updateMessage();
        }

        private static double toSliderValue(int value, int min, int max) {
            return (double) (value - min) / (max - min);
        }

        private int current() {
            return min + (int) Math.round(this.value * (max - min));
        }

        void resetToDefault() {
            this.value = toSliderValue(defaultValue, min, max);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("text.autoconfig.oredetector.option." + option)
                    .append(": ")
                    .append(Component.literal(Integer.toString(current()))));
        }

        @Override
        protected void applyValue() {
            // Only the label follows the handle; the config is written once on close.
            updateMessage();
        }

        void commit() {
            config.set(current());
        }
    }
}
