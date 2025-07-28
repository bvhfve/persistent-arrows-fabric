package com.persistentarrows.tracking;

import com.persistentarrows.debug.PersistentArrowsDebugger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.LingeringPotionItem;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects when lingering arrows hit entities and monitors for death after impact.
 * This triggers the arrow respawn mechanism when entities die from arrow effects.
 */
public class ArrowImpactDetector {
    
    // Track arrows that have hit entities and are waiting for potential death
    private static final Map<UUID, ArrowHitData> pendingHits = new ConcurrentHashMap<>();
    
    /**
     * Handle arrow-entity collision events.
     * Called when an arrow hits any entity.
     */
    public static void onArrowHitEntity(PersistentProjectileEntity arrow, Entity target) {
        if (!isLingeringArrow(arrow)) {
            return; // Only care about lingering arrows
        }
        
        UUID arrowId = arrow.getUuid();
        
        // Check if this arrow is being tracked
        if (!LingeringArrowTracker.isTracked(arrowId)) {
            PersistentArrowsDebugger.debug("Lingering arrow hit entity but was not tracked: " + arrowId);
            return;
        }
        
        PersistentArrowsDebugger.info("Tracked lingering arrow hit entity: " + target.getType().getTranslationKey());
        PersistentArrowsDebugger.info("Arrow ID: " + arrowId);
        PersistentArrowsDebugger.info("Target Health: " + (target instanceof LivingEntity living ? living.getHealth() : "N/A"));
        
        // Store impact data for potential respawn
        if (target instanceof LivingEntity livingTarget) {
            handleLivingEntityImpact(arrow, livingTarget);
        }
    }
    
    /**
     * Handle impact with living entities specifically.
     */
    private static void handleLivingEntityImpact(PersistentProjectileEntity arrow, LivingEntity target) {
        UUID arrowId = arrow.getUuid();
        float targetHealth = target.getHealth();
        
        PersistentArrowsDebugger.info("Lingering arrow impact details:");
        PersistentArrowsDebugger.info("  Arrow: " + arrowId);
        PersistentArrowsDebugger.info("  Target: " + target.getType().getTranslationKey());
        PersistentArrowsDebugger.info("  Target Health: " + targetHealth + "/" + target.getMaxHealth());
        PersistentArrowsDebugger.info("  Target Alive: " + target.isAlive());
        
        // Store hit data for health-based detection
        ArrowHitData hitData = new ArrowHitData(arrowId, target.getUuid(), targetHealth, System.currentTimeMillis());
        pendingHits.put(arrowId, hitData);
        
        // Mark arrow data for potential respawn based on target health
        ArrowNBTData arrowData = LingeringArrowTracker.getTrackedArrowData(arrowId);
        if (arrowData != null && targetHealth <= 10.0f) {
            arrowData.markForRespawn(target.getWorld());
            PersistentArrowsDebugger.info("Arrow marked for respawn - target health: " + targetHealth);
        }
    }
    
    /**
     * Handle arrow despawn - check if arrow was marked for respawn.
     * Called when an arrow is removed from tracking.
     */
    public static void onArrowDespawn(UUID arrowId) {
        ArrowHitData hitData = pendingHits.remove(arrowId);
        if (hitData != null) {
            PersistentArrowsDebugger.info("Arrow despawned, checking for respawn: " + arrowId);
            // Cleanup - no longer needed since we use health-based detection
        }
    }
    
    /**
     * Check if an arrow is a lingering potion arrow or tipped arrow with potion effects.
     */
    public static boolean isLingeringArrow(PersistentProjectileEntity arrow) {
        // Check if it's a lingering potion item
        if (arrow.getItemStack().getItem() instanceof LingeringPotionItem) {
            return true;
        }
        
        // Check if it's a tipped arrow with potion effects
        if (arrow.getItemStack().getItem() instanceof net.minecraft.item.TippedArrowItem) {
            // Tipped arrows with any potion effects should be considered "lingering" for our purposes
            try {
                // Use reflection to check for potion contents to avoid version-specific imports
                return arrow.getItemStack().getComponents().toString().contains("potion_contents") ||
                       arrow.getItemStack().getComponents().toString().contains("PotionContents");
            } catch (Exception e) {
                // Fallback: assume tipped arrows have potion contents
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determine if damage would be instantly lethal.
     */
    public static boolean wouldBeInstantKill(LivingEntity target, float damage) {
        return damage >= target.getHealth() && target.isAlive();
    }
    
    /**
     * Simple data class to track arrow hits for cleanup purposes.
     */
    private static class ArrowHitData {
        private final UUID arrowId;
        private final UUID targetId;
        private final float originalHealth;
        private final long hitTime;
        
        public ArrowHitData(UUID arrowId, UUID targetId, float originalHealth, long hitTime) {
            this.arrowId = arrowId;
            this.targetId = targetId;
            this.originalHealth = originalHealth;
            this.hitTime = hitTime;
        }
        
        public UUID getArrowId() { return arrowId; }
        public UUID getTargetId() { return targetId; }
        public float getOriginalHealth() { return originalHealth; }
        public long getHitTime() { return hitTime; }
    }
}