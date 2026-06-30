package com.zazac1.revampedxpbottle.mixin;

import com.zazac1.revampedxpbottle.ModComponents;
import com.zazac1.revampedxpbottle.RevampedXpBottleMod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownExperienceBottle.class)
public class ThrownExperienceBottleMixin {
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void revampedXpBottle$onHit(HitResult hitResult, CallbackInfo ci) {
        ThrownExperienceBottle self = (ThrownExperienceBottle) (Object) this;

        if (!(self.level() instanceof ServerLevel serverLevel)) return;

        Integer storedLevels = self.getItem().get(ModComponents.STORED_LEVELS);
        if (storedLevels == null || storedLevels <= 0) return;

        int xpAmount = RevampedXpBottleMod.xpCostForLevels(0, storedLevels);
        ExperienceOrb.award(serverLevel, self.position(), xpAmount);
        self.discard();
        ci.cancel();
    }
}
