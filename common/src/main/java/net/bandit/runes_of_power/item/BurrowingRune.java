package net.bandit.runes_of_power.item;

import net.bandit.runes_of_power.registry.ItemRegistry;
import net.bandit.runes_of_power.registry.SoundsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BurrowingRune extends Item {

    private static final String NBT_LEVEL = "BurrowLevel";
    private static final int MAX_LEVEL = 3;

    public BurrowingRune(Properties properties) {
        super(properties.durability(150));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);


        if (!world.isClientSide && player.isShiftKeyDown()) {
            if (tryUpgradeRune(player, stack)) {
                return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
            }
        }

        // Normal use
        if (!world.isClientSide) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                int level = getRuneLevel(stack);

                int cooldown = getCooldownForLevel(level);
                int hasteDuration = 200;
                int hasteAmplifier = level;

                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, hasteDuration, hasteAmplifier));

                if (world instanceof ServerLevel serverLevel) {
                    breakNearbyBlocks(serverLevel, player.blockPosition(), player, level);
                }

                player.getCooldowns().addCooldown(this, cooldown);
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
        } else {
            world.playLocalSound(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundsRegistry.BURROW_RUNE_USE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F,
                    false
            );
            playItemUseAnimation(player, stack);
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

        if (!offhand.is(ItemRegistry.BURROW_RUNE.get())) {
            player.displayClientMessage(
                    Component.literal("You need a special catalyst in your offhand to empower this rune.")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return false;
        }

        // Consume one upgrade item
        offhand.shrink(1);

        int newLevel = currentLevel + 1;
        setRuneLevel(runeStack, newLevel);

        player.displayClientMessage(
                Component.literal("The rune hums with deeper power. (Tier " + (newLevel + 1) + ")")
                        .withStyle(ChatFormatting.GOLD),
                true
        );

        return true;
    }

    private int getCooldownForLevel(int level) {
        // Lower cooldown at higher levels
        return switch (level) {
            case 0 -> 200;
            case 1 -> 160;
            case 2 -> 120;
            case 3 -> 100;
            default -> 200;
        };
    }

    private void breakNearbyBlocks(ServerLevel world, BlockPos center, Player player, int level) {
        int radius = 1 + level; // 1–4

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {

                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    if (isBurrowable(state.getBlock())) {
                        world.destroyBlock(pos, true, player);
                    }
                }
            }
        }
    }

    private boolean isBurrowable(Block block) {
        return block == Blocks.DIRT
                || block == Blocks.GRASS_BLOCK
                || block == Blocks.SAND
                || block == Blocks.RED_SAND
                || block == Blocks.GRAVEL
                || block == Blocks.CLAY
                || block == Blocks.SOUL_SAND
                || block == Blocks.SOUL_SOIL
                || block == Blocks.MUD;
    }

    private void playItemUseAnimation(Player player, ItemStack stack) {
        if (player.level().isClientSide) {
            Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
        }
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
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.runes_of_power.burrowing_rune.tooltip")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.runes_of_power.burrowing_rune.lore")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        int level = getRuneLevel(stack);

        String[] roman = {"I", "II", "III", "IV"};
        tooltip.add(Component.literal("Tier: " + roman[Mth.clamp(level, 0, 3)])
                .withStyle(ChatFormatting.DARK_GREEN));

        tooltip.add(Component.literal("Radius: " + (1 + level) +
                        "   Cooldown: " + (getCooldownForLevel(level) / 20.0F) + "s")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.runes_of_power.burrowing_rune.upgrade_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }


    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
