package com.persistentarrows.mixin;

import com.persistentarrows.debug.PersistentArrowsDebugger;
import com.persistentarrows.util.ArrowPersistenceHelper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    
    @Inject(method = "damage", at = @At("HEAD"))
    private void onPotionDamageStart(net.minecraft.server.world.ServerWorld world, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        
        // Only log damage from area effect clouds to reduce spam
        if (damageSource.getSource() instanceof AreaEffectCloudEntity cloud) {
            PersistentArrowsDebugger.logDamageEvent(entity, damageSource, amount, "START");
            // Check if this damage would be instantly lethal
            if (amount >= entity.getHealth() && entity.isAlive()) {
                PersistentArrowsDebugger.info("Potential instant kill detected - will confirm after damage application");
            }
        }
    }
    
    @Inject(method = "damage", at = @At("RETURN"))
    private void onPotionDamageEnd(net.minecraft.server.world.ServerWorld world, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        
        // Only log damage from area effect clouds to reduce spam
        if (damageSource.getSource() instanceof AreaEffectCloudEntity cloud) {
            PersistentArrowsDebugger.logDamageEvent(entity, damageSource, amount, "END");
            if (!entity.isAlive() && cir.getReturnValue()) {
                PersistentArrowsDebugger.info("INSTANT KILL CONFIRMED - Entity died from area effect cloud damage");
                // Entity was instantly killed by lingering potion cloud
                ArrowPersistenceHelper.onInstantKill(entity, cloud);
            } else if (!entity.isAlive()) {
                PersistentArrowsDebugger.warn("Entity died but damage was not applied successfully");
            } else {
                PersistentArrowsDebugger.debug("Entity survived area effect cloud damage");
            }
        }
    }
}