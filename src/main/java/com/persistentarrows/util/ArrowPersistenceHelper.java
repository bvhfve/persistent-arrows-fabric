package com.persistentarrows.util;

import com.persistentarrows.debug.PersistentArrowsDebugger;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.LingeringPotionItem;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class to manage arrow persistence based on Bedrock Edition behavior.
 * Tracks lingering arrows that cause instant kills and prevents their despawn.
 */
public class ArrowPersistenceHelper {
    // Simple tracking similar to Fire Arrows config approach
    private static final Map<UUID, ArrowImpactData> trackedArrows = new ConcurrentHashMap<>();
    private static final Set<UUID> persistentArrows = ConcurrentHashMap.newKeySet();
    
    public static class ArrowImpactData {
        public final PersistentProjectileEntity arrow;
        public final LivingEntity target;
        public final long impactTime;
        public boolean cloudCreated = false;
        
        public ArrowImpactData(PersistentProjectileEntity arrow, LivingEntity target) {
            this.arrow = arrow;
            this.target = target;
            this.impactTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Track a lingering arrow impact for potential persistence.
     * Called when a lingering arrow hits a living entity.
     */
    public static void trackLingeringArrowImpact(PersistentProjectileEntity arrow, LivingEntity target) {
        // Simple tracking like Fire Arrows checks blocks
        boolean isLingeringArrow = arrow.getItemStack().getItem() instanceof LingeringPotionItem;
        
        PersistentArrowsDebugger.logArrowTracking(arrow, target, isLingeringArrow);
        
        if (isLingeringArrow) {
            trackedArrows.put(arrow.getUuid(), new ArrowImpactData(arrow, target));
            PersistentArrowsDebugger.info("Arrow tracked for persistence monitoring");
        }
        
        PersistentArrowsDebugger.logHelperState(trackedArrows.size(), persistentArrows.size());
    }
    
    /**
     * Mark an arrow for persistence when it causes an instant kill.
     * Called when a mob dies instantly from lingering potion damage.
     */
    public static void onInstantKill(LivingEntity target, AreaEffectCloudEntity cloud) {
        PersistentArrowsDebugger.logInstantKill(target, cloud);
        
        // Find the arrow that created this lethal cloud
        boolean arrowFound = false;
        for (ArrowImpactData data : trackedArrows.values()) {
            if (data.target == target && !data.target.isAlive()) {
                persistentArrows.add(data.arrow.getUuid());
                PersistentArrowsDebugger.info("Arrow " + data.arrow.getUuid() + " marked for persistence due to instant kill");
                arrowFound = true;
                break;
            }
        }
        
        if (!arrowFound) {
            PersistentArrowsDebugger.warn("No tracked arrow found for instant kill target: " + target.getType().getTranslationKey());
        }
        
        PersistentArrowsDebugger.logHelperState(trackedArrows.size(), persistentArrows.size());
    }
    
    /**
     * Check if an arrow should persist (not despawn normally).
     * Used in arrow tick logic to prevent despawn.
     */
    public static boolean shouldArrowPersist(PersistentProjectileEntity arrow) {
        boolean shouldPersist = persistentArrows.contains(arrow.getUuid());
        String reason = shouldPersist ? "Arrow is marked for persistence" : "Arrow not in persistence list";
        
        PersistentArrowsDebugger.logPersistenceCheck(arrow, shouldPersist, reason);
        
        return shouldPersist;
    }
    
    /**
     * Clean up tracking data for arrows that have been removed.
     * Cleanup similar to Fire Arrows config management.
     */
    public static void cleanupArrowData(UUID arrowId) {
        boolean wasTracked = trackedArrows.containsKey(arrowId);
        boolean wasPersistent = persistentArrows.contains(arrowId);
        
        trackedArrows.remove(arrowId);
        persistentArrows.remove(arrowId);
        
        if (wasTracked || wasPersistent) {
            PersistentArrowsDebugger.logArrowCleanup(arrowId, "Manual cleanup - arrow removed/despawned");
        }
        
        PersistentArrowsDebugger.logHelperState(trackedArrows.size(), persistentArrows.size());
    }
    
    /**
     * Get the number of currently tracked arrows (for debugging).
     */
    public static int getTrackedArrowCount() {
        return trackedArrows.size();
    }
    
    /**
     * Get the number of persistent arrows (for debugging).
     */
    public static int getPersistentArrowCount() {
        return persistentArrows.size();
    }
}