package com.persistentarrows.mixin;

import com.persistentarrows.debug.PersistentArrowsDebugger;
import com.persistentarrows.util.ArrowPersistenceHelper;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AreaEffectCloudEntity.class)
public class AreaEffectCloudEntityMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onCloudTick(CallbackInfo ci) {
        AreaEffectCloudEntity cloud = (AreaEffectCloudEntity)(Object)this;
        
        // Track when lingering clouds apply damage
        // This helps identify instant kills from potion effects
        if (!cloud.getWorld().isClient) {
            // Check for entities within the cloud that might be taking damage
            List<LivingEntity> affectedEntities = cloud.getWorld().getEntitiesByClass(
                LivingEntity.class, 
                cloud.getBoundingBox().expand(0.5D),
                entity -> true
            );
            
            PersistentArrowsDebugger.logCloudTick(cloud, affectedEntities.size());
            
            for (LivingEntity entity : affectedEntities) {
                // Check if entity is about to die from cloud effects
                if (entity.getHealth() <= 1.0F && entity.isAlive()) {
                    PersistentArrowsDebugger.info("Low health entity in cloud: " + entity.getType().getTranslationKey() + 
                        " (Health: " + entity.getHealth() + ") - potential instant kill target");
                }
            }
        }
    }
}