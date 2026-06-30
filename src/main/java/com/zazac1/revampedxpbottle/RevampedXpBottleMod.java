package com.zazac1.revampedxpbottle;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevampedXpBottleMod implements ModInitializer {
    public static final String MOD_ID = "revamped_xp_bottle";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int MAX_LEVELS = 30;

    @Override
    public void onInitialize() {
        ModComponents.register();
        registerNetworking();
        registerBottleInteractions();
        LOGGER.info("Revamped XP Bottle initialized.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private void registerNetworking() {
        PayloadTypeRegistry.serverboundPlay().register(FillBottlePayload.TYPE, FillBottlePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FillBottlePayload.TYPE,
                (payload, context) -> fillBottleFromLeftClick(context.player()));
    }

    public static void fillBottleFromLeftClick(Player player) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!stack.is(Items.EXPERIENCE_BOTTLE) || !stack.has(ModComponents.STORED_LEVELS)) return;

        int currentLevels = stack.getOrDefault(ModComponents.STORED_LEVELS, 0);
        if (currentLevels >= MAX_LEVELS) {
            player.sendOverlayMessage(Component.translatable("message.revamped_xp_bottle.bottle_full"));
            return;
        }

        int xpToTake = xpCostOfLevel(currentLevels);
        if (rawXp(player) < xpToTake) {
            player.sendOverlayMessage(Component.translatable("message.revamped_xp_bottle.not_enough_xp"));
            return;
        }

        drainRawXp(player, xpToTake);
        stack.set(ModComponents.STORED_LEVELS, currentLevels + 1);
        playFillSound(player, player.level());
    }

    private void registerBottleInteractions() {
        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            ItemStack stack = player.getItemInHand(hand);

            if (stack.is(Items.GLASS_BOTTLE)) {
                if (world.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }

                stack.shrink(1);
                giveOrDrop(player, makeXpBottle(0));
                playFillSound(player, world);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }

    private static void drainRawXp(Player player, int xpToTake) {
        if (xpToTake <= 0) return;

        int newRaw = rawXp(player) - xpToTake;
        if (newRaw < 0) return;

        int newLevel = 0;
        int bar = newRaw;
        while (bar >= xpCostOfLevel(newLevel)) {
            bar -= xpCostOfLevel(newLevel);
            newLevel++;
        }

        if (player instanceof ServerPlayer sp) {
            sp.setExperienceLevels(newLevel);
            sp.setExperiencePoints(bar);
        } else {
            player.experienceLevel = newLevel;
            player.experienceProgress = (float) bar / xpCostOfLevel(newLevel);
        }
    }

    public static int rawXp(Player player) {
        int raw = 0;
        for (int i = 0; i < player.experienceLevel; i++) {
            raw += xpCostOfLevel(i);
        }
        raw += (int) Math.ceil(xpCostOfLevel(player.experienceLevel) * player.experienceProgress);
        return raw;
    }

    public static int xpCostOfLevel(int level) {
        if (level >= 30) return 9 * level - 158;
        if (level >= 15) return 5 * level - 38;
        return Math.max(1, 2 * level + 7);
    }

    public static int xpCostForLevels(int startLevel, int count) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            total += xpCostOfLevel(startLevel + i);
        }
        return total;
    }

    public static ItemStack makeXpBottle(int levels) {
        ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
        bottle.set(ModComponents.STORED_LEVELS, Math.clamp(levels, 0, MAX_LEVELS));
        bottle.set(DataComponents.ITEM_NAME,
                Component.translatable("item.revamped_xp_bottle.xp_bottle").withStyle(ChatFormatting.AQUA));
        return bottle;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void playFillSound(Player player, Level world) {
        world.playSound(null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL,
                SoundSource.PLAYERS,
                0.5f, 1.0f);
    }
}
