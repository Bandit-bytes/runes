package net.bandit.runes_of_power.fabric;

import net.fabricmc.api.ModInitializer;

import net.bandit.runes_of_power.RunesMod;

public final class RunesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RunesMod.init();
    }
}
