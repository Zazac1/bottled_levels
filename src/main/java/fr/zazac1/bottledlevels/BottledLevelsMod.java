package fr.zazac1.bottledlevels;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BottledLevelsMod implements ModInitializer {
    public static final String MODID = "bottled_levels";
    public static final Identifier XP_BOTTLE_ID = Identifier.of(MODID, "xp_bottle");
    public static Item XP_BOTTLE;

    @Override
    public void onInitialize() {
        XP_BOTTLE = Registry.register(Registries.ITEM, XP_BOTTLE_ID, new XpBottleItem(
                new Item.Settings()
                        .maxCount(64)
                        .component(DataComponentTypes.CUSTOM_MODEL_DATA,
                                new CustomModelDataComponent(0))
        ));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(XP_BOTTLE));
        BottledLevelsCommands.register();
    }
}
