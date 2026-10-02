package fr.zazac1.bottledlevels;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.function.Consumer;

public class XpBottleItem extends Item {
    private static final String NBT_FORMAT = "BottledLevelsFormat";
    private static final int FORMAT_WHOLE_LEVELS = 2;
    private static final String NBT_STORED_LEVELS = "StoredLevels";
    private static final String LEGACY_STORED_XP = "StoredXP";
    private static final String LEGACY_STORED_LEVELS = "LevelsAbsorbed";

    public XpBottleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(stack)) {
            if (!world.isClient()) user.sendMessage(Text.translatable("item.bottled_levels.xp_bottle.cooldown"), true);
            return ActionResult.FAIL;
        }
        if (user.isSneaking()) {
            if (world.isClient()) return ActionResult.SUCCESS;
            return transfer(world, user, hand, true);
        }

        if (readContents(stack).levels() == 0) return ActionResult.PASS;
        // Drinking is intentionally the only non-sneaking transfer path.
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof PlayerEntity player) {
            transfer(world, player, player.getActiveHand(), false);
        }
        return playerStack(user, stack);
    }

    private ItemStack playerStack(LivingEntity user, ItemStack fallback) {
        return user instanceof PlayerEntity player ? player.getStackInHand(player.getActiveHand()) : fallback;
    }

    private ActionResult transfer(World world, PlayerEntity user, Hand hand, boolean depositing) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(stack)) {
            user.sendMessage(Text.translatable("item.bottled_levels.xp_bottle.cooldown"), true);
            return ActionResult.FAIL;
        }
        BottleContents contents = readContents(stack);
        if (contents.ambiguousLegacy()) {
            user.sendMessage(Text.translatable("item.bottled_levels.xp_bottle.legacy_ambiguous"), true);
            return ActionResult.FAIL;
        }

        WorldBottleConfig config = WorldBottleConfig.get(world.getServer());
        LevelTransferService.Transfer transfer = depositing
                ? LevelTransferService.deposit(user.experienceLevel, contents.levels(), config.maxLevels)
                : LevelTransferService.withdraw(user.experienceLevel, contents.levels());
        if (!transfer.valid()) return ActionResult.FAIL;
        if (!transfer.changed()) return ActionResult.CONSUME;

        if (!applyToOneBottle(user, hand, stack, transfer.storedLevelsAfter(), config.maxLevels)) {
            user.sendMessage(Text.translatable("item.bottled_levels.xp_bottle.inventory_full"), true);
            return ActionResult.FAIL;
        }

        float progress = user.experienceProgress;
        user.addExperienceLevels(transfer.playerLevelsAfter() - user.experienceLevel);
        user.experienceProgress = progress;
        if (config.cooldownSeconds > 0) {
            user.getItemCooldownManager().set(stack, config.cooldownSeconds * 20);
        }

        if (depositing && config.damageOnDeposit && config.depositDamage > 0.0f && world instanceof ServerWorld serverWorld) {
            user.damage(serverWorld, world.getDamageSources().generic(), config.depositDamage);
        }
        playTransferSound(world, user, depositing, transfer.storedLevelsAfter(), config.maxLevels);
        return ActionResult.CONSUME;
    }

    private boolean applyToOneBottle(PlayerEntity player, Hand hand, ItemStack stack, int storedLevels, int capacity) {
        if (stack.getCount() == 1) {
            writeContents(stack, storedLevels, capacity);
            return true;
        }

        // Prepare the changed bottle before placing it in the inventory. It must
        // no longer be component-compatible with the source stack, otherwise the
        // inventory would merge it back and the entire stack would be modified.
        ItemStack changedBottle = stack.copyWithCount(1);
        writeContents(changedBottle, storedLevels, capacity);
        if (!canStoreChangedBottle(player, stack, changedBottle)) return false;

        stack.decrement(1);
        if (player.getInventory().insertStack(changedBottle)) return true;

        // A failed insertion cannot consume either XP or an item.
        stack.increment(1);
        return false;
    }

    private boolean canStoreChangedBottle(PlayerEntity player, ItemStack original, ItemStack changedBottle) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack candidate = player.getInventory().getStack(slot);
            if (candidate == original) continue;
            if (candidate.isEmpty()) return true;
            if (ItemStack.areItemsAndComponentsEqual(candidate, changedBottle)
                    && candidate.getCount() <= candidate.getMaxCount() - changedBottle.getCount()) return true;
        }
        return false;
    }

    private void playTransferSound(World world, PlayerEntity player, boolean depositing, int storedAfter, int capacity) {
        if (depositing) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    storedAfter >= capacity ? SoundEvents.ENTITY_PLAYER_LEVELUP : SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS, 0.5f, 1.0f);
        } else {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 0.8f, 1.0f);
        }
    }

    private BottleContents readContents(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return BottleContents.empty();
        NbtCompound nbt = customData.copyNbt();
        if (nbt.getInt(NBT_FORMAT, 0) >= FORMAT_WHOLE_LEVELS) {
            int levels = nbt.getInt(NBT_STORED_LEVELS, -1);
            return levels >= 0 ? new BottleContents(levels, false) : BottleContents.ambiguous();
        }
        if (nbt.contains(LEGACY_STORED_LEVELS)) {
            int legacyLevels = nbt.getInt(LEGACY_STORED_LEVELS, -1);
            return legacyLevels >= 0 ? new BottleContents(legacyLevels, false) : BottleContents.ambiguous();
        }
        return nbt.getInt(LEGACY_STORED_XP, 0) > 0 ? BottleContents.ambiguous() : BottleContents.empty();
    }

    private void writeContents(ItemStack stack, int levels, int capacity) {
        if (levels == 0) {
            clearContents(stack);
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                    new CustomModelDataComponent(List.of(0f), List.of(), List.of(), List.of()));
            return;
        }
        NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        stack.set(DataComponentTypes.CUSTOM_DATA, customData.apply(nbt -> {
            nbt.putInt(NBT_FORMAT, FORMAT_WHOLE_LEVELS);
            nbt.putInt(NBT_STORED_LEVELS, levels);
        }));
        int tier = levels >= capacity ? 6 : (int) ((long) levels * 6 / capacity);
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(List.of((float) tier), List.of(), List.of(), List.of()));
    }

    private void clearContents(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return;

        NbtCompound nbt = customData.copyNbt();
        nbt.remove(NBT_FORMAT);
        nbt.remove(NBT_STORED_LEVELS);
        nbt.remove(LEGACY_STORED_XP);
        nbt.remove(LEGACY_STORED_LEVELS);
        if (nbt.isEmpty()) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent,
                              Consumer<Text> tooltip, TooltipType type) {
        BottleContents contents = readContents(stack);
        if (contents.ambiguousLegacy()) {
            tooltip.accept(Text.translatable("item.bottled_levels.xp_bottle.legacy_ambiguous").formatted(Formatting.RED));
            return;
        }
        tooltip.accept(Text.translatable("item.bottled_levels.xp_bottle.levels", contents.levels())
                .formatted(Formatting.GREEN));
    }

    private record BottleContents(int levels, boolean ambiguousLegacy) {
        static BottleContents empty() { return new BottleContents(0, false); }
        static BottleContents ambiguous() { return new BottleContents(0, true); }
    }
}
