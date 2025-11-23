package net.bandit.runes.item;

import net.bandit.runes.registry.EffectsRegistry;
import net.bandit.runes.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResisRune extends Item {

    private static final String NBT_LEVEL = "FireResLevel";
    private static final int MAX_LEVEL = 3;

    public ResisRune(Properties properties) {
        super(properties.durability(20));
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
                int extraDuration = getExtraDurationForLevel(level); // ticks

                // Fire resistance
                MobEffectInstance fireResEffect = player.getEffect(MobEffects.FIRE_RESISTANCE);
                if (fireResEffect != null) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.FIRE_RESISTANCE,
                            fireResEffect.getDuration() + extraDuration,
                            fireResEffect.getAmplifier()
                    ));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, extraDuration, 0));
                }

                // Lava vision
                MobEffectInstance lavaVisionEffect = player.getEffect(EffectsRegistry.LAVA_VISION.get());
                if (lavaVisionEffect != null) {
                    player.addEffect(new MobEffectInstance(
                            EffectsRegistry.LAVA_VISION.get(),
                            lavaVisionEffect.getDuration() + extraDuration,
                            lavaVisionEffect.getAmplifier()
                    ));
                } else {
                    player.addEffect(new MobEffectInstance(EffectsRegistry.LAVA_VISION.get(), extraDuration, 0));
                }

                // Sound
                world.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.BOTTLE_FILL_DRAGONBREATH,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

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

        // Require another fire resistance rune in offhand
        if (!offhand.is(ItemRegistry.FIRE_RESISTANCE_RUNE.get())) {
            player.displayClientMessage(
                    Component.literal("Another Inferno Rune is needed in your offhand to stoke the flames.")
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
                Component.literal("The heat bends away from you. (Tier " + (newLevel + 1) + ")")
                        .withStyle(ChatFormatting.DARK_RED),
                true
        );

        return true;
    }

    // Extra duration added for both effects (ticks)
    private int getExtraDurationForLevel(int level) {
        // 0: 30s, 1: 45s, 2: 60s, 3: 80s
        return switch (level) {
            case 0 -> 600;
            case 1 -> 900;
            case 2 -> 1200;
            case 3 -> 1600;
            default -> 600;
        };
    }

    // Cooldown between uses (ticks)
    private int getCooldownForLevel(int level) {
        // 0: 20s, 1: 15s, 2: 10s, 3: 6s
        return switch (level) {
            case 0 -> 400;
            case 1 -> 300;
            case 2 -> 200;
            case 3 -> 120;
            default -> 400;
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
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.runes.fire_resistance_rune.tooltip")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.runes.fire_resistance_rune.lore_1")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.runes.fire_resistance_rune.lore_2")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        int level = getRuneLevel(stack);
        String[] roman = {"I", "II", "III", "IV"};

        tooltip.add(Component.literal("Tier: " + roman[Mth.clamp(level, 0, 3)])
                .withStyle(ChatFormatting.DARK_GREEN));

        float durationSeconds = getExtraDurationForLevel(level) / 20.0F;
        float cdSeconds = getCooldownForLevel(level) / 20.0F;

        tooltip.add(Component.literal("Duration per use: " + durationSeconds + "s   Cooldown: " + cdSeconds + "s")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.runes.fire_resistance_rune.upgrade_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
