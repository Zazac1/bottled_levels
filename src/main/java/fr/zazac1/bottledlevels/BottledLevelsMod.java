package fr.zazac1.bottledlevels;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomModelData;
import java.util.List;

public class BottledLevelsMod implements ModInitializer {
    public static final String MODID = "bottled_levels";
    public static final Identifier XP_BOTTLE_ID = Identifier.fromNamespaceAndPath(MODID, "xp_bottle");
    public static Item XP_BOTTLE;

    @Override
    public void onInitialize() {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, XP_BOTTLE_ID);

        XP_BOTTLE = Registry.register(BuiltInRegistries.ITEM, XP_BOTTLE_ID, new XpBottleItem(
                new Item.Properties()
                        .stacksTo(64)
                        .setId(itemKey)
                        .component(DataComponents.CUSTOM_MODEL_DATA,
                                new CustomModelData(List.of(0f), List.of(), List.of(), List.of()))
        ));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(XP_BOTTLE));
        BottledLevelsCommands.register();
    }
}
