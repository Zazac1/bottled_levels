package fr.zazac1.bottledlevels;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(BottledLevelsMod.MODID)
public final class BottledLevelsMod {
    public static final String MODID = "bottled_levels";
    public static final ResourceLocation XP_BOTTLE_ID = ResourceLocation.fromNamespaceAndPath(MODID, "xp_bottle");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<Item> XP_BOTTLE = ITEMS.register("xp_bottle", () -> new XpBottleItem(
            new Item.Properties().stacksTo(64).component(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(0))));

    public BottledLevelsMod(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreativeTabEntries);
        BottledLevelsCommands.register();
    }

    private void addCreativeTabEntries(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) event.accept(XP_BOTTLE);
    }
}
