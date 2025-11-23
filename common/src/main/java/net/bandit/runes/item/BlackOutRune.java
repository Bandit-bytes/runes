package net.bandit.runes.item;

import net.bandit.runes.registry.ItemRegistry;
import net.bandit.runes.registry.SoundsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlackOutRune extends Item {

    private static final String NBT_LEVEL = "BlackOutLevel";
    private static final int MAX_LEVEL = 3;

    public BlackOutRune(Properties properties) {
        super(properties.durability(100));
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

                int cooldown = getCooldownForLevel(level);
                int effectDuration = getDisorientDurationForLevel(level); // both confusion + blindness

                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, effectDuration, 0));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, effectDuration, 0));

                // Teleport home
                player.getServer().execute(() -> teleportPlayer(player));

                // Cooldown + durability
                player.getCooldowns().addCooldown(this, cooldown);
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

        // Require another BlackOut Rune in offhand
        if (!offhand.is(ItemRegistry.BLACK_OUT_RUNE.get())) {
            player.displayClientMessage(
                    Component.literal("You need another Shadow Rune in your offhand to deepen this bond.")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return false;
        }


        offhand.shrink(1);

        int newLevel = currentLevel + 1;
        setRuneLevel(runeStack, newLevel);

        player.displayClientMessage(
                Component.literal("The shadows tighten around you. (Tier " + (newLevel + 1) + ")")
                        .withStyle(ChatFormatting.DARK_PURPLE),
                true
        );

        return true;
    }

    private int getCooldownForLevel(int level) {

        return switch (level) {
            case 0 -> 200; // 10s
            case 1 -> 160; // 8s
            case 2 -> 120; // 6s
            case 3 -> 80;  // 4s
            default -> 200;
        };
    }

    private int getDisorientDurationForLevel(int level) {
        return switch (level) {
            case 0 -> 140; // 7s
            case 1 -> 100; // 5s
            case 2 -> 60;  // 3s
            case 3 -> 40;  // 2s
            default -> 100;
        };
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
                    respawnPos = Player.findRespawnPositionAndUseSpawnBlock(
                            targetWorld,
                            bedLocation,
                            serverPlayer.getRespawnAngle(),
                            serverPlayer.isRespawnForced(),
                            false
                    ).orElse(null);
                }
            }
            if (respawnPos == null) {
                targetWorld = serverPlayer.getServer().getLevel(Level.OVERWORLD);
                if (targetWorld != null) {
                    BlockPos worldSpawn = targetWorld.getSharedSpawnPos();
                    respawnPos = new Vec3(
                            worldSpawn.getX() + 0.5,
                            worldSpawn.getY() + 0.5,
                            worldSpawn.getZ() + 0.5
                    );
                }
            }
            if (respawnPos != null && targetWorld != null) {
                serverPlayer.teleportTo(
                        targetWorld,
                        respawnPos.x,
                        respawnPos.y,
                        respawnPos.z,
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot()
                );
            }
        }
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
        tooltip.add(Component.translatable("item.runes.black_out_rune.tooltip")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.runes.black_out_rune.lore")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        int level = getRuneLevel(stack);

        // Tier (Roman numerals)
        String[] roman = {"I", "II", "III", "IV"};
        tooltip.add(Component.literal("Tier: " + roman[Mth.clamp(level, 0, 3)])
                .withStyle(ChatFormatting.DARK_GREEN));

        // Show cooldown + disorientation duration
        float cdSeconds = getCooldownForLevel(level) / 20.0F;
        float durSeconds = getDisorientDurationForLevel(level) / 20.0F;

        tooltip.add(Component.literal(
                        "Cooldown: " + cdSeconds + "s   Disorientation: " + durSeconds + "s")
                .withStyle(ChatFormatting.GRAY));

        // Upgrade hint
        tooltip.add(Component.translatable("item.runes.black_out_rune.upgrade_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
