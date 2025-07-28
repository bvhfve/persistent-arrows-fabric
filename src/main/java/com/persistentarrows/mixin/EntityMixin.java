package com.persistentarrows.mixin;

import com.persistentarrows.tracking.LingeringArrowTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This new mixin targets the 'remove' method in the base Entity class
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "remove(Lnet/minecraft/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        // First, cast 'this' to an Entity to check its type
        Entity entity = (Entity)(Object)this;

        // CRITICAL: We only want to run this code for arrows, not every entity.
        // This 'instanceof' check ensures your logic is applied correctly.
        if (entity instanceof PersistentProjectileEntity) {
            PersistentProjectileEntity arrow = (PersistentProjectileEntity) entity;

            // Only process on server side
            if (!arrow.getWorld().isClient()) {
                // Clean up tracking data
                LingeringArrowTracker.stopTracking(arrow.getUuid(), "Arrow removed: " + reason);
            }
        }
    }
}