package net.bandit.runes_of_power.item;

import net.bandit.runes_of_power.registry.EffectsRegistry;
import net.bandit.runes_of_power.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlightRune extends Item {

    private static final String NBT_LEVEL = "FlightLevel";
    private static final int MAX_LEVEL = 3;

    public FlightRune(Properties properties) {
        super(properties.durability(24));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Sneak-use to upgrade
        if (!world.isClientSide && player.isShiftKeyDown()) {
            if (tryUpgradeRune(player, stack)) {
                return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
            }
        }

        if (!world.isClientSide) {
            int level = getRuneLevel(stack);

            if (!player.getCooldowns().isOnCooldown(this)) {

                int duration = getDurationForLevel(level);

                // Apply your custom creative flight effect
                player.addEffect(new MobEffectInstance(EffectsRegistry.CREATIVE_FLIGHT.get(), duration, 0));

                // Sound
                world.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.BOTTLE_EMPTY,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                // Cooldown + durability
                player.getCooldowns().addCooldown(this, getCooldownForLevel(level));
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    private boolean tryUpgradeRune(Player player, ItemStack runeStack) {
        int currentLevel = getRuneLevel(runeStack);
        if (currentLevel >= MAX_LEVEL) {
            player.displayClientMessage(
                    Component.literal("This rune has reached its maximum power.")
                            .withStyle(ChatFormatting.DARK_RED),
                    true
            );
            return false;
        }

        ItemStack offhand = player.getOffhandItem();

        // Require another Flight Rune in offhand
        if (!offhand.is(ItemRegistry.CREATIVE_FLIGHT_RUNE.get())) {
            player.displayClientMessage(
                    Component.literal("Another Rune of Ascension is needed in your offhand to heighten your flight.")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
            return false;
        }

        // Consume one upgrade item
        offhand.shrink(1);

        int newLevel = currentLevel + 1;
        setRuneLevel(runeStack, newLevel);

        player.displayClientMessage(
                Component.literal("The winds answer more eagerly. (Tier " + (newLevel + 1) + ")")
                        .withStyle(ChatFormatting.DARK_AQUA),
                true
        );

        return true;
    }

    // Duration of flight per use (ticks)
    private int getDurationForLevel(int level) {
        // 0: 5 min, 1: 7 min, 2: 9 min, 3: 12 min
        return switch (level) {
            case 0 -> 6000;   // 300s
            case 1 -> 8400;   // 420s
            case 2 -> 10800;  // 540s
            case 3 -> 14400;  // 720s
            default -> 6000;
        };
    }

    // Cooldown between uses (ticks)
    private int getCooldownForLevel(int level) {
        // 0: 60s, 1: 45s, 2: 30s, 3: 20s
        return switch (level) {
            case 0 -> 1200;
            case 1 -> 900;
            case 2 -> 600;
            case 3 -> 400;
            default -> 1200;
        };
    }

    // === NBT Level Helpers ===

    private int getRuneLevel(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int lvl = tag.getInt(NBT_LEVEL);
        return Mth.clamp(lvl, 0, MAX_LEVEL);
    }

    private void setRuneLevel(ItemStack stack, int level) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_LEVEL, Mth.clamp(level, 0, MAX_LEVEL));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.runes_of_power.creative_flight_rune.tooltip")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.runes_of_power.creative_flight_rune.lore_1")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.runes_of_power.creative_flight_rune.lore_2")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        int level = getRuneLevel(stack);
        String[] roman = {"I", "II", "III", "IV"};

        tooltip.add(Component.literal("Tier: " + roman[Mth.clamp(level, 0, 3)])
                .withStyle(ChatFormatting.DARK_GREEN));

        float durationSeconds = getDurationForLevel(level) / 20.0F;
        float cdSeconds = getCooldownForLevel(level) / 20.0F;

        tooltip.add(Component.literal("Duration: " + durationSeconds + "s   Cooldown: " + cdSeconds + "s")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.runes_of_power.creative_flight_rune.upgrade_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
