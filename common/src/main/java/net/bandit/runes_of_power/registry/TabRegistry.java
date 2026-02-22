package net.bandit.runes_of_power.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.runes_of_power.RunesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class TabRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(RunesMod.MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> RUNES_TAB = TABS.register(
            "runes_tab",
            () -> CreativeTabRegistry.create(
                    Component.translatable("category.runes_of_power"),
                    () -> new ItemStack(ItemRegistry.STEALTH_RUNE.get())
            )
    );
    public static void init() {
        TABS.register();
    }
}
