package net.bandit.runes_of_power.item;

import net.bandit.runes_of_power.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
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

public class CureRune extends Item {

    private static final String NBT_LEVEL = "CureLevel";
    private static final int MAX_LEVEL = 3;

    public CureRune(Properties properties) {
        super(properties.durability(80));
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

                // Core effect: cure insomnia / prevent phantoms
                player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));

                // Extra soothing effects based on tier
                applyRestfulEffects(player, level);

                // Sound
                world.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.HONEY_DRINK,
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

    private void applyRestfulEffects(Player player, int level) {
        // Gentle bonuses as the rune grows stronger
        switch (level) {
            case 0 -> {
                // Just the base cure — no extra effects
            }
            case 1 -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0)); // 5s Regen I
            }
            case 2 -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 140, 0)); // 7s Regen I
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0));   // 10s Absorption I
            }
            case 3 -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1)); // 8s Regen II
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));   // 15s Absorption II
            }
        }
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

        // Require another Cure Rune in offhand
        if (!offhand.is(ItemRegistry.CURE_INSOMNIA_RUNE.get())) {
            player.displayClientMessage(
                    Component.literal("Another Dreamweaver Rune is needed in your offhand to strengthen this charm.")
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
            return false;
        }

        offhand.shrink(1);

        int newLevel = currentLevel + 1;
        setRuneLevel(runeStack, newLevel);

        player.displayClientMessage(
                Component.literal("The rune settles your spirit more deeply. (Tier " + (newLevel + 1) + ")")
                        .withStyle(ChatFormatting.DARK_PURPLE),
                true
        );

        return true;
    }

    private int getCooldownForLevel(int level) {
        // Higher tiers = shorter cooldown (ticks)
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
        tooltip.add(Component.translatable("item.runes_of_power.cure_insomnia_rune.tooltip")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.runes_of_power.cure_insomnia_rune.lore")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        int level = getRuneLevel(stack);
        String[] roman = {"I", "II", "III", "IV"};

        tooltip.add(Component.literal("Tier: " + roman[Mth.clamp(level, 0, 3)])
                .withStyle(ChatFormatting.DARK_GREEN));

        float cdSeconds = getCooldownForLevel(level) / 20.0F;

        tooltip.add(Component.literal("Cooldown: " + cdSeconds + "s")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.runes_of_power.cure_insomnia_rune.upgrade_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
