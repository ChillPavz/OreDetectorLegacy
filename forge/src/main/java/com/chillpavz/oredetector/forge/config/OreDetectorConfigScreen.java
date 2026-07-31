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
 * Forge build past 1.21.3 and neither Configured nor Forge Config Screens have a 1.21.11 Forge
 * build, so where Fabric and NeoForge get a generated Cloth screen, Forge needs this hand-written
 * one. It deliberately reuses the same lang keys as the Cloth screen so the wording matches.
 */
public class OreDetectorConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int WIDGET_WIDTH = 310;
    private static final int WIDGET_HEIGHT = 20;

    private final Screen parent;
    private final List<IntSlider> sliders = new ArrayList<>();

    public OreDetectorConfigScreen(Screen parent) {
        super(Component.translatable("text.autoconfig.oredetector.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        sliders.clear();
        int x = this.width / 2 - WIDGET_WIDTH / 2;
        int y = Math.max(40, this.height / 2 - (7 * ROW_HEIGHT) / 2);

        y = addSlider(x, y, "downReach", OreDetectorConfigData.DOWN_REACH,
                OreDetectorConfig.REACH_MIN, OreDetectorConfig.REACH_MAX);
        y = addSlider(x, y, "sideReach", OreDetectorConfigData.SIDE_REACH,
                OreDetectorConfig.REACH_MIN, OreDetectorConfig.REACH_MAX);
        y = addSlider(x, y, "columnRadius", OreDetectorConfigData.COLUMN_RADIUS,
                OreDetectorConfig.COLUMN_RADIUS_MIN, OreDetectorConfig.COLUMN_RADIUS_MAX);
        y = addSlider(x, y, "cooldownTicks", OreDetectorConfigData.COOLDOWN_TICKS,
                OreDetectorConfig.COOLDOWN_MIN, OreDetectorConfig.COOLDOWN_MAX);
        y = addSlider(x, y, "durabilityPercent", OreDetectorConfigData.DURABILITY_PERCENT,
                OreDetectorConfigData.DURABILITY_PERCENT_MIN, OreDetectorConfigData.DURABILITY_PERCENT_MAX);
        y = addSlider(x, y, "soundVolumePercent", OreDetectorConfigData.SOUND_VOLUME_PERCENT,
                OreDetectorConfigData.SOUND_VOLUME_PERCENT_MIN, OreDetectorConfigData.SOUND_VOLUME_PERCENT_MAX);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(this.width / 2 - 100, y + 4, 200, WIDGET_HEIGHT)
                .build());
    }

    private int addSlider(int x, int y, String option, ForgeConfigSpec.IntValue value, int min, int max) {
        IntSlider slider = new IntSlider(x, y, option, value, min, max);
        sliders.add(slider);
        addRenderableWidget(slider);
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

        IntSlider(int x, int y, String option, ForgeConfigSpec.IntValue config, int min, int max) {
            super(x, y, WIDGET_WIDTH, WIDGET_HEIGHT, Component.empty(),
                    (double) (config.get() - min) / (max - min));
            this.config = config;
            this.option = option;
            this.min = min;
            this.max = max;
            updateMessage();
        }

        private int current() {
            return min + (int) Math.round(this.value * (max - min));
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
