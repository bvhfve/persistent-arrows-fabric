package com.persistentarrows.mixin;

import com.persistentarrows.tracking.LingeringArrowTracker;
import com.persistentarrows.tracking.RespawnScheduler;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for ServerWorld to handle periodic cleanup of tracking data.
 */
@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    
    private int cleanupCounter = 0;
    
    /**
     * Perform periodic cleanup and process respawn requests.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onServerTick(CallbackInfo ci) {
        // Process pending arrow respawns every tick
        RespawnScheduler.processPendingRespawns();
        
        // Clean up old data every 5 seconds (100 ticks)
        cleanupCounter++;
        if (cleanupCounter >= 100) {
            LingeringArrowTracker.cleanupOldData();
            cleanupCounter = 0;
        }
    }
}