package net.bandit.runes.fabric.client;

import net.bandit.runes.RunesMod;
import net.fabricmc.api.ClientModInitializer;

public final class RunesModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RunesMod.initClient();
    }
}
