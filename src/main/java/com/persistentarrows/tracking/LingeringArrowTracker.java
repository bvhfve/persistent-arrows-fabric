package com.persistentarrows.tracking;

import com.persistentarrows.debug.PersistentArrowsDebugger;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks lingering arrows floating in bubble columns and stores their NBT data.
 * This is the core component for the NBT tracking & respawn system.
 */
public class LingeringArrowTracker {
    // Thread-safe storage for tracked arrows
    private static final Map<UUID, ArrowNBTData> trackedArrows = new ConcurrentHashMap<>();
    
    /**
     * Check if an arrow should be tracked (lingering arrow in bubble column).
     * Fixed to prevent constant tracking/untracking when arrows bounce.
     */
    public static void checkAndTrackArrow(PersistentProjectileEntity arrow) {
        if (!isLingeringArrow(arrow)) {
            return;
        }
        
        UUID arrowId = arrow.getUuid();
        Vec3d position = arrow.getPos();
        boolean inBubbleColumn = isInBubbleColumn(arrow.getWorld(), position);
        
        ArrowNBTData existingData = trackedArrows.get(arrowId);
        
        if (inBubbleColumn) {
            if (existingData == null) {
                // Start tracking new arrow in bubble column
                startTracking(arrow);
            } else {
                // Update bubble column status and reset timeout
                existingData.setInBubbleColumn(true);
                existingData.resetLastSeenTime();
            }
        } else {
            if (existingData != null) {
                // Arrow temporarily left bubble column, but don't stop tracking immediately
                existingData.setInBubbleColumn(false);
                
                // Only stop tracking if arrow has been outside bubble column for too long
                long timeSinceLastSeen = System.currentTimeMillis() - existingData.getLastSeenTime();
                if (timeSinceLastSeen > 2000) { // 2 seconds grace period
                    stopTracking(arrowId, "Left bubble column for too long");
                }
            }
        }
    }
    
    /**
     * Start tracking a lingering arrow by storing its complete NBT data.
     */
    public static void startTracking(PersistentProjectileEntity arrow) {
        UUID arrowId = arrow.getUuid();
        
        // Store essential arrow data for respawning (no complex NBT needed)
        ItemStack arrowItem = arrow.getItemStack();
        String arrowType = arrowItem.getItem().getTranslationKey();
        
        PersistentArrowsDebugger.info("Storing arrow data - Type: " + arrowType);
        
        Vec3d position = arrow.getPos();
        Vec3d velocity = arrow.getVelocity();
        
        ArrowNBTData arrowData = new ArrowNBTData(arrowId, arrowItem, position, velocity);
        arrowData.setInBubbleColumn(true);
        
        trackedArrows.put(arrowId, arrowData);
        
        PersistentArrowsDebugger.info("Started tracking lingering arrow: " + arrowId + " at " + position);
        PersistentArrowsDebugger.info("Total tracked arrows: " + trackedArrows.size());
    }
    
    /**
     * Stop tracking an arrow and clean up its data.
     * Check if arrow was marked for respawn and handle it.
     */
    public static void stopTracking(UUID arrowId, String reason) {
        ArrowNBTData removed = trackedArrows.remove(arrowId);
        if (removed != null) {
            PersistentArrowsDebugger.info("Stopped tracking arrow " + arrowId + " - Reason: " + reason);
            PersistentArrowsDebugger.info("Total tracked arrows: " + trackedArrows.size());
            
            // Check if arrow was marked for respawn
            if (removed.isMarkedForRespawn() && removed.getRespawnWorld() != null) {
                PersistentArrowsDebugger.info("Respawning arrow after despawn: " + arrowId);
                ArrowRespawner.respawnArrow(removed, removed.getRespawnWorld());
            }
            
            // Notify impact detector of despawn
            ArrowImpactDetector.onArrowDespawn(arrowId);
        }
    }
    
    /**
     * Get stored NBT data for a tracked arrow.
     */
    public static ArrowNBTData getTrackedArrowData(UUID arrowId) {
        return trackedArrows.get(arrowId);
    }
    
    /**
     * Check if an arrow is currently being tracked.
     */
    public static boolean isTracked(UUID arrowId) {
        return trackedArrows.containsKey(arrowId);
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
     * Check if a position is within a bubble column.
     */
    private static boolean isInBubbleColumn(World world, Vec3d position) {
        BlockPos blockPos = BlockPos.ofFloored(position);
        BlockState blockState = world.getBlockState(blockPos);
        
        // Check if the block is a bubble column
        return blockState.isOf(Blocks.BUBBLE_COLUMN);
    }
    
    /**
     * Clean up old/invalid arrow data to prevent memory leaks.
     */
    public static void cleanupOldData() {
        trackedArrows.entrySet().removeIf(entry -> {
            ArrowNBTData data = entry.getValue();
            if (!data.isValid()) {
                PersistentArrowsDebugger.debug("Cleaned up old arrow data: " + entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Get the number of currently tracked arrows (for debugging).
     */
    public static int getTrackedArrowCount() {
        return trackedArrows.size();
    }
    
    /**
     * Get all tracked arrow data (for debugging).
     */
    public static Map<UUID, ArrowNBTData> getAllTrackedArrows() {
        return new ConcurrentHashMap<>(trackedArrows);
    }
}