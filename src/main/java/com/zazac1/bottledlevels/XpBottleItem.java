package com.zazac1.bottledlevels;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class XpBottleItem extends Item {
    private static final String NBT_XP = "StoredXP";
    private static final String NBT_LEVELS = "LevelsAbsorbed";

    public XpBottleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        if (user instanceof PlayerEntity player && player.isSneaking()) return 72000;
        // if mustDrink=false, drinking completes almost instantly (1 tick is enough)
        return ModConfig.INSTANCE.mustDrink ? 32 : 1;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!(user instanceof PlayerEntity player)) return stack;
        if (player.isSneaking()) return stack;

        if (!world.isClient()) {
            int stored = getStoredXp(stack);
            if (stored > 0) {
                player.addExperience(stored);
                stack.decrement(1);

                ItemStack emptyBottle = new ItemStack(BottledLevelsMod.XP_BOTTLE);
                setStored(emptyBottle, 0, 0);
                if (!player.getInventory().insertStack(emptyBottle)) {
                    player.dropItem(emptyBottle, false);
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 0.8f, 1.0f + world.random.nextFloat() * 0.1f);
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.6f, 0.9f + world.random.nextFloat() * 0.2f);
            }
        }

        return stack;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient()) return;
        if (!(user instanceof PlayerEntity player)) return;
        if (!player.isSneaking()) return; // fill mode only (Shift held)

        int usedTicks = getMaxUseTime(stack, user) - remainingUseTicks;
        if (usedTicks % 5 == 0) {
            depositOneLevel(world, player, stack);
        }
    }

    private void depositOneLevel(World world, PlayerEntity player, ItemStack stack) {
        int maxLevels = ModConfig.INSTANCE.maxLevels;
        int levels = getStoredLevels(stack);
        if (levels >= maxLevels) return;

        // cost based on the BOTTLE's current level (not the player's), × number of bottles in the stack
        int xpCostPerBottle = getXpForNextLevel(levels);
        int totalXpCost = xpCostPerBottle * stack.getCount();
        int playerTotalXp = getPlayerTotalXp(player);
        if (playerTotalXp < totalXpCost) return;

        player.addExperience(-totalXpCost);
        setStored(stack, getStoredXp(stack) + xpCostPerBottle, levels + 1);

        if (levels + 1 >= maxLevels) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.2f);
        } else {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2f, 0.8f + world.random.nextFloat() * 0.4f);
        }
    }

    private int getPlayerTotalXp(PlayerEntity player) {
        int level = Math.max(0, player.experienceLevel);
        int xpToNextLevel = getXpForNextLevel(level);
        return getTotalXpAtLevel(level) + Math.round(player.experienceProgress * xpToNextLevel);
    }

    private int getXpForNextLevel(int level) {
        if (level >= 30) return 112 + (level - 30) * 9;
        if (level >= 15) return 37 + (level - 15) * 5;
        return 7 + level * 2;
    }

    private int getTotalXpAtLevel(int level) {
        if (level <= 16) return level * level + 6 * level;
        if (level <= 31) return (int) (2.5 * level * level - 40.5 * level + 360);
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    private int getStoredXp(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return 0;
        return customData.copyNbt().getInt(NBT_XP, 0);
    }

    private int getStoredLevels(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return 0;
        return customData.copyNbt().getInt(NBT_LEVELS, 0);
    }

    private void setStored(ItemStack stack, int xp, int levels) {
        int clampedLevels = Math.max(0, Math.min(levels, ModConfig.INSTANCE.maxLevels));
        NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        stack.set(DataComponentTypes.CUSTOM_DATA, customData.apply(nbt -> {
            nbt.putInt(NBT_XP, Math.max(0, xp));
            nbt.putInt(NBT_LEVELS, clampedLevels);
        }));

        // tier 0 = empty, tier 6 = full (every 5 levels)
        int tier = Math.min(6, clampedLevels / 5);
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                List.of((float) tier), List.of(), List.of(), List.of()
        ));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> tooltip, TooltipType type) {
        int levels = getStoredLevels(stack);
        tooltip.accept(Text.literal("Level " + levels + " / " + ModConfig.INSTANCE.maxLevels).formatted(Formatting.GREEN));
    }
}

