package com.zazac1.revampedxpbottle.client.mixin;

import com.zazac1.revampedxpbottle.FillBottlePayload;
import com.zazac1.revampedxpbottle.ModComponents;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class RevampedXpBottleClientMixin {
    @Inject(at = @At("HEAD"), method = "startAttack", cancellable = true)
    private void revampedXpBottle$fillBottleOnLeftClick(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = (Minecraft) (Object) this;
        if (client.player == null) return;

        ItemStack stack = client.player.getMainHandItem();
        if (!stack.is(Items.EXPERIENCE_BOTTLE) || !stack.has(ModComponents.STORED_LEVELS)) return;
        if (!ClientPlayNetworking.canSend(FillBottlePayload.TYPE)) return;

        ClientPlayNetworking.send(new FillBottlePayload());
        cir.setReturnValue(true);
    }
}
