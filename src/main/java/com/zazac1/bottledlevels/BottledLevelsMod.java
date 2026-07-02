package com.zazac1.bottledlevels;

import net.fabricmc.api.ModInitializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

public class BottledLevelsMod implements ModInitializer {
    public static final String MODID = "bottled_levels";
    public static final Identifier XP_BOTTLE_ID = Identifier.of(MODID, "xp_bottle");
    public static Item XP_BOTTLE;

    @Override
    public void onInitialize() {
        ModConfig.load();

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, XP_BOTTLE_ID);
        int maxCount = ModConfig.INSTANCE.stackable ? 64 : 1;

        XP_BOTTLE = Registry.register(Registries.ITEM, XP_BOTTLE_ID, new XpBottleItem(
                new Item.Settings()
                        .maxCount(maxCount)
                        .registryKey(itemKey)
                        // icône par défaut = tier 0 (bouteille vide) pour les items sans données
                        .component(DataComponentTypes.CUSTOM_MODEL_DATA,
                                new CustomModelDataComponent(List.of(0f), List.of(), List.of(), List.of()))
        ));
    }
}

