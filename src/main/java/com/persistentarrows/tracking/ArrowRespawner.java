package com.persistentarrows.tracking;

import com.persistentarrows.debug.PersistentArrowsDebugger;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Handles respawning arrows with preserved NBT data after instant kills.
 * Creates new arrow entities that maintain all original properties.
 */
public class ArrowRespawner {
    
    /**
     * Handle respawning an arrow after it caused an instant kill.
     */
    public static void handleInstantKillRespawn(UUID originalArrowId, Vec3d impactPosition, ServerWorld world) {
        ArrowNBTData arrowData = LingeringArrowTracker.getTrackedArrowData(originalArrowId);
        
        if (arrowData == null) {
            PersistentArrowsDebugger.warn("Cannot respawn arrow - no tracked data found for: " + originalArrowId);
            return;
        }
        
        PersistentArrowsDebugger.info("Respawning arrow after instant kill:");
        PersistentArrowsDebugger.info("  Original Arrow ID: " + originalArrowId);
        PersistentArrowsDebugger.info("  Impact Position: " + impactPosition);
        PersistentArrowsDebugger.info("  Original Position: " + arrowData.getPosition());
        
        // Schedule the respawn for the next server tick
        RespawnScheduler.scheduleRespawn(originalArrowId, impactPosition, world);
    }
    
    /**
     * Respawn an arrow using lightweight ArrowNBTData (new optimized method).
     */
    public static void respawnArrow(ArrowNBTData arrowData, World world) {
        if (world.isClient) {
            return; // Only respawn on server side
        }
        
        PersistentArrowsDebugger.info("Respawning arrow: " + arrowData.getArrowId());
        PersistentArrowsDebugger.info("  Type: " + arrowData.getArrowType());
        PersistentArrowsDebugger.info("  Position: " + arrowData.getPosition());
        
        try {
            // Create new arrow entity with the same item stack (preserves potion effects)
            ArrowEntity newArrow = new ArrowEntity(world, arrowData.getPosition().x, 
                arrowData.getPosition().y, arrowData.getPosition().z, 
                arrowData.getArrowItem(), null);
            
            // Set velocity to zero (floating arrow)
            newArrow.setVelocity(0, 0, 0);
            
            // Spawn the arrow
            world.spawnEntity(newArrow);
            
            // Critical: Start tracking the respawned arrow to continue the persistence loop
            if (LingeringArrowTracker.isLingeringArrow(newArrow)) {
                LingeringArrowTracker.startTracking(newArrow);
                PersistentArrowsDebugger.info("Started tracking respawned arrow: " + newArrow.getUuid());
            }
            
            PersistentArrowsDebugger.info("Successfully respawned and re-tracked arrow at " + arrowData.getPosition());
            
        } catch (Exception e) {
            PersistentArrowsDebugger.error("Failed to respawn arrow: " + e.getMessage());
        }
    }
    
    /**
     * Create a new arrow entity with preserved NBT data (legacy method).
     */
    public static PersistentProjectileEntity respawnArrowLegacy(World world, Vec3d position, NbtCompound originalNBT) {
        if (!(world instanceof ServerWorld serverWorld)) {
            PersistentArrowsDebugger.warn("Cannot respawn arrow - not a server world");
            return null;
        }
        
        try {
            // Create new arrow entity - use the correct constructor for 1.21.7
            ArrowEntity newArrow = new ArrowEntity(world, position.x, position.y, position.z, ItemStack.EMPTY, ItemStack.EMPTY);
            
            // Apply original NBT data (excluding position and UUID)
            NbtCompound cleanedNBT = cleanNBTForRespawn(originalNBT);
            // Use reflection-based approach for reading NBT
            try {
                java.lang.reflect.Method readNbtMethod = newArrow.getClass().getMethod("readNbt", NbtCompound.class);
                readNbtMethod.invoke(newArrow, cleanedNBT);
            } catch (Exception e) {
                PersistentArrowsDebugger.warn("Failed to read NBT data via reflection: " + e.getMessage());
                // Fallback: apply basic properties manually
                if (cleanedNBT.contains("damage")) {
                    float damage = cleanedNBT.getFloat("damage").orElse(2.0f);
                    newArrow.setDamage(damage);
                }
                if (cleanedNBT.contains("pickup")) {
                    // Set pickup type if available
                    byte pickupValue = cleanedNBT.getByte("pickup").orElse((byte) 1);
                    // Note: May need additional reflection for pickup type
                }
            }
            
            // Set position explicitly
            newArrow.setPosition(position);
            
            // Set arrow to "stuck" state (not moving)
            newArrow.setVelocity(Vec3d.ZERO);
            // Set arrow to stuck state using reflection since inGround is private
            try {
                java.lang.reflect.Field inGroundField = PersistentProjectileEntity.class.getDeclaredField("inGround");
                inGroundField.setAccessible(true);
                inGroundField.setBoolean(newArrow, true);
            } catch (Exception e) {
                PersistentArrowsDebugger.warn("Could not set inGround field: " + e.getMessage());
            }
            
            // Spawn the arrow in the world
            world.spawnEntity(newArrow);
            
            PersistentArrowsDebugger.info("Successfully respawned arrow at: " + position);
            PersistentArrowsDebugger.info("New arrow ID: " + newArrow.getUuid());
            
            return newArrow;
            
        } catch (Exception e) {
            PersistentArrowsDebugger.warn("Failed to respawn arrow: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Clean NBT data for respawning - remove position and UUID to avoid conflicts.
     */
    private static NbtCompound cleanNBTForRespawn(NbtCompound originalNBT) {
        NbtCompound cleanedNBT = originalNBT.copy();
        
        // Remove position data (will be set explicitly)
        cleanedNBT.remove("Pos");
        cleanedNBT.remove("Motion");
        cleanedNBT.remove("Rotation");
        
        // Remove UUID (new entity will get new UUID)
        cleanedNBT.remove("UUID");
        
        // Remove age and pickup delay to ensure arrow behaves correctly
        cleanedNBT.remove("Age");
        cleanedNBT.remove("PickupDelay");
        
        // Ensure arrow is in ground
        cleanedNBT.putBoolean("inGround", true);
        
        return cleanedNBT;
    }
    
    /**
     * Apply stored NBT data to an arrow entity.
     */
    public static void applyNBTData(PersistentProjectileEntity arrow, NbtCompound nbt) {
        try {
            NbtCompound cleanedNBT = cleanNBTForRespawn(nbt);
            // Use reflection-based approach for reading NBT
            try {
                java.lang.reflect.Method readNbtMethod = arrow.getClass().getMethod("readNbt", NbtCompound.class);
                readNbtMethod.invoke(arrow, cleanedNBT);
            } catch (Exception reflectionException) {
                PersistentArrowsDebugger.warn("Failed to read NBT data via reflection: " + reflectionException.getMessage());
                // Fallback: apply basic properties manually
                if (cleanedNBT.contains("damage")) {
                    float damage = cleanedNBT.getFloat("damage").orElse(2.0f);
                    arrow.setDamage(damage);
                }
            }
            
            PersistentArrowsDebugger.debug("Applied NBT data to arrow: " + arrow.getUuid());
            
        } catch (Exception e) {
            PersistentArrowsDebugger.warn("Failed to apply NBT data: " + e.getMessage());
        }
    }
}