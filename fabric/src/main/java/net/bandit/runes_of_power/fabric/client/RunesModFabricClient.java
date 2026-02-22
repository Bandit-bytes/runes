package net.bandit.runes_of_power.fabric.client;

import net.bandit.runes_of_power.RunesMod;
import net.fabricmc.api.ClientModInitializer;

public final class RunesModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RunesMod.initClient();
    }
}
