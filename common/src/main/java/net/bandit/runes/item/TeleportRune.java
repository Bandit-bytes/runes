package net.bandit.runes.item;

import net.bandit.runes.config.RunesConfig;
import net.bandit.runes.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeleportRune extends Item {
    public static final String DIMENSION_KEY = "TeleportDimension";

    private static final String NBT_LEVEL = "TeleportLevel";
    private static final int MAX_LEVEL = 3;

    public TeleportRune(Properties properties) {
        super(properties.durability(4));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player.isShiftKeyDown()) {
            if (tryUpgradeRune(player, stack)) {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        if (!level.isClientSide) {
            int runeLevel = getRuneLevel(stack);

            if (!player.getCooldowns().isOnCooldown(this)) {
                CompoundTag tag = stack.getOrCreateTag();

                if (!tag.contains(DIMENSION_KEY)) {
                    ResourceKey<Level> defaultDimension = RunesConfig.getDefaultDimension();
                    if (defaultDimension != null) {
                        tag.putString(DIMENSION_KEY, defaultDimension.location().toString());
                    }
                }

                if (tag.contains(DIMENSION_KEY)) {
                    ResourceKey<Level> dimension = ResourceKey.create(
                            Registries.DIMENSION,
                            new ResourceLocation(tag.getString(DIMENSION_KEY))
                    );

                    if (player instanceof ServerPlayer serverPlayer) {
                        boolean teleported = teleportPlayer(serverPlayer, dimension, runeLevel);
                        if (teleported) {
                            player.getCooldowns().addCooldown(this, getCooldownForLevel(runeLevel));
                            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                        }
                    }
                } else {
                    player.sendSystemMessage(
                            Component.translatable("item.runes.teleport_rune.invalid_dimension")
                    );
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
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

        if (!offhand.is(ItemRegistry.TELEPORT_RUNE.get())) {
            player.displayClientMessage(
                    Component.literal("Another Rune of Passage is needed in your offhand to strengthen this link.")
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    true
            );
            return false;
        }

        offhand.shrink(1);

        int newLevel = currentLevel + 1;
        setRuneLevel(runeStack, newLevel);

        player.displayClientMessage(
                Component.literal("The path between realms grows clearer. (Tier " + (newLevel + 1) + ")")
                        .withStyle(ChatFormatting.DARK_PURPLE),
                true
        );

        return true;
    }

    private boolean teleportPlayer(ServerPlayer player, ResourceKey<Level> destination, int runeLevel) {
        if (player.level().dimension() != destination) {
            ServerLevel targetLevel = player.getServer().getLevel(destination);
            if (targetLevel != null) {
                Vec3 spawnPos = findSafeSpawnPos(targetLevel);
                float spawnAngle = targetLevel.getSharedSpawnAngle();

                player.teleportTo(targetLevel, spawnPos.x, spawnPos.y, spawnPos.z, spawnAngle, 0.0F);
                applyPostTeleportProtection(player, runeLevel);
                return true;
            }
        } else {
            if (runeLevel >= 2) {
                ServerLevel targetLevel = player.server.getLevel(destination);
                if (targetLevel != null) {
                    Vec3 spawnPos = findSafeSpawnPos(targetLevel);
                    float spawnAngle = targetLevel.getSharedSpawnAngle();

                    player.teleportTo(targetLevel, spawnPos.x, spawnPos.y, spawnPos.z, spawnAngle, 0.0F);
                    applyPostTeleportProtection(player, runeLevel);
                    return true;
                }
            }
        }
        return false;
    }
    private Vec3 findSafeSpawnPos(ServerLevel level) {
        BlockPos base = level.getSharedSpawnPos();

        int maxRadius = 16;

        for (int r = 0; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int x = base.getX() + dx;
                    int z = base.getZ() + dz;

                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos groundPos = new BlockPos(x, y - 1, z);
                    BlockPos feetPos = groundPos.above();

                    BlockState groundState = level.getBlockState(groundPos);
                    BlockState feetState = level.getBlockState(feetPos);

                    boolean solidGround = !groundState.isAir();
                    boolean spaceForPlayer = feetState.getCollisionShape(level, feetPos).isEmpty()
                            && level.getBlockState(feetPos.above()).getCollisionShape(level, feetPos.above()).isEmpty();

                    if (solidGround && spaceForPlayer) {
                        return new Vec3(x + 0.5D, feetPos.getY() + 0.01D, z + 0.5D);
                    }
                }
            }
        }

        BlockPos fallback = base.atY(level.getSeaLevel());
        return new Vec3(fallback.getX() + 0.5D, fallback.getY() + 5.0D, fallback.getZ() + 0.5D);
    }


    private void applyPostTeleportProtection(ServerPlayer player, int level) {
        int slowFallingDuration;
        int resistanceDuration;

        switch (level) {
            case 0 -> {
                slowFallingDuration = 60;
                resistanceDuration = 40;
            }
            case 1 -> {
                slowFallingDuration = 100;
                resistanceDuration = 60;
            }
            case 2 -> {
                slowFallingDuration = 140;
                resistanceDuration = 80;
            }
            case 3 -> {
                slowFallingDuration = 200;
                resistanceDuration = 100;
            }
            default -> {
                slowFallingDuration = 60;
                resistanceDuration = 40;
            }
        }

        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, slowFallingDuration, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, resistanceDuration, 0));
    }

    private int getCooldownForLevel(int level) {
        // Teleport is strong, so longer base cooldown
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Show destination from the stack if present, otherwise config default
        String dimensionText = "No destination set";

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(DIMENSION_KEY)) {
            dimensionText = tag.getString(DIMENSION_KEY);
        } else {
            ResourceKey<Level> defaultDimension = RunesConfig.getDefaultDimension();
            if (defaultDimension != null) {
                dimensionText = defaultDimension.location().toString();
            }
        }

        tooltip.add(Component.literal("Teleport to: " + dimensionText)
                .withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.translatable("item.runes.teleport_rune.tooltip")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.runes.teleport_rune.lore")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        int runeLevel = getRuneLevel(stack);
        String[] roman = {"I", "II", "III", "IV"};

        tooltip.add(Component.literal("Tier: " + roman[Mth.clamp(runeLevel, 0, 3)])
                .withStyle(ChatFormatting.DARK_GREEN));

        float cdSeconds = getCooldownForLevel(runeLevel) / 20.0F;

        tooltip.add(Component.literal("Cooldown: " + cdSeconds + "s")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.runes.teleport_rune.upgrade_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
