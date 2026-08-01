package com.chillpavz.oredetector.item;

import java.util.List;

import com.chillpavz.oredetector.Constants;
import com.chillpavz.oredetector.config.OreDetectorConfig;
import com.chillpavz.oredetector.registry.ModItems;
import com.chillpavz.oredetector.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Base detector item. Right-click a surface: it scans an N x N beam INTO that surface (opposite the
 * clicked face) for the target ore, reaching further downward than sideways/upward. Reach, column
 * size and cooldown come from {@link OreDetectorConfig}. Reports the count in the action bar, plays
 * a cue, and wears the tool down by 1 per use plus 1 per ore found.
 */
public class OreDetectorItem extends Item {

    private static final float SOUND_PITCH = 1.0f;

    /**
     * NBT key holding the game time at which this particular detector becomes usable again.
     * 1.21.1's {@code ItemCooldowns} is keyed by {@code Item}, not by stack or cooldown group (that
     * arrived in 1.21.2), so vanilla cooldowns would apply to every detector of the same kind at
     * once. The expiry is tracked per stack instead, at the cost of vanilla's cooldown sweep on the
     * icon — hence the explicit "recharging" message when a scan is refused.
     */
    private static final String COOLDOWN_UNTIL_TAG = "OreDetectorCooldownUntil";

    public OreDetectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!level.isClientSide()) {
            ItemStack stack = context.getItemInHand();
            if (player != null && isOnCooldown(stack, level)) {
                player.displayClientMessage(
                        Component.translatable("hud.oredetector.cooldown").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.FAIL;
            }

            Direction scanDir = context.getClickedFace().getOpposite();
            int found = scan(level, context.getClickedPos(), scanDir);

            SoundEvent sound = found > 0 ? ModSounds.FOUND : ModSounds.NOT_FOUND;
            if (OreDetectorConfig.soundVolume > 0.0) {
                level.playSound(null, context.getClickedPos(), sound, SoundSource.PLAYERS,
                        (float) OreDetectorConfig.soundVolume, SOUND_PITCH);
                // The beep is a real noise: emit a vibration so nearby sculk sensors react to it.
                // Muting the detector (volume 0) therefore also makes it sculk-safe.
                level.gameEvent(player, GameEvent.INSTRUMENT_PLAY, context.getClickedPos());
            }

            if (player != null) {
                Component message = found > 0
                        ? Component.translatable("hud.oredetector.found", found, getOreName()).withColor(getOreColor())
                        : Component.translatable("hud.oredetector.none", getOreName()).withStyle(ChatFormatting.GRAY);
                player.displayClientMessage(message, true);
            }

            if (player != null) {
                applyCooldown(stack, level);
                // 1 for the scan itself, plus 1 per ore found.
                // The stack is the one in the used hand, so map that hand straight to its slot.
                stack.hurtAndBreak(1 + found, player,
                        context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Whether this particular detector is still recharging. */
    private static boolean isOnCooldown(ItemStack stack, Level level) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && level.getGameTime() < data.copyTag().getLong(COOLDOWN_UNTIL_TAG);
    }

    /** Starts the cooldown for THIS detector only, leaving other detectors of the same kind usable. */
    private static void applyCooldown(ItemStack stack, Level level) {
        long until = level.getGameTime() + OreDetectorConfig.cooldownTicks;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(COOLDOWN_UNTIL_TAG, until));
    }

    /** Counts matching ore in an N x N beam that starts at {@code origin} and extends along {@code dir}. */
    private int scan(Level level, BlockPos origin, Direction dir) {
        int depth = dir == Direction.DOWN ? OreDetectorConfig.downReach : OreDetectorConfig.sideReach;
        int radius = OreDetectorConfig.columnRadius;   // 0 -> 1x1, 1 -> 3x3, ... 3 -> 7x7

        // Two axes perpendicular to the scan direction, used for the (2r+1) x (2r+1) cross-section.
        int[] axisU;
        int[] axisV;
        switch (dir.getAxis()) {
            case Y -> { axisU = new int[]{1, 0, 0}; axisV = new int[]{0, 0, 1}; }
            case X -> { axisU = new int[]{0, 1, 0}; axisV = new int[]{0, 0, 1}; }
            default -> { axisU = new int[]{1, 0, 0}; axisV = new int[]{0, 1, 0}; }
        }
        int stepX = dir.getStepX();
        int stepY = dir.getStepY();
        int stepZ = dir.getStepZ();

        int count = 0;
        for (int u = -radius; u <= radius; u++) {
            for (int v = -radius; v <= radius; v++) {
                for (int d = 0; d < depth; d++) {
                    BlockPos pos = origin.offset(
                            u * axisU[0] + v * axisV[0] + d * stepX,
                            u * axisU[1] + v * axisV[1] + d * stepY,
                            u * axisU[2] + v * axisV[2] + d * stepZ);
                    if (isValidBlock(level.getBlockState(pos))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.oredetector.usage", getOreName()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.oredetector.range", OreDetectorConfig.downReach, OreDetectorConfig.sideReach)
                .withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, lines, flag);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        // Properties.repairable() does not exist at this version; the mapping lives in ModItems.
        return ModItems.isRepairIngredient(stack.getItem(), ingredient);
    }

    /** The block(s) this detector reacts to. */
    public boolean isValidBlock(BlockState state) {
        return false;
    }

    /** Display name of the ore this detector looks for, used in messages and tooltips. */
    protected Component getOreName() {
        return Component.translatable("ore.oredetector.unknown");
    }

    /** RGB color used to tint the "found" message, chosen to resemble the ore. */
    protected int getOreColor() {
        return 0xFFFFFF;
    }
}
