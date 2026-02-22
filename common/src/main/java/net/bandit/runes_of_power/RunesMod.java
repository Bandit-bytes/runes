package net.bandit.runes_of_power;


import net.bandit.runes_of_power.config.RunesConfig;
import net.bandit.runes_of_power.registry.EffectsRegistry;
import net.bandit.runes_of_power.registry.ItemRegistry;
import net.bandit.runes_of_power.registry.SoundsRegistry;
import net.bandit.runes_of_power.registry.TabRegistry;

public final class RunesMod {
    public static final String MOD_ID = "runes_of_power";

    public static void init() {
        ItemRegistry.register();
        EffectsRegistry.register();
        RunesConfig.loadConfig();
        TabRegistry.init();
        SoundsRegistry.registerSounds();
    }

    public static void initClient() {
    }
}
