package fr.zazac1.bottledlevels;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;

import java.util.List;

public class XpBottleItem extends Item {
    private static final String NBT_FORMAT = "BottledLevelsFormat";
    private static final int FORMAT_WHOLE_LEVELS = 2;
    private static final String NBT_STORED_LEVELS = "StoredLevels";
    private static final String LEGACY_STORED_XP = "StoredXP";
    private static final String LEGACY_STORED_LEVELS = "LevelsAbsorbed";
    // Matches the cadence used by Minecraft's Consumable component.
    private static final int DRINK_SOUND_INTERVAL_TICKS = 4;
    private static final int DRINK_SOUND_START_DELAY_TICKS = 7;

    public XpBottleItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (user.getCooldowns().isOnCooldown(this)) {
            if (!world.isClientSide()) user.displayClientMessage(Component.translatable("item.bottled_levels.xp_bottle.cooldown"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (user.isShiftKeyDown()) {
            if (world.isClientSide()) return InteractionResultHolder.sidedSuccess(stack, true);
            return result(transfer(world, user, hand, true), stack);
        }

        if (readContents(stack).levels() == 0) return InteractionResultHolder.pass(stack);
        // Drinking is intentionally the only non-sneaking transfer path.
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    private InteractionResultHolder<ItemStack> result(InteractionResult result, ItemStack stack) {
        if (result == InteractionResult.FAIL) return InteractionResultHolder.fail(stack);
        if (result == InteractionResult.PASS) return InteractionResultHolder.pass(stack);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int elapsedUseTicks = getUseDuration(stack, user) - remainingUseTicks;
        if (!world.isClientSide()
                && elapsedUseTicks > DRINK_SOUND_START_DELAY_TICKS
                && remainingUseTicks % DRINK_SOUND_INTERVAL_TICKS == 0) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5f, 0.9f);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide() && user instanceof Player player) {
            transfer(world, player, player.getUsedItemHand(), false);
        }
        return playerStack(user, stack);
    }

    private ItemStack playerStack(LivingEntity user, ItemStack fallback) {
        return user instanceof Player player ? player.getItemInHand(player.getUsedItemHand()) : fallback;
    }

    private InteractionResult transfer(Level world, Player user, InteractionHand hand, boolean depositing) {
        ItemStack stack = user.getItemInHand(hand);
        if (user.getCooldowns().isOnCooldown(this)) {
            user.displayClientMessage(Component.translatable("item.bottled_levels.xp_bottle.cooldown"), true);
            return InteractionResult.FAIL;
        }
        BottleContents contents = readContents(stack);
        if (contents.ambiguousLegacy()) {
            user.displayClientMessage(Component.translatable("item.bottled_levels.xp_bottle.legacy_ambiguous"), true);
            return InteractionResult.FAIL;
        }

        WorldBottleConfig config = WorldBottleConfig.get(world.getServer());
        LevelTransferService.Transfer transfer = depositing
                ? LevelTransferService.deposit(user.experienceLevel, contents.levels(), config.maxLevels)
                : LevelTransferService.withdraw(user.experienceLevel, contents.levels());
        if (!transfer.valid()) return InteractionResult.FAIL;
        if (!transfer.changed()) return InteractionResult.CONSUME;

        if (!applyToOneBottle(user, hand, stack, transfer.storedLevelsAfter(), config.maxLevels)) {
            user.displayClientMessage(Component.translatable("item.bottled_levels.xp_bottle.inventory_full"), true);
            return InteractionResult.FAIL;
        }

        float progress = user.experienceProgress;
        user.giveExperienceLevels(transfer.playerLevelsAfter() - user.experienceLevel);
        user.experienceProgress = progress;
        if (config.cooldownSeconds > 0) {
            user.getCooldowns().addCooldown(this, config.cooldownSeconds * 20);
        }

        if (depositing && config.damageOnDeposit && config.depositDamage > 0.0f) {
            user.hurt(world.damageSources().generic(), config.depositDamage);
        }
        playTransferSound(world, user, depositing, transfer.storedLevelsAfter(), config.maxLevels);
        return InteractionResult.CONSUME;
    }

    private boolean applyToOneBottle(Player player, InteractionHand hand, ItemStack stack, int storedLevels, int capacity) {
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

        stack.shrink(1);
        if (player.getInventory().add(changedBottle)) return true;

        // A failed insertion cannot consume either XP or an item.
        stack.grow(1);
        return false;
    }

    private boolean canStoreChangedBottle(Player player, ItemStack original, ItemStack changedBottle) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate == original) continue;
            if (candidate.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(candidate, changedBottle)
                    && candidate.getCount() <= candidate.getMaxStackSize() - changedBottle.getCount()) return true;
        }
        return false;
    }

    private void playTransferSound(Level world, Player player, boolean depositing, int storedAfter, int capacity) {
        if (depositing) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    storedAfter >= capacity ? SoundEvents.PLAYER_LEVELUP : SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.5f, 1.0f);
        } else {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.8f, 1.0f);
        }
    }

    private BottleContents readContents(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return BottleContents.empty();
        CompoundTag nbt = customData.copyTag();
        if (getInt(nbt, NBT_FORMAT, 0) >= FORMAT_WHOLE_LEVELS) {
            int levels = getInt(nbt, NBT_STORED_LEVELS, -1);
            return levels >= 0 ? new BottleContents(levels, false) : BottleContents.ambiguous();
        }
        if (nbt.contains(LEGACY_STORED_LEVELS)) {
            int legacyLevels = getInt(nbt, LEGACY_STORED_LEVELS, -1);
            return legacyLevels >= 0 ? new BottleContents(legacyLevels, false) : BottleContents.ambiguous();
        }
        return getInt(nbt, LEGACY_STORED_XP, 0) > 0 ? BottleContents.ambiguous() : BottleContents.empty();
    }

    private int getInt(CompoundTag nbt, String key, int fallback) {
        return nbt.contains(key) ? nbt.getInt(key) : fallback;
    }

    private void writeContents(ItemStack stack, int levels, int capacity) {
        if (levels == 0) {
            clearContents(stack);
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(0));
            return;
        }
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        stack.set(DataComponents.CUSTOM_DATA, customData.update(nbt -> {
            nbt.putInt(NBT_FORMAT, FORMAT_WHOLE_LEVELS);
            nbt.putInt(NBT_STORED_LEVELS, levels);
        }));
        // Empty bottles use texture 0. The nine filled textures scale with the
        // configured capacity: at the default capacity of 30, each texture
        // covers three levels (1-3, 4-6, ..., 28-30).
        int tier = Math.min(9, (int) (((long) levels * 9 + capacity - 1) / capacity));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(tier));
    }

    private void clearContents(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        CompoundTag nbt = customData.copyTag();
        nbt.remove(NBT_FORMAT);
        nbt.remove(NBT_STORED_LEVELS);
        nbt.remove(LEGACY_STORED_XP);
        nbt.remove(LEGACY_STORED_LEVELS);
        if (nbt.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        BottleContents contents = readContents(stack);
        if (contents.ambiguousLegacy()) {
            tooltip.add(Component.translatable("item.bottled_levels.xp_bottle.legacy_ambiguous").withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.translatable("item.bottled_levels.xp_bottle.levels", contents.levels()).withStyle(ChatFormatting.GREEN));
    }

    private record BottleContents(int levels, boolean ambiguousLegacy) {
        static BottleContents empty() { return new BottleContents(0, false); }
        static BottleContents ambiguous() { return new BottleContents(0, true); }
    }
}
