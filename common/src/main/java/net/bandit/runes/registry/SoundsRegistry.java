package net.bandit.runes.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.bandit.runes.RunesMod;

public class SoundsRegistry {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(RunesMod.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> BLACK_OUT_RUNE_USE = register("black_out_rune_use");
    public static final RegistrySupplier<SoundEvent> BURROW_RUNE_USE = register("burrow_rune_use");
    public static final RegistrySupplier<SoundEvent> FIRE_RESISTANCE_RUNE_USE = register("fire_resistance_rune_use");
    public static final RegistrySupplier<SoundEvent> CREATIVE_FLIGHT_RUNE_USE = register("creative_flight_rune_use");
    public static final RegistrySupplier<SoundEvent> TELEPORT_RUNE_USE = register("teleport_rune_use");
    public static final RegistrySupplier<SoundEvent> WATER_RUNE_USE = register("water_rune_use");
    public static final RegistrySupplier<SoundEvent> STEALTH_RUNE_USE = register("stealth_rune_use");

    private static RegistrySupplier<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RunesMod.MOD_ID, name)));
    }

    public static void registerSounds() {
        SOUNDS.register();
    }
}
