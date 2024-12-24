package net.bandit.runes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaterPillItem extends Item {

    public WaterPillItem(Properties properties) {
        super(properties.durability(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            MobEffectInstance currentEffect = player.getEffect(MobEffects.WATER_BREATHING);
            int additionalDuration = 600;

            if (currentEffect != null) {
                int newDuration = currentEffect.getDuration() + additionalDuration;
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, newDuration));
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, additionalDuration));
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
            for (int i = 0; i < 10; i++) {
                double xOffset = (world.random.nextDouble() - 0.5) * 2;
                double yOffset = world.random.nextDouble();
                double zOffset = (world.random.nextDouble() - 0.5) * 2;
                world.addParticle(ParticleTypes.BUBBLE, player.getX() + xOffset, player.getY() + yOffset, player.getZ() + zOffset, 0, 0, 0);
            }

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
        tooltip.add(Component.translatable("item.runes.water_rune.tooltip").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.runes.water_rune.lore").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
