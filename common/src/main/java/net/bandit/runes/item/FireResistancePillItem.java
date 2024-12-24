package net.bandit.runes.item;

import net.bandit.runes.registry.EffectsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireResistancePillItem extends Item {

    public FireResistancePillItem(Properties properties) {
        super(properties.durability(20));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            MobEffectInstance fireResEffect = player.getEffect(MobEffects.FIRE_RESISTANCE);
            if (fireResEffect != null) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
                        fireResEffect.getDuration() + 600, fireResEffect.getAmplifier()));
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            }

            MobEffectInstance lavaVisionEffect = player.getEffect(EffectsRegistry.LAVA_VISION.get());
            if (lavaVisionEffect != null) {
                player.addEffect(new MobEffectInstance(EffectsRegistry.LAVA_VISION.get(),
                        lavaVisionEffect.getDuration() + 600, lavaVisionEffect.getAmplifier()));
            } else {
                player.addEffect(new MobEffectInstance(EffectsRegistry.LAVA_VISION.get(), 600, 0));
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.PLAYERS, 1.0F, 1.0F);
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
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
        tooltip.add(Component.translatable("item.runes.fire_resistance_rune.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.runes.fire_resistance_rune.lore_1").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.runes.fire_resistance_rune.lore_2").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
