package com.persistentarrows.mixin;

import com.persistentarrows.debug.PersistentArrowsDebugger;
import com.persistentarrows.tracking.LingeringArrowTracker;
import com.persistentarrows.tracking.ArrowImpactDetector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This mixin correctly targets methods specific to PersistentProjectileEntity
@Mixin(PersistentProjectileEntity.class)
public class PersistentArrowMixin {

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onArrowHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object)this;
        Entity target = entityHitResult.getEntity();

        // Only process on server side
        if (!arrow.getWorld().isClient()) {
            if (target instanceof LivingEntity livingTarget) {
                PersistentArrowsDebugger.logArrowHit(arrow, livingTarget);
            } else {
                PersistentArrowsDebugger.logArrowHit(arrow, target);
            }

            // Use new impact detection system
            ArrowImpactDetector.onArrowHitEntity(arrow, target);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object)this;

        // Only process on server side
        if (!arrow.getWorld().isClient()) {
            // Use new tracking system instead of old reflection approach
            LingeringArrowTracker.checkAndTrackArrow(arrow);
        }
    }
}