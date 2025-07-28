package com.persistentarrows.tracking;

import com.persistentarrows.debug.PersistentArrowsDebugger;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles scheduling arrow respawns for the next server tick.
 * This ensures respawns happen safely in the main server thread.
 */
public class RespawnScheduler {
    
    private static final Queue<RespawnRequest> pendingRespawns = new ConcurrentLinkedQueue<>();
    
    /**
     * Data class for respawn requests.
     */
    public static class RespawnRequest {
        public final UUID originalArrowId;
        public final Vec3d impactPosition;
        public final ServerWorld world;
        public final long requestTime;
        
        public RespawnRequest(UUID originalArrowId, Vec3d impactPosition, ServerWorld world) {
            this.originalArrowId = originalArrowId;
            this.impactPosition = impactPosition;
            this.world = world;
            this.requestTime = System.currentTimeMillis();
        }
        
        public boolean isValid() {
            // Requests are valid for 5 seconds
            return (System.currentTimeMillis() - requestTime) < 5000;
        }
    }
    
    /**
     * Schedule an arrow respawn for the next server tick.
     */
    public static void scheduleRespawn(UUID originalArrowId, Vec3d impactPosition, ServerWorld world) {
        RespawnRequest request = new RespawnRequest(originalArrowId, impactPosition, world);
        pendingRespawns.offer(request);
        
        PersistentArrowsDebugger.info("Scheduled arrow respawn: " + originalArrowId + " at " + impactPosition);
    }
    
    /**
     * Process all pending respawn requests. Called from server tick.
     */
    public static void processPendingRespawns() {
        RespawnRequest request;
        int processed = 0;
        
        while ((request = pendingRespawns.poll()) != null) {
            if (request.isValid()) {
                processRespawnRequest(request);
                processed++;
            } else {
                PersistentArrowsDebugger.warn("Discarded expired respawn request: " + request.originalArrowId);
            }
        }
        
        if (processed > 0) {
            PersistentArrowsDebugger.info("Processed " + processed + " arrow respawn requests");
        }
    }
    
    /**
     * Process a single respawn request.
     */
    private static void processRespawnRequest(RespawnRequest request) {
        ArrowNBTData arrowData = LingeringArrowTracker.getTrackedArrowData(request.originalArrowId);
        
        if (arrowData == null) {
            PersistentArrowsDebugger.warn("Cannot process respawn - no tracked data for: " + request.originalArrowId);
            return;
        }
        
        try {
            // Create the new arrow with preserved NBT data
            ArrowRespawner.respawnArrow(arrowData, request.world);
            
            // Clean up the original tracking data
            LingeringArrowTracker.stopTracking(request.originalArrowId, "Successfully respawned");
            
        } catch (Exception e) {
            PersistentArrowsDebugger.warn("Failed to process respawn request: " + e.getMessage());
        }
    }
    
    /**
     * Get the number of pending respawn requests.
     */
    public static int getPendingRespawnCount() {
        return pendingRespawns.size();
    }
}