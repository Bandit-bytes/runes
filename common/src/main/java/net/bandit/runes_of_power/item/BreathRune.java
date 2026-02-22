package net.bandit.runes_of_power.item;

import net.bandit.runes_of_power.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
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

public class BreathRune extends Item {

    private static final String NBT_LEVEL = "BreathLevel";
    private static final int MAX_LEVEL = 3;

    public BreathRune(Properties properties) {
        super(properties.durability(64));
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
            if (!player.getCooldowns().isOnCooldown(this)) {

                int level = getRuneLevel(stack);
                int additionalDuration = getExtraDurationForLevel(level);

                MobEffectInstance currentEffect = player.getEffect(MobEffects.WATER_BREATHING);

                if (currentEffect != null) {
                    int newDuration = currentEffect.getDuration() + additionalDuration;
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, newDuration));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, additionalDuration));
                }

                // Optional: tiny Dolphins Grace buff to feel more "tide-blessed"
                if (level > 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100 + (level * 40), 0));
                }

                // Sound + particles
                world.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                for (int i = 0; i < 10; i++) {
                    double xOffset = (world.random.nextDouble() - 0.5) * 2;
                    double yOffset = world.random.nextDouble();
                    double zOffset = (world.random.nextDouble() - 0.5) * 2;
                    world.addParticle(
                            ParticleTypes.BUBBLE,
                            player.getX() + xOffset,
                            player.getY() + yOffset,
                            player.getZ() + zOffset,
                            0, 0, 0
                    );
                }

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

        // Require another Rune of Tides (water rune) in offhand
        if (!offhand.is(ItemRegistry.WATER_RUNE.get())) {
            player.displayClientMessage(
                    Component.literal("Another Rune of Tides is needed in your offhand to deepen its blessing.")
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
            return false;
        }

        // Consume one upgrade item
        offhand.shrink(1);

        int newLevel = currentLevel + 1;
        setRuneLevel(runeStack, newLevel);

        player.displayClientMessage(
                Component.literal("The waters answer your call. (Tier " + (newLevel + 1) + ")")
                        .withStyle(ChatFormatting.DARK_AQUA),
                true
        );

        return true;
    }

    private int getExtraDurationForLevel(int level) {
        // Extra duration added per use (in ticks)
        // 0: 30s, 1: 45s, 2: 60s, 3: 80s
        return switch (level) {
            case 0 -> 600;
            case 1 -> 900;
            case 2 -> 1200;
            case 3 -> 1600;
            default -> 600;
        };
    }

    private int getCooldownForLevel(int level) {
        // Higher tiers = shorter cooldown (in ticks)
        // 0: 10s, 1: 8s, 2: 6s, 3: 4s
        return switch (level) {
            case 0 -> 200;
            case 1 -> 160;
            case 2 -> 120;
            case 3 -> 80;
            default -> 200;
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
        tooltip.add(Component.translatable("item.runes_of_power.water_rune.tooltip")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.runes_of_power.water_rune.lore")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        int level = getRuneLevel(stack);
        String[] roman = {"I", "II", "III", "IV"};

        tooltip.add(Component.literal("Tier: " + roman[Mth.clamp(level, 0, 3)])
                .withStyle(ChatFormatting.DARK_GREEN));

        float extraSeconds = getExtraDurationForLevel(level) / 20.0F;
        float cdSeconds = getCooldownForLevel(level) / 20.0F;

        tooltip.add(Component.literal(
                        "Extra duration: " + extraSeconds + "s   Cooldown: " + cdSeconds + "s")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.runes_of_power.water_rune.upgrade_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
