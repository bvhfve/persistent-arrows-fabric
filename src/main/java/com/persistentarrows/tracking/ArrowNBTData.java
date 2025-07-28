package com.persistentarrows.tracking;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.UUID;

/**
 * Lightweight data class to store essential information for tracked arrows.
 * Optimized for performance with minimal memory footprint.
 */
public class ArrowNBTData {
    private final UUID arrowId;
    private final ItemStack arrowItem;
    private final String arrowType;
    private final Vec3d position;
    private final Vec3d velocity;
    private final long trackingStartTime;
    private boolean inBubbleColumn;
    private long lastSeenTime;
    private boolean markedForRespawn;
    private World respawnWorld;
    
    public ArrowNBTData(UUID arrowId, ItemStack arrowItem, Vec3d position, Vec3d velocity) {
        this.arrowId = arrowId;
        this.arrowItem = arrowItem.copy(); // Store a copy to preserve original properties
        this.arrowType = arrowItem.getItem().getTranslationKey();
        this.position = position;
        this.velocity = velocity;
        this.trackingStartTime = System.currentTimeMillis();
        this.lastSeenTime = this.trackingStartTime;
        this.inBubbleColumn = false;
        this.markedForRespawn = false;
        this.respawnWorld = null;
    }
    
    // Convenience constructor with zero velocity
    public ArrowNBTData(UUID arrowId, ItemStack arrowItem, Vec3d position) {
        this(arrowId, arrowItem, position, Vec3d.ZERO);
    }
    
    public UUID getArrowId() {
        return arrowId;
    }
    
    public ItemStack getArrowItem() {
        return arrowItem.copy(); // Return a copy to prevent external mutations
    }
    
    public String getArrowType() {
        return arrowType;
    }
    
    public Vec3d getPosition() {
        return position;
    }
    
    public Vec3d getVelocity() {
        return velocity;
    }
    
    public long getTrackingStartTime() {
        return trackingStartTime;
    }
    
    public boolean isInBubbleColumn() {
        return inBubbleColumn;
    }
    
    public void setInBubbleColumn(boolean inBubbleColumn) {
        this.inBubbleColumn = inBubbleColumn;
    }
    
    public long getLastSeenTime() {
        return lastSeenTime;
    }
    
    public void resetLastSeenTime() {
        this.lastSeenTime = System.currentTimeMillis();
    }
    
    public boolean isMarkedForRespawn() {
        return markedForRespawn;
    }
    
    public World getRespawnWorld() {
        return respawnWorld;
    }
    
    public void markForRespawn(World world) {
        this.markedForRespawn = true;
        this.respawnWorld = world;
    }
    
    /**
     * Check if this arrow data is still valid (not too old).
     * Prevents memory leaks from arrows that disappeared.
     * Optimized based on knowledgebase performance patterns.
     */
    public boolean isValid() {
        long maxAge = 5 * 60 * 1000; // 5 minutes - reasonable cleanup interval
        return (System.currentTimeMillis() - trackingStartTime) < maxAge;
    }
    
    /**
     * Check if this is a tipped arrow (for respawn logic).
     */
    public boolean isTippedArrow() {
        return arrowType.contains("tipped_arrow");
    }
    
    /**
     * Check if this is a lingering potion arrow.
     */
    public boolean isLingeringArrow() {
        return arrowType.contains("lingering");
    }
    
    @Override
    public String toString() {
        return String.format("ArrowData{id=%s, type=%s, pos=%s, inBubble=%s, age=%dms}", 
            arrowId.toString().substring(0, 8), arrowType, position, inBubbleColumn, 
            System.currentTimeMillis() - trackingStartTime);
    }
}