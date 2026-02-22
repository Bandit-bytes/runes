package net.bandit.runes_of_power.registry;

import net.bandit.runes_of_power.RunesMod;
import net.bandit.runes_of_power.effect.CreativeFlightEffect;
import net.bandit.runes_of_power.effect.LavaVisionEffect;
import net.bandit.runes_of_power.effect.StealthEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class EffectsRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(RunesMod.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> LAVA_VISION = MOB_EFFECTS.register("lava_vision",
            LavaVisionEffect::new);
    public static final RegistrySupplier<MobEffect> STEALTH = MOB_EFFECTS.register("stealth_effect",
            StealthEffect::new);
    public static final RegistrySupplier<MobEffect> CREATIVE_FLIGHT = MOB_EFFECTS.register("creative_flight",
            CreativeFlightEffect::new);


    public static void register() {
        MOB_EFFECTS.register();
    }
}
