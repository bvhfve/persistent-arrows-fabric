package com.persistentarrows.debug;

import com.persistentarrows.PersistentArrows;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Comprehensive debugging class for Persistent Arrows mod.
 * Logs every possible interaction, error, and state change for thorough testing.
 */
public class PersistentArrowsDebugger {
    
    private static final String PREFIX = "[PersistentArrows Debug] ";
    private static final String SEPARATOR = "================================================";
    
    // Debug levels
    public static boolean VERBOSE_LOGGING = true;
    public static boolean LOG_ARROW_TRACKING = true;
    public static boolean LOG_DAMAGE_EVENTS = true;
    public static boolean LOG_CLOUD_EVENTS = true;
    public static boolean LOG_FIELD_ACCESS = true;
    public static boolean LOG_PERSISTENCE_DECISIONS = true;
    
    /**
     * Initialize debugging system
     */
    public static void init() {
        info("=== PERSISTENT ARROWS DEBUG SYSTEM INITIALIZED ===");
        info("Verbose Logging: " + VERBOSE_LOGGING);
        info("Arrow Tracking: " + LOG_ARROW_TRACKING);
        info("Damage Events: " + LOG_DAMAGE_EVENTS);
        info("Cloud Events: " + LOG_CLOUD_EVENTS);
        info("Field Access: " + LOG_FIELD_ACCESS);
        info("Persistence Decisions: " + LOG_PERSISTENCE_DECISIONS);
        info(SEPARATOR);
    }
    
    // === ARROW TRACKING DEBUG ===
    
    public static void logArrowHit(PersistentProjectileEntity arrow, LivingEntity target) {
        if (!LOG_ARROW_TRACKING) return;
        
        info("ARROW HIT EVENT:");
        info("  Arrow UUID: " + arrow.getUuid());
        info("  Arrow Type: " + arrow.getClass().getSimpleName());
        info("  Target: " + target.getType().getTranslationKey() + " (UUID: " + target.getUuid() + ")");
        info("  Target Health: " + target.getHealth() + "/" + target.getMaxHealth());
        info("  Target Position: " + target.getBlockPos());
        info("  Arrow Position: " + arrow.getBlockPos());
        info("  World: " + (arrow.getWorld().isClient ? "CLIENT" : "SERVER"));
        
        // Check arrow item
        ItemStack arrowItem = arrow.getItemStack();
        info("  Arrow Item: " + arrowItem.getItem().getTranslationKey());
        info("  Is Lingering Potion: " + (arrowItem.getItem() instanceof LingeringPotionItem));
        
        if (arrowItem.getItem() instanceof LingeringPotionItem) {
            try {
                // Try to get potion information - may vary by version
                info("  Potion Item: Lingering Potion detected");
                info("  Item Components: " + arrowItem.getComponents().toString());
            } catch (Exception e) {
                info("  Potion Info: Unable to extract potion details");
            }
        }
        
        info("  Arrow InGround: " + getArrowInGroundState(arrow));
        info("  Arrow Life: " + getArrowLifeState(arrow));
        info(SEPARATOR);
    }
    
    // Overloaded method for generic Entity targets
    public static void logArrowHit(PersistentProjectileEntity arrow, net.minecraft.entity.Entity target) {
        if (!LOG_ARROW_TRACKING) return;
        
        info("ARROW HIT EVENT:");
        info("  Arrow UUID: " + arrow.getUuid());
        info("  Arrow Type: " + arrow.getClass().getSimpleName());
        info("  Target: " + target.getType().getTranslationKey() + " (UUID: " + target.getUuid() + ")");
        info("  Target Position: " + target.getBlockPos());
        info("  Arrow Position: " + arrow.getBlockPos());
        info("  World: " + (arrow.getWorld().isClient ? "CLIENT" : "SERVER"));
        
        // Check arrow item
        ItemStack arrowItem = arrow.getItemStack();
        info("  Arrow Item: " + arrowItem.getItem().getTranslationKey());
        info("  Is Lingering Potion: " + (arrowItem.getItem() instanceof LingeringPotionItem));
        
        if (arrowItem.getItem() instanceof LingeringPotionItem) {
            try {
                // Try to get potion information - may vary by version
                info("  Potion Item: Lingering Potion detected");
                info("  Item Components: " + arrowItem.getComponents().toString());
            } catch (Exception e) {
                info("  Potion Info: Unable to extract potion details");
            }
        }
        
        info("  Arrow InGround: " + getArrowInGroundState(arrow));
        info("  Arrow Life: " + getArrowLifeState(arrow));
        info(SEPARATOR);
    }
    
    public static void logArrowTracking(PersistentProjectileEntity arrow, LivingEntity target, boolean wasTracked) {
        if (!LOG_ARROW_TRACKING) return;
        
        info("ARROW TRACKING DECISION:");
        info("  Arrow UUID: " + arrow.getUuid());
        info("  Target: " + target.getType().getTranslationKey());
        info("  Was Tracked: " + wasTracked);
        info("  Reason: " + (wasTracked ? "Lingering potion arrow detected" : "Not a lingering potion arrow"));
        info(SEPARATOR);
    }
    
    // === DAMAGE EVENT DEBUG ===
    
    public static void logDamageEvent(LivingEntity target, DamageSource source, float amount, String phase) {
        if (!LOG_DAMAGE_EVENTS) return;
        
        info("DAMAGE EVENT (" + phase + "):");
        info("  Target: " + target.getType().getTranslationKey() + " (UUID: " + target.getUuid() + ")");
        info("  Target Health Before: " + target.getHealth() + "/" + target.getMaxHealth());
        info("  Damage Amount: " + amount);
        info("  Damage Source: " + source.getName());
        info("  Source Entity: " + (source.getSource() != null ? source.getSource().getType().getTranslationKey() : "null"));
        info("  Is Area Effect Cloud: " + (source.getSource() instanceof AreaEffectCloudEntity));
        info("  Would Be Lethal: " + (amount >= target.getHealth()));
        info("  Target Is Alive: " + target.isAlive());
        info("  World: " + (target.getWorld().isClient ? "CLIENT" : "SERVER"));
        info(SEPARATOR);
    }
    
    public static void logInstantKill(LivingEntity target, AreaEffectCloudEntity cloud) {
        if (!LOG_DAMAGE_EVENTS) return;
        
        info("INSTANT KILL DETECTED:");
        info("  Target: " + target.getType().getTranslationKey() + " (UUID: " + target.getUuid() + ")");
        info("  Target Health: " + target.getHealth());
        info("  Target Is Alive: " + target.isAlive());
        info("  Cloud UUID: " + cloud.getUuid());
        info("  Cloud Position: " + cloud.getBlockPos());
        info("  Cloud Effects: [Debug info unavailable - method not found]");
        info("  Cloud Owner: " + (cloud.getOwner() != null ? cloud.getOwner().getType().getTranslationKey() : "null"));
        info("  World: " + (target.getWorld().isClient ? "CLIENT" : "SERVER"));
        info(SEPARATOR);
    }
    
    // === CLOUD EVENT DEBUG ===
    
    public static void logCloudTick(AreaEffectCloudEntity cloud, int nearbyEntities) {
        if (!LOG_CLOUD_EVENTS || !VERBOSE_LOGGING) return;
        
        debug("CLOUD TICK:");
        debug("  Cloud UUID: " + cloud.getUuid());
        debug("  Cloud Position: " + cloud.getBlockPos());
        debug("  Cloud Age: " + cloud.age);
        debug("  Cloud Duration: " + cloud.getDuration());
        debug("  Nearby Entities: " + nearbyEntities);
        debug("  Cloud Effects: [Debug info unavailable - method not found]");
        debug("  World: " + (cloud.getWorld().isClient ? "CLIENT" : "SERVER"));
    }
    
    // === FIELD ACCESS DEBUG ===
    
    public static void logFieldAccess(PersistentProjectileEntity arrow, String fieldName, Object value, boolean success) {
        if (!LOG_FIELD_ACCESS) return;
        
        info("FIELD ACCESS ATTEMPT:");
        info("  Arrow UUID: " + arrow.getUuid());
        info("  Field Name: " + fieldName);
        info("  New Value: " + value);
        info("  Success: " + success);
        info("  Arrow Type: " + arrow.getClass().getSimpleName());
        info("  World: " + (arrow.getWorld().isClient ? "CLIENT" : "SERVER"));
        
        if (!success) {
            error("  FIELD ACCESS FAILED for " + fieldName);
        }
        
        info(SEPARATOR);
    }
    
    public static void logReflectionError(Exception e, String operation) {
        if (!LOG_FIELD_ACCESS) return;
        
        error("REFLECTION ERROR during " + operation + ":");
        error("  Exception Type: " + e.getClass().getSimpleName());
        error("  Message: " + e.getMessage());
        error("  Stack Trace: ");
        for (StackTraceElement element : e.getStackTrace()) {
            error("    " + element.toString());
        }
        error(SEPARATOR);
    }
    
    // === PERSISTENCE DECISION DEBUG ===
    
    public static void logPersistenceCheck(PersistentProjectileEntity arrow, boolean shouldPersist, String reason) {
        if (!LOG_PERSISTENCE_DECISIONS) return;
        
        info("PERSISTENCE CHECK:");
        info("  Arrow UUID: " + arrow.getUuid());
        info("  Should Persist: " + shouldPersist);
        info("  Reason: " + reason);
        info("  Arrow Position: " + arrow.getBlockPos());
        info("  Arrow InGround: " + getArrowInGroundState(arrow));
        info("  Arrow Life: " + getArrowLifeState(arrow));
        info("  World: " + (arrow.getWorld().isClient ? "CLIENT" : "SERVER"));
        info(SEPARATOR);
    }
    
    public static void logArrowPersistenceApplied(PersistentProjectileEntity arrow, boolean inGroundSet, boolean lifeReset) {
        if (!LOG_PERSISTENCE_DECISIONS) return;
        
        info("PERSISTENCE APPLIED:");
        info("  Arrow UUID: " + arrow.getUuid());
        info("  InGround Set to False: " + inGroundSet);
        info("  Life Reset to Zero: " + lifeReset);
        info("  Arrow Position: " + arrow.getBlockPos());
        info("  World: " + (arrow.getWorld().isClient ? "CLIENT" : "SERVER"));
        info(SEPARATOR);
    }
    
    // === HELPER TRACKING DEBUG ===
    
    public static void logHelperState(int trackedArrows, int persistentArrows) {
        if (!LOG_PERSISTENCE_DECISIONS || !VERBOSE_LOGGING) return;
        
        debug("HELPER STATE:");
        debug("  Tracked Arrows: " + trackedArrows);
        debug("  Persistent Arrows: " + persistentArrows);
    }
    
    public static void logArrowCleanup(UUID arrowId, String reason) {
        if (!LOG_ARROW_TRACKING) return;
        
        info("ARROW CLEANUP:");
        info("  Arrow UUID: " + arrowId);
        info("  Reason: " + reason);
        info(SEPARATOR);
    }
    
    // === UTILITY METHODS ===
    
    private static String getArrowInGroundState(PersistentProjectileEntity arrow) {
        try {
            java.lang.reflect.Field inGroundField = PersistentProjectileEntity.class.getDeclaredField("inGround");
            inGroundField.setAccessible(true);
            return String.valueOf(inGroundField.getBoolean(arrow));
        } catch (Exception e) {
            return "UNKNOWN (reflection failed)";
        }
    }
    
    private static String getArrowLifeState(PersistentProjectileEntity arrow) {
        try {
            java.lang.reflect.Field lifeField = PersistentProjectileEntity.class.getDeclaredField("life");
            lifeField.setAccessible(true);
            return String.valueOf(lifeField.getInt(arrow));
        } catch (Exception e) {
            return "UNKNOWN (reflection failed)";
        }
    }
    
    // === LOGGING METHODS ===
    
    public static void info(String message) {
        PersistentArrows.LOGGER.info(PREFIX + message);
    }
    
    public static void debug(String message) {
        if (VERBOSE_LOGGING) {
            PersistentArrows.LOGGER.info(PREFIX + "[DEBUG] " + message);
        }
    }
    
    public static void warn(String message) {
        PersistentArrows.LOGGER.warn(PREFIX + message);
    }
    
    public static void error(String message) {
        PersistentArrows.LOGGER.error(PREFIX + message);
    }
    
    // === TEST METHODS ===
    
    public static void logTestScenario(String scenarioName) {
        info("=== TEST SCENARIO: " + scenarioName + " ===");
    }
    
    public static void logExpectedBehavior(String behavior) {
        info("EXPECTED: " + behavior);
    }
    
    public static void logActualBehavior(String behavior) {
        info("ACTUAL: " + behavior);
    }
    
    public static void logTestResult(boolean passed) {
        if (passed) {
            info("TEST RESULT: ✅ PASSED");
        } else {
            error("TEST RESULT: ❌ FAILED");
        }
        info(SEPARATOR);
    }
}