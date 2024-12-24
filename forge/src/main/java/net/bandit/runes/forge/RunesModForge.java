package net.bandit.runes.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.EnvExecutor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.bandit.runes.RunesMod;

@Mod(RunesMod.MOD_ID)
public final class RunesModForge {
    public RunesModForge() {
        EventBuses.registerModEventBus(RunesMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        RunesMod.init();
        EnvExecutor.runInEnv(Dist.CLIENT, () -> RunesMod::initClient);
    }
}
