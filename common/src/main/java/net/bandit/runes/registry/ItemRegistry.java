package net.bandit.runes.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bandit.runes.RunesMod;
import net.bandit.runes.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(RunesMod.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> EMPTY_RUNE = ITEMS.register("empty_rune",
            () -> new Item(new Properties().stacksTo(2).arch$tab(TabRegistry.RUNES_TAB)));
    public static final RegistrySupplier<Item> WATER_RUNE = ITEMS.register("water_rune",
            () -> new BreathRune(new Properties().stacksTo(2).arch$tab(TabRegistry.RUNES_TAB).rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> CURE_INSOMNIA_RUNE = ITEMS.register("cure_rune",
            () -> new CureRune(new Item.Properties().stacksTo(2).arch$tab(TabRegistry.RUNES_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> STEALTH_RUNE = ITEMS.register("stealth_rune",
            () -> new StealthRune(new Item.Properties().stacksTo(2).arch$tab(TabRegistry.RUNES_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> BURROW_RUNE = ITEMS.register("burrow_rune",
            () -> new BurrowingRune(new Item.Properties().stacksTo(2).arch$tab(TabRegistry.RUNES_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> CREATIVE_FLIGHT_RUNE = ITEMS.register("flight_rune",
            () -> new FlightRune(new Item.Properties().stacksTo(2).arch$tab(TabRegistry.RUNES_TAB).rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> FIRE_RESISTANCE_RUNE = ITEMS.register("fire_resistance_rune",
            () -> new ResisRune(new Item.Properties().stacksTo(2).arch$tab(TabRegistry.RUNES_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> BLACK_OUT_RUNE = ITEMS.register("return_rune",
            () -> new BlackOutRune(new Item.Properties().stacksTo(1).arch$tab(TabRegistry.RUNES_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> TELEPORT_RUNE = ITEMS.register("teleport_rune",
            () -> new TeleportRune(new Item.Properties().stacksTo(2).arch$tab(TabRegistry.RUNES_TAB).rarity(Rarity.EPIC)));


    public static void register() {
        ITEMS.register();
    }
}
