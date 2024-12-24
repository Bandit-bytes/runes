package net.bandit.runes;


import net.bandit.runes.config.RunesConfig;
import net.bandit.runes.registry.EffectsRegistry;
import net.bandit.runes.registry.ItemRegistry;
import net.bandit.runes.registry.SoundsRegistry;
import net.bandit.runes.registry.TabRegistry;

public final class RunesMod {
    public static final String MOD_ID = "runes";

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
