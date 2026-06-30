package com.zazac1.revampedxpbottle.client;

import com.zazac1.revampedxpbottle.RevampedXpBottleMod;
import com.zazac1.revampedxpbottle.ModComponents;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class RevampedXpBottleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		registerTooltips();
	}

	/**
	 * Affiche le nombre de niveaux stockés dans la bottle en tooltip.
	 */
	private void registerTooltips() {
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
			if (!stack.is(Items.EXPERIENCE_BOTTLE)) return;
			Integer levels = stack.get(ModComponents.STORED_LEVELS);
			if (levels == null || levels == 0) return;

			lines.add(Component
				.literal("\u00a77Levels: " + levels + "/" + RevampedXpBottleMod.MAX_LEVELS)
				.withStyle(ChatFormatting.DARK_GREEN));
		});
	}
}