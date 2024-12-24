package net.bandit.runes.item;

import net.bandit.runes.registry.SoundsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlackOutPillItem extends Item {

    public BlackOutPillItem(Properties properties) {
        super(properties.durability(100));
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                player.getServer().execute(() -> teleportPlayer(player));
                player.getCooldowns().addCooldown(this, 200);
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
        } else {
            player.level().playLocalSound(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundsRegistry.BLACK_OUT_RUNE_USE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F,
                    false
            );
            playItemUseAnimation(player, stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }


    private void teleportPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos bedLocation = serverPlayer.getRespawnPosition();
            ResourceKey<Level> respawnDimension = serverPlayer.getRespawnDimension();

            ServerLevel targetWorld = null;
            Vec3 respawnPos = null;

            if (bedLocation != null) {
                targetWorld = serverPlayer.getServer().getLevel(respawnDimension);
                if (targetWorld != null) {
                    respawnPos = Player.findRespawnPositionAndUseSpawnBlock(targetWorld, bedLocation, serverPlayer.getRespawnAngle(), serverPlayer.isRespawnForced(), false).orElse(null);
                }
            }
            if (respawnPos == null) {
                targetWorld = serverPlayer.getServer().getLevel(Level.OVERWORLD);
                if (targetWorld != null) {
                    BlockPos worldSpawn = targetWorld.getSharedSpawnPos();
                    respawnPos = new Vec3(worldSpawn.getX() + 0.5, worldSpawn.getY() + 0.5, worldSpawn.getZ() + 0.5);
                }
            }
            if (respawnPos != null && targetWorld != null) {
                serverPlayer.teleportTo(targetWorld, respawnPos.x, respawnPos.y, respawnPos.z, serverPlayer.getYRot(), serverPlayer.getXRot());
            }
        }
    }

    private void playItemUseAnimation(Player player, ItemStack stack) {
        if (player.level().isClientSide) {
            Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.runes.black_out_rune.tooltip")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.runes.black_out_rune.lore").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
