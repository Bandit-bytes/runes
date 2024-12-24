package net.bandit.runes.fabric;

import net.fabricmc.api.ModInitializer;

import net.bandit.runes.RunesMod;

public final class RunesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RunesMod.init();
    }
}
